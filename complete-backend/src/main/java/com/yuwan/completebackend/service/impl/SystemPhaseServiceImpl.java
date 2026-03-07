package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.SystemPhaseConfigMapper;
import com.yuwan.completebackend.mapper.SystemPhaseRecordMapper;
import com.yuwan.completebackend.model.dto.InitPhaseDTO;
import com.yuwan.completebackend.model.dto.SwitchPhaseDTO;
import com.yuwan.completebackend.model.entity.SystemPhaseConfig;
import com.yuwan.completebackend.model.entity.SystemPhaseRecord;
import com.yuwan.completebackend.model.enums.SystemPhase;
import com.yuwan.completebackend.model.vo.PhaseItemVO;
import com.yuwan.completebackend.model.vo.PhaseRecordVO;
import com.yuwan.completebackend.model.vo.PhaseStatusVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ISystemPhaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统阶段管理服务实现类
 * 提供阶段查询、初始化、切换、校验等功能
 *
 * <p>核心规则：</p>
 * <ul>
 *   <li>阶段只能按序前进（1→2→3→4），切换后不可回滚</li>
 *   <li>阶段切换后，已过阶段的写操作对非管理员冻结</li>
 *   <li>当前阶段缓存至Redis，避免频繁查库</li>
 * </ul>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SystemPhaseServiceImpl implements ISystemPhaseService {

    private final SystemPhaseConfigMapper phaseConfigMapper;
    private final SystemPhaseRecordMapper phaseRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis缓存Key - 当前阶段代码
     */
    private static final String CACHE_KEY_CURRENT_PHASE = "system:phase:current";

    /**
     * Redis缓存Key - 当前学期
     */
    private static final String CACHE_KEY_CURRENT_COHORT = "system:phase:cohort";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 总阶段数
     */
    private static final int TOTAL_PHASES = 4;

    @Override
    public PhaseStatusVO getCurrentPhaseStatus() {
        // 查询所有阶段配置
        List<SystemPhaseConfig> configs = phaseConfigMapper.selectList(
                new LambdaQueryWrapper<SystemPhaseConfig>()
                        .orderByAsc(SystemPhaseConfig::getPhaseOrder)
        );

        // 查询当前生效的阶段记录
        SystemPhaseRecord currentRecord = getCurrentRecord();

        // 查询所有历史切换记录（用于判断每个阶段的状态）
        List<SystemPhaseRecord> allRecords = phaseRecordMapper.selectList(
                new LambdaQueryWrapper<SystemPhaseRecord>()
                        .orderByAsc(SystemPhaseRecord::getPhaseOrder)
        );

        // 将切换记录按阶段代码索引，便于快速查找
        Map<String, SystemPhaseRecord> recordMap = allRecords.stream()
                .collect(Collectors.toMap(SystemPhaseRecord::getPhaseCode, r -> r, (a, b) -> b));

        // 构建响应
        PhaseStatusVO statusVO = new PhaseStatusVO();
        statusVO.setTotalPhases(TOTAL_PHASES);

        if (currentRecord != null) {
            statusVO.setPhaseCode(currentRecord.getPhaseCode());
            statusVO.setPhaseName(getPhaseNameByCode(currentRecord.getPhaseCode()));
            statusVO.setPhaseOrder(currentRecord.getPhaseOrder());
            statusVO.setSwitchTime(DATE_FORMAT.format(currentRecord.getSwitchTime()));
            statusVO.setOperatorName(currentRecord.getOperatorName());
            statusVO.setCohort(currentRecord.getCohort());
            statusVO.setInitialized(true);

            // 计算进度百分比
            int progressPercent = calculateProgress(currentRecord.getPhaseOrder());
            statusVO.setProgressPercent(progressPercent);
        } else {
            statusVO.setInitialized(false);
            statusVO.setProgressPercent(0);
        }

        // 构建阶段列表
        List<PhaseItemVO> phaseList = new ArrayList<>();
        for (SystemPhaseConfig config : configs) {
            PhaseItemVO item = new PhaseItemVO();
            item.setPhaseCode(config.getPhaseCode());
            item.setPhaseName(config.getPhaseName());
            item.setOrder(config.getPhaseOrder());
            item.setDescription(config.getPhaseDescription());
            item.setIcon(config.getPhaseIcon());
            item.setColor(config.getPhaseColor());

            // 判断阶段状态
            if (currentRecord == null) {
                item.setStatus("PENDING");
            } else if (config.getPhaseOrder() < currentRecord.getPhaseOrder()) {
                item.setStatus("COMPLETED");
                // 设置该阶段的切换时间
                SystemPhaseRecord record = recordMap.get(config.getPhaseCode());
                if (record != null) {
                    item.setSwitchTime(DATE_FORMAT.format(record.getSwitchTime()));
                }
            } else if (config.getPhaseOrder().equals(currentRecord.getPhaseOrder())) {
                item.setStatus("ACTIVE");
                item.setSwitchTime(DATE_FORMAT.format(currentRecord.getSwitchTime()));
            } else {
                item.setStatus("PENDING");
            }

            phaseList.add(item);
        }
        statusVO.setPhaseList(phaseList);

        return statusVO;
    }

    @Override
    public PhaseStatusVO initPhase(InitPhaseDTO initDTO) {
        // 检查是否已初始化
        SystemPhaseRecord existingRecord = getCurrentRecord();
        if (existingRecord != null) {
            throw new BusinessException("系统阶段已初始化，当前阶段：" + getPhaseNameByCode(existingRecord.getPhaseCode()));
        }

        // 获取当前操作人信息
        String operatorId = SecurityUtil.getCurrentUserId();
        String operatorName = SecurityUtil.getCurrentUsername();
        if (operatorId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }

        // 获取操作人真实姓名
        String realName = getRealName();

        // 创建初始阶段记录（课题申报阶段）
        SystemPhaseRecord record = new SystemPhaseRecord();
        record.setCohort(initDTO.getCohort());
        record.setPhaseCode(SystemPhase.TOPIC_DECLARATION.name());
        record.setPhaseOrder(SystemPhase.TOPIC_DECLARATION.getOrder());
        record.setPreviousPhaseCode(null);
        record.setSwitchTime(new Date());
        record.setOperatorId(operatorId);
        record.setOperatorName(realName);
        record.setSwitchReason(initDTO.getReason() != null ? initDTO.getReason() : "系统阶段初始化");
        record.setIsCurrent(1);

        phaseRecordMapper.insert(record);

        // 更新Redis缓存
        updatePhaseCache(SystemPhase.TOPIC_DECLARATION.name(), null);

        log.info("系统阶段初始化完成，操作人：{}", realName);

        return getCurrentPhaseStatus();
    }

    @Override
    public PhaseStatusVO switchPhase(SwitchPhaseDTO switchDTO) {
        // 校验目标阶段代码是否有效
        SystemPhase targetPhase;
        try {
            targetPhase = SystemPhase.fromCode(switchDTO.getTargetPhaseCode());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的目标阶段代码: " + switchDTO.getTargetPhaseCode());
        }

        // 获取当前阶段记录
        SystemPhaseRecord currentRecord = getCurrentRecord();
        if (currentRecord == null) {
            throw new BusinessException("系统阶段尚未初始化，请先执行阶段初始化");
        }

        // 获取当前阶段枚举
        SystemPhase currentPhase = SystemPhase.fromCode(currentRecord.getPhaseCode());

        // 校验是否为最后阶段
        if (currentPhase.isLastPhase()) {
            throw new BusinessException("当前已是最后阶段【" + currentPhase.getDescription() + "】，无法继续切换");
        }

        // 严格校验：只能切换到下一阶段（不可回滚、不可跳跃）
        if (!currentPhase.isNextPhase(targetPhase)) {
            SystemPhase expectedNext = currentPhase.getNextPhase();
            String expectedDesc = expectedNext != null ? expectedNext.getDescription() : "无";
            throw new BusinessException(
                    "阶段切换失败：只能切换到下一阶段（当前：" + currentPhase.getDescription()
                            + "，目标必须为：" + expectedDesc + "）"
            );
        }

        // 获取当前操作人信息
        String operatorId = SecurityUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        String realName = getRealName();

        // 将当前记录的 is_current 置为0
        LambdaUpdateWrapper<SystemPhaseRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemPhaseRecord::getIsCurrent, 1)
                .set(SystemPhaseRecord::getIsCurrent, 0);
        phaseRecordMapper.update(null, updateWrapper);

        // 创建新的阶段记录
        SystemPhaseRecord newRecord = new SystemPhaseRecord();
        newRecord.setCohort(currentRecord.getCohort());
        newRecord.setPhaseCode(targetPhase.name());
        newRecord.setPhaseOrder(targetPhase.getOrder());
        newRecord.setPreviousPhaseCode(currentPhase.name());
        newRecord.setSwitchTime(new Date());
        newRecord.setOperatorId(operatorId);
        newRecord.setOperatorName(realName);
        newRecord.setSwitchReason(switchDTO.getSwitchReason());
        newRecord.setIsCurrent(1);

        phaseRecordMapper.insert(newRecord);

        // 更新Redis缓存
        updatePhaseCache(targetPhase.name(), null);

        log.info("系统阶段切换完成：{} → {}，操作人：{}，原因：{}",
                currentPhase.getDescription(), targetPhase.getDescription(),
                realName, switchDTO.getSwitchReason());

        return getCurrentPhaseStatus();
    }

    @Override
    public List<PhaseRecordVO> getPhaseRecords() {
        List<SystemPhaseRecord> records = phaseRecordMapper.selectList(
                new LambdaQueryWrapper<SystemPhaseRecord>()
                        .orderByDesc(SystemPhaseRecord::getSwitchTime)
        );

        return records.stream().map(this::buildPhaseRecordVO).collect(Collectors.toList());
    }

    @Override
    public boolean isPhaseActive(String phaseCode) {
        String currentPhaseCode = getCurrentPhaseCode();
        return phaseCode != null && phaseCode.equals(currentPhaseCode);
    }

    @Override
    public String getCurrentPhaseCode() {
        // 优先从Redis缓存读取
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_CURRENT_PHASE);
        if (cached != null) {
            return cached.toString();
        }

        // 缓存未命中，查库
        SystemPhaseRecord currentRecord = getCurrentRecord();
        if (currentRecord != null) {
            // 回填缓存
            updatePhaseCache(currentRecord.getPhaseCode(), currentRecord.getCohort());
            return currentRecord.getPhaseCode();
        }

        return null;
    }

    // ==================== 私有方法 ====================

    /**
     * 查询当前生效的阶段记录（is_current = 1）
     *
     * @return 当前阶段记录，未初始化时返回null
     */
    private SystemPhaseRecord getCurrentRecord() {
        return phaseRecordMapper.selectOne(
                new LambdaQueryWrapper<SystemPhaseRecord>()
                        .eq(SystemPhaseRecord::getIsCurrent, 1)
        );
    }

    /**
     * 根据阶段代码获取阶段中文名
     *
     * @param phaseCode 阶段代码
     * @return 阶段中文名
     */
    private String getPhaseNameByCode(String phaseCode) {
        try {
            return SystemPhase.fromCode(phaseCode).getDescription();
        } catch (IllegalArgumentException e) {
            return phaseCode;
        }
    }

    /**
     * 计算进度百分比
     * 公式：(当前阶段序号 - 1 + 0.5) / 总阶段数 × 100
     *
     * @param currentOrder 当前阶段序号
     * @return 进度百分比（0-100）
     */
    private int calculateProgress(int currentOrder) {
        double progress = (currentOrder - 1 + 0.5) / TOTAL_PHASES * 100;
        return (int) Math.round(progress);
    }

    /**
     * 更新Redis阶段缓存
     *
     * @param phaseCode 当前阶段代码
     * @param cohort  毕业届别
     */
    private void updatePhaseCache(String phaseCode, String cohort) {
        redisTemplate.opsForValue().set(CACHE_KEY_CURRENT_PHASE, phaseCode);
    }

    /**
     * 构建阶段切换记录VO
     *
     * @param record 阶段切换记录实体
     * @return 阶段切换记录VO
     */
    private PhaseRecordVO buildPhaseRecordVO(SystemPhaseRecord record) {
        PhaseRecordVO vo = new PhaseRecordVO();
        vo.setRecordId(record.getRecordId());
        vo.setCohort(record.getCohort());
        vo.setPhaseCode(record.getPhaseCode());
        vo.setPhaseName(getPhaseNameByCode(record.getPhaseCode()));
        vo.setPhaseOrder(record.getPhaseOrder());
        vo.setPreviousPhaseCode(record.getPreviousPhaseCode());
        vo.setPreviousPhaseName(
                record.getPreviousPhaseCode() != null
                        ? getPhaseNameByCode(record.getPreviousPhaseCode())
                        : null
        );
        vo.setSwitchTime(DATE_FORMAT.format(record.getSwitchTime()));
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setSwitchReason(record.getSwitchReason());
        vo.setIsCurrent(record.getIsCurrent() == 1);
        return vo;
    }

    /**
     * 获取当前用户真实姓名
     * 从SecurityContext中获取，若获取不到则返回用户名
     *
     * @return 操作人真实姓名
     */
    private String getRealName() {
        var userDetails = SecurityUtil.getCurrentUser();
        if (userDetails != null && userDetails.getRealName() != null) {
            return userDetails.getRealName();
        }
        String username = SecurityUtil.getCurrentUsername();
        return username != null ? username : "未知用户";
    }
}
