package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.EnterpriseMapper;
import com.yuwan.completebackend.mapper.MajorDirectionMapper;
import com.yuwan.completebackend.mapper.MajorMapper;
import com.yuwan.completebackend.mapper.MajorTeacherMapper;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.mapper.UserRoleMapper;
import com.yuwan.completebackend.model.dto.CreateEnterpriseDTO;
import com.yuwan.completebackend.model.dto.UpdateEnterpriseDTO;
import com.yuwan.completebackend.model.entity.Enterprise;
import com.yuwan.completebackend.model.entity.Major;
import com.yuwan.completebackend.model.entity.MajorDirection;
import com.yuwan.completebackend.model.entity.MajorTeacher;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.vo.EnterpriseOverviewVO;
import com.yuwan.completebackend.model.vo.EnterpriseQueryVO;
import com.yuwan.completebackend.model.vo.EnterpriseVO;
import com.yuwan.completebackend.model.vo.DirectionOverviewVO;
import com.yuwan.completebackend.model.vo.MajorOverviewVO;
import com.yuwan.completebackend.model.vo.UserVO;
import com.yuwan.completebackend.service.IEnterpriseService;
import cn.hutool.extra.pinyin.PinyinUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 企业管理服务实现类
 * 提供企业CRUD功能实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EnterpriseServiceImpl implements IEnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final MajorDirectionMapper majorDirectionMapper;
    private final MajorMapper majorMapper;
    private final MajorTeacherMapper majorTeacherMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public EnterpriseVO createEnterprise(CreateEnterpriseDTO createDTO) {
        // 检查企业名称是否已存在
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getEnterpriseName, createDTO.getEnterpriseName());
        if (enterpriseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("企业名称已存在");
        }

        // 检查企业编码是否已存在
        if (StringUtils.hasText(createDTO.getEnterpriseCode())) {
            LambdaQueryWrapper<Enterprise> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(Enterprise::getEnterpriseCode, createDTO.getEnterpriseCode());
            if (enterpriseMapper.selectCount(codeWrapper) > 0) {
                throw new BusinessException("企业编码已存在");
            }
        }

        // 创建企业实体
        Enterprise enterprise = new Enterprise();
        BeanUtils.copyProperties(createDTO, enterprise);
        enterprise.setEnterpriseStatus(1); // 默认正常状态

        // 如果选择了负责人，自动填充联系信息
        if (StringUtils.hasText(createDTO.getLeaderId())) {
            fillContactFromLeader(enterprise, createDTO.getLeaderId());
        }

        // 保存到数据库
        enterpriseMapper.insert(enterprise);
        log.info("创建企业成功，企业ID: {}", enterprise.getEnterpriseId());

        return convertToVO(enterprise);
    }

    @Override
    public EnterpriseVO updateEnterprise(String enterpriseId, UpdateEnterpriseDTO updateDTO) {
        // 查询企业是否存在
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }

        // 检查企业名称是否重复
        if (StringUtils.hasText(updateDTO.getEnterpriseName()) 
                && !updateDTO.getEnterpriseName().equals(enterprise.getEnterpriseName())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getEnterpriseName, updateDTO.getEnterpriseName())
                   .ne(Enterprise::getEnterpriseId, enterpriseId);
            if (enterpriseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("企业名称已存在");
            }
        }

        // 检查企业编码是否重复
        if (StringUtils.hasText(updateDTO.getEnterpriseCode()) 
                && !updateDTO.getEnterpriseCode().equals(enterprise.getEnterpriseCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getEnterpriseCode, updateDTO.getEnterpriseCode())
                   .ne(Enterprise::getEnterpriseId, enterpriseId);
            if (enterpriseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("企业编码已存在");
            }
        }

        // 更新企业信息
        if (StringUtils.hasText(updateDTO.getEnterpriseName())) {
            enterprise.setEnterpriseName(updateDTO.getEnterpriseName());
        }
        if (StringUtils.hasText(updateDTO.getEnterpriseCode())) {
            enterprise.setEnterpriseCode(updateDTO.getEnterpriseCode());
        }
        // 负责人变更时重新填充联系信息
        if (StringUtils.hasText(updateDTO.getLeaderId())) {
            enterprise.setLeaderId(updateDTO.getLeaderId());
            fillContactFromLeader(enterprise, updateDTO.getLeaderId());
        } else if (updateDTO.getLeaderId() != null && updateDTO.getLeaderId().isEmpty()) {
            // 显式清空负责人
            enterprise.setLeaderId(null);
        }
        if (StringUtils.hasText(updateDTO.getContactPerson())) {
            enterprise.setContactPerson(updateDTO.getContactPerson());
        }
        if (StringUtils.hasText(updateDTO.getContactPhone())) {
            enterprise.setContactPhone(updateDTO.getContactPhone());
        }
        if (StringUtils.hasText(updateDTO.getContactEmail())) {
            enterprise.setContactEmail(updateDTO.getContactEmail());
        }
        if (StringUtils.hasText(updateDTO.getAddress())) {
            enterprise.setAddress(updateDTO.getAddress());
        }
        if (StringUtils.hasText(updateDTO.getDescription())) {
            enterprise.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getEnterpriseStatus() != null) {
            enterprise.setEnterpriseStatus(updateDTO.getEnterpriseStatus());
        }

        enterpriseMapper.updateById(enterprise);
        log.info("更新企业成功，企业ID: {}", enterpriseId);

        return convertToVO(enterprise);
    }

    @Override
    public EnterpriseVO getEnterpriseDetail(String enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }
        return convertToVO(enterprise);
    }

    @Override
    public PageResult<EnterpriseVO> getEnterpriseList(EnterpriseQueryVO queryVO) {
        // 构建查询条件
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryVO.getEnterpriseName())) {
            wrapper.like(Enterprise::getEnterpriseName, queryVO.getEnterpriseName());
        }
        if (StringUtils.hasText(queryVO.getEnterpriseCode())) {
            wrapper.eq(Enterprise::getEnterpriseCode, queryVO.getEnterpriseCode());
        }
        if (queryVO.getEnterpriseStatus() != null) {
            wrapper.eq(Enterprise::getEnterpriseStatus, queryVO.getEnterpriseStatus());
        }
        wrapper.orderByDesc(Enterprise::getCreateTime);

        // 分页查询
        Page<Enterprise> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<Enterprise> result = enterpriseMapper.selectPage(page, wrapper);

        // 转换结果
        List<EnterpriseVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), queryVO.getPageNum().longValue(), queryVO.getPageSize().longValue());
    }

    @Override
    public List<EnterpriseVO> getAllEnterprises() {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getEnterpriseStatus, 1)
               .orderByAsc(Enterprise::getEnterpriseName);
        
        List<Enterprise> enterprises = enterpriseMapper.selectList(wrapper);
        return enterprises.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEnterprise(String enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }
        
        // TODO: 检查是否有关联的课题或用户

        enterpriseMapper.deleteById(enterpriseId);
        log.info("删除企业成功，企业ID: {}", enterpriseId);
    }

    @Override
    public void updateEnterpriseStatus(String enterpriseId, Integer status) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }

        enterprise.setEnterpriseStatus(status);
        enterpriseMapper.updateById(enterprise);
        log.info("更新企业状态成功，企业ID: {}, 状态: {}", enterpriseId, status);
    }

    @Override
    public PageResult<EnterpriseOverviewVO> getEnterpriseOverview(EnterpriseQueryVO queryVO) {
        // 构建查询条件
        LambdaQueryWrapper<Enterprise> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（企业名称或企业代码）
        if (StringUtils.hasText(queryVO.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Enterprise::getEnterpriseName, queryVO.getKeyword())
                    .or()
                    .like(Enterprise::getEnterpriseCode, queryVO.getKeyword())
            );
        }
        
        // 状态筛选
        if (queryVO.getStatus() != null) {
            queryWrapper.eq(Enterprise::getEnterpriseStatus, queryVO.getStatus());
        }
        
        // 排序：按创建时间倒序
        queryWrapper.orderByDesc(Enterprise::getCreateTime);

        // 分页查询
        Page<Enterprise> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<Enterprise> enterprisePage = enterpriseMapper.selectPage(page, queryWrapper);

        // 转换为概览VO并填充统计数据
        List<EnterpriseOverviewVO> overviewList = new ArrayList<>();
        for (Enterprise enterprise : enterprisePage.getRecords()) {
            EnterpriseOverviewVO overviewVO = convertToOverviewVO(enterprise);
            
            // 查询该企业下所有专业方向
            LambdaQueryWrapper<MajorDirection> directionWrapper = new LambdaQueryWrapper<>();
            directionWrapper.eq(MajorDirection::getEnterpriseId, enterprise.getEnterpriseId());
            directionWrapper.orderByAsc(MajorDirection::getSortOrder);
            List<MajorDirection> directions = majorDirectionMapper.selectList(directionWrapper);
            
            // 统计专业方向数量
            overviewVO.setDirectionCount(directions.size());
            
            // 转换为方向概览VO并查询专业详情
            List<DirectionOverviewVO> directionVOs = new ArrayList<>();
            int totalTeacherCount = 0;
            int totalStudentCount = 0;
            int totalMajorCount = 0;
            
            for (MajorDirection direction : directions) {
                DirectionOverviewVO directionVO = new DirectionOverviewVO();
                directionVO.setDirectionId(direction.getDirectionId());
                directionVO.setDirectionName(direction.getDirectionName());
                directionVO.setDirectionCode(direction.getDirectionCode());
                directionVO.setLeaderName(direction.getLeaderName());
                
                // 查询该方向下的所有专业
                LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
                majorWrapper.eq(Major::getDirectionId, direction.getDirectionId());
                majorWrapper.orderByAsc(Major::getSortOrder);
                List<Major> majors = majorMapper.selectList(majorWrapper);
                
                // 转换为专业概览VO，并填充每个专业关联的教师姓名
                Set<String> directionTeacherIds = new HashSet<>();
                List<String> majorIds = majors.stream()
                    .map(Major::getMajorId)
                    .collect(Collectors.toList());
                List<MajorOverviewVO> majorVOs = new ArrayList<>();
                for (Major major : majors) {
                    MajorOverviewVO majorVO = new MajorOverviewVO();
                    majorVO.setMajorId(major.getMajorId());
                    majorVO.setMajorName(major.getMajorName());
                    majorVO.setMajorCode(major.getMajorCode());
                    majorVO.setDegreeType(major.getDegreeType());

                    // 查询该专业关联的老师（via major_teacher）
                    List<MajorTeacher> relations = majorTeacherMapper.selectList(
                            new LambdaQueryWrapper<MajorTeacher>()
                                    .eq(MajorTeacher::getMajorId, major.getMajorId()));
                    if (!relations.isEmpty()) {
                        List<String> teacherUserIds = relations.stream()
                                .map(MajorTeacher::getUserId).collect(Collectors.toList());
                        // 记录到方向层去重集合
                        directionTeacherIds.addAll(teacherUserIds);
                        List<String> teacherNames = userMapper.selectList(
                                new LambdaQueryWrapper<User>()
                                        .in(User::getUserId, teacherUserIds)
                                        .orderByAsc(User::getRealName))
                                .stream().map(User::getRealName).collect(Collectors.toList());
                        majorVO.setTeacherNames(teacherNames);
                    } else {
                        majorVO.setTeacherNames(new ArrayList<>());
                    }
                    majorVOs.add(majorVO);
                }
                directionVO.setMajors(majorVOs);

                // 统计该方向下的教师数量（去重：一个老师可能关联多个专业）
                int teacherCount = directionTeacherIds.size();
                directionVO.setTeacherCount(teacherCount);
                totalTeacherCount += teacherCount;
                
                // 统计该方向下的学生数量：优先按 major_id 聚合，兼容旧数据按 direction_id 聚合
                QueryWrapper<User> studentWrapper = new QueryWrapper<>();
                studentWrapper.inSql("user_id",
                        "SELECT ur.user_id FROM user_role ur " +
                                "INNER JOIN role_info r ON ur.role_id = r.role_id " +
                                "WHERE r.role_code = 'STUDENT' AND r.deleted = 0");
                studentWrapper.and(wrapper -> {
                    if (!majorIds.isEmpty()) {
                        wrapper.in("major_id", majorIds)
                                .or()
                                .eq("direction_id", direction.getDirectionId());
                    } else {
                        wrapper.eq("direction_id", direction.getDirectionId());
                    }
                });
                int studentCount = Math.toIntExact(userMapper.selectCount(studentWrapper));
                directionVO.setStudentCount(studentCount);
                totalStudentCount += studentCount;
                
                totalMajorCount += majors.size();
                directionVOs.add(directionVO);
            }
            
            // 设置统计数据
            overviewVO.setDirections(directionVOs);
            overviewVO.setMajorCount(totalMajorCount);
            overviewVO.setTeacherCount(totalTeacherCount);
            overviewVO.setStudentCount(totalStudentCount);
            
            overviewList.add(overviewVO);
        }

        // 构建分页结果
        PageResult<EnterpriseOverviewVO> result = new PageResult<>();
        result.setRecords(overviewList);
        result.setTotal(enterprisePage.getTotal());
        result.setCurrent(queryVO.getPageNum().longValue());
        result.setSize(queryVO.getPageSize().longValue());

        log.info("查询企业概览成功，共{}条记录", result.getTotal());
        return result;
    }

    /**
     * 将企业实体转换为VO
     *
     * @param enterprise 企业实体
     * @return 企业VO
     */
    private EnterpriseVO convertToVO(Enterprise enterprise) {
        EnterpriseVO vo = new EnterpriseVO();
        BeanUtils.copyProperties(enterprise, vo);
        
        // 状态描述
        vo.setEnterpriseStatusDesc(enterprise.getEnterpriseStatus() == 1 ? "正常" : "禁用");
        
        // 格式化时间
        if (enterprise.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(enterprise.getCreateTime()));
        }
        if (enterprise.getUpdateTime() != null) {
            vo.setUpdateTime(DATE_FORMAT.format(enterprise.getUpdateTime()));
        }

        // 填充负责人信息
        if (StringUtils.hasText(enterprise.getLeaderId())) {
            User leader = userMapper.selectById(enterprise.getLeaderId());
            if (leader != null) {
                vo.setLeaderId(leader.getUserId());
                vo.setLeaderName(leader.getRealName());
                vo.setLeaderPhone(leader.getUserPhone());
                vo.setLeaderEmail(leader.getUserEmail());
                // 以负责人为联系人（若企业侧未单独设置）
                if (!StringUtils.hasText(enterprise.getContactPerson())) {
                    vo.setContactPerson(leader.getRealName());
                }
            }
        }
        
        return vo;
    }

    /**
     * 根据负责人ID将联系信息填充到企业实体
     */
    private void fillContactFromLeader(Enterprise enterprise, String leaderId) {
        User leader = userMapper.selectById(leaderId);
        if (leader == null) {
            throw new BusinessException("所选负责人不存在");
        }
        enterprise.setLeaderId(leaderId);
        enterprise.setContactPerson(leader.getRealName());
        // 若企业未单独指定电话/邮箱，使用负责人信息
        if (!StringUtils.hasText(enterprise.getContactPhone())) {
            enterprise.setContactPhone(leader.getUserPhone());
        }
        if (!StringUtils.hasText(enterprise.getContactEmail())) {
            enterprise.setContactEmail(leader.getUserEmail());
        }
    }

    @Override
    public String generateEnterpriseCode(String name) {
        if (!StringUtils.hasText(name)) {
            return "ENT" + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        }

        // 去除常见中文企业后缀
        String cleaned = name.replaceAll("有限公司|股份公司|集团|科技|网络|实业|技术|软件|智能|信息|数字|创新", "").trim();
        if (!StringUtils.hasText(cleaned)) {
            cleaned = name;
        }

        // 使用 Hutool PinyinUtil 取每个字符首字母（汉字取拼音首字母，ASCII字母保留）
        String prefix;
        try {
            prefix = PinyinUtil.getFirstLetter(cleaned, "").toUpperCase().replaceAll("[^A-Z]", "");
        } catch (Exception e) {
            // Fallback：直接取ASCII大写字母
            prefix = cleaned.toUpperCase().replaceAll("[^A-Z]", "");
        }

        if (!StringUtils.hasText(prefix)) {
            prefix = "ENT";
        }
        // 限制前缀长度为 2~6 位
        if (prefix.length() > 6) {
            prefix = prefix.substring(0, 6);
        }

        // 生成唯一编码（最多重试5次）
        String finalPrefix = prefix;
        for (int i = 0; i < 5; i++) {
            String code = finalPrefix + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
            LambdaQueryWrapper<Enterprise> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(Enterprise::getEnterpriseCode, code);
            if (enterpriseMapper.selectCount(codeWrapper) == 0) {
                return code;
            }
        }
        // 仍冲突时用更大随机数
        return finalPrefix + System.currentTimeMillis() % 100000;
    }

    @Override
    public List<UserVO> searchEnterpriseLeaders(String keyword) {
        // 找到 ENTERPRISE_LEADER 角色
        Role leaderRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ENTERPRISE_LEADER"));
        if (leaderRole == null) {
            return new ArrayList<>();
        }

        // 找到持有该角色的所有用户ID
        List<String> userIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<com.yuwan.completebackend.model.entity.UserRole>()
                                .eq(com.yuwan.completebackend.model.entity.UserRole::getRoleId, leaderRole.getRoleId()))
                .stream()
                .map(com.yuwan.completebackend.model.entity.UserRole::getUserId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 按关键词过滤（姓名或账号模糊匹配），只返回正常状态用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                .in(User::getUserId, userIds)
                .eq(User::getUserStatus, 1)
                .orderByAsc(User::getRealName);
        if (StringUtils.hasText(keyword)) {
            userWrapper.and(w -> w.like(User::getRealName, keyword).or().like(User::getUsername, keyword));
        }

        return userMapper.selectList(userWrapper).stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setUserId(user.getUserId());
            vo.setRealName(user.getRealName());
            vo.setUsername(user.getUsername());
            vo.setUserPhone(user.getUserPhone());
            vo.setUserEmail(user.getUserEmail());
            vo.setEmployeeNo(user.getEmployeeNo());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 将企业实体转换为概览VO
     *
     * @param enterprise 企业实体
     * @return 企业概览VO
     */
    private EnterpriseOverviewVO convertToOverviewVO(Enterprise enterprise) {
        EnterpriseOverviewVO vo = new EnterpriseOverviewVO();
        BeanUtils.copyProperties(enterprise, vo);
        
        // 状态描述
        vo.setStatusDesc(enterprise.getEnterpriseStatus() == 1 ? "正常" : "禁用");
        vo.setStatus(enterprise.getEnterpriseStatus());
        
        // 格式化创建时间
        if (enterprise.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(enterprise.getCreateTime()));
        }
        
        return vo;
    }
}
