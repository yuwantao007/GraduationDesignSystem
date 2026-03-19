package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.DocumentAccessLogMapper;
import com.yuwan.completebackend.mapper.DocumentInfoMapper;
import com.yuwan.completebackend.model.dto.UploadDocumentDTO;
import com.yuwan.completebackend.model.entity.DocumentAccessLog;
import com.yuwan.completebackend.model.entity.DocumentInfo;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.enums.AccessType;
import com.yuwan.completebackend.model.enums.DocumentType;
import com.yuwan.completebackend.model.vo.DocumentInfoVO;
import com.yuwan.completebackend.model.vo.DocumentPreviewVO;
import com.yuwan.completebackend.model.vo.TopicDocumentOverviewVO;
import com.yuwan.completebackend.service.IDocumentService;
import com.yuwan.completebackend.service.minio.MinioService;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.security.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档管理服务实现类
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DocumentServiceImpl implements IDocumentService {

    @Autowired
    private DocumentInfoMapper documentInfoMapper;

    @Autowired
    private DocumentAccessLogMapper documentAccessLogMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private TopicSelectionMapper topicSelectionMapper;

    @Autowired
    private TopicMapper topicMapper;

    // 支持在线预览的文件后缀
    private static final Set<String> PREVIEW_SUPPORTED_SUFFIXES = Set.of(
            "pdf", "jpg", "jpeg", "png", "gif", "bmp", "webp", "txt"
    );

    @Override
    public DocumentInfoVO uploadDocument(MultipartFile file, UploadDocumentDTO dto) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 验证文档类型
        if (!DocumentType.isValid(dto.getDocumentType())) {
            throw new BusinessException("无效的文档类型");
        }

        // 教师批注文档需要走教师上传接口
        if (dto.getDocumentType() == DocumentType.TEACHER_ANNOTATION.getCode()) {
            throw new BusinessException("教师批注文档请使用教师上传接口");
        }

        // 获取学生的课题选择信息
        TopicSelection selection = getStudentTopicSelection(currentUserId);
        if (selection == null) {
            throw new BusinessException("您尚未选择课题，无法上传文档");
        }

        return saveDocument(file, currentUserId, selection.getTopicId(), dto.getDocumentType(),
                currentUserId, dto.getRemark());
    }

    @Override
    public DocumentInfoVO uploadTeacherAnnotation(MultipartFile file, String studentId, String remark) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        if (!StringUtils.hasText(studentId)) {
            throw new BusinessException("学生ID不能为空");
        }

        // 获取学生的课题选择信息
        TopicSelection selection = getStudentTopicSelection(studentId);
        if (selection == null) {
            throw new BusinessException("该学生尚未选择课题");
        }

        // todo: 验证当前教师是否有权限给该学生上传批注

        return saveDocument(file, studentId, selection.getTopicId(),
                DocumentType.TEACHER_ANNOTATION.getCode(), currentUserId, remark);
    }

    @Override
    public List<DocumentInfoVO> getStudentDocuments(String studentId, Integer documentType, Boolean latestOnly) {
        List<DocumentInfoVO> documents = documentInfoMapper.selectByStudentId(studentId, documentType, latestOnly);
        // 填充文档类型描述和文件大小描述
        documents.forEach(this::fillDocumentDesc);
        return documents;
    }

    @Override
    public List<DocumentInfoVO> getMyDocuments(Integer documentType) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        return getStudentDocuments(currentUserId, documentType, null);
    }

    @Override
    public DocumentPreviewVO getPreviewUrl(String documentId, String ipAddress) {
        DocumentInfo document = getDocumentById(documentId);

        // 记录访问日志
        logAccess(documentId, AccessType.PREVIEW.getCode(), ipAddress);

        // 获取预签名URL
        String previewUrl = minioService.getPresignedUrl(document.getFilePath());

        DocumentPreviewVO vo = new DocumentPreviewVO();
        vo.setDocumentId(documentId);
        vo.setFileName(document.getFileName());
        vo.setFileSuffix(document.getFileSuffix());
        vo.setPreviewUrl(previewUrl);
        vo.setExpiresIn(15 * 60); // 15分钟
        vo.setSupportPreview(PREVIEW_SUPPORTED_SUFFIXES.contains(
                document.getFileSuffix().toLowerCase()));

        return vo;
    }

    @Override
    public void downloadDocument(String documentId, HttpServletResponse response, String ipAddress) {
        DocumentInfo document = getDocumentById(documentId);

        // 记录访问日志
        logAccess(documentId, AccessType.DOWNLOAD.getCode(), ipAddress);

        try (InputStream inputStream = minioService.downloadFile(document.getFilePath());
             OutputStream outputStream = response.getOutputStream()) {

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8));
            response.setContentLengthLong(document.getFileSize());

            // 复制文件流
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

        } catch (Exception e) {
            log.error("文件下载失败: {}", documentId, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        DocumentInfo document = getDocumentById(documentId);

        // 验证是否为上传者本人
        if (!document.getUploaderId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己上传的文档");
        }

        // 最新版本不可删除
        if (document.getIsLatest() == 1) {
            throw new BusinessException("最新版本的文档不可删除");
        }

        // 逻辑删除
        document.setDeleted(1);
        documentInfoMapper.updateById(document);

        // 物理删除MinIO中的文件（可选，根据业务需求决定）
        // minioService.deleteFile(document.getFilePath());

        log.info("文档删除成功: {}, 操作人: {}", documentId, currentUserId);
    }

    @Override
    public void setAsLatest(String documentId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        DocumentInfo document = getDocumentById(documentId);

        // 验证是否为上传者本人
        if (!document.getUploaderId().equals(currentUserId)) {
            throw new BusinessException("只能操作自己上传的文档");
        }

        // 将同类型的其他文档设置为非最新
        documentInfoMapper.updateNotLatest(document.getStudentId(),
                document.getDocumentType(), documentId);

        // 设置当前文档为最新
        document.setIsLatest(1);
        documentInfoMapper.updateById(document);

        log.info("设置最新版本成功: {}, 操作人: {}", documentId, currentUserId);
    }

    @Override
    public TopicDocumentOverviewVO getTopicDocumentOverview(String topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 获取选题学生
        LambdaQueryWrapper<TopicSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicSelection::getTopicId, topicId)
                .eq(TopicSelection::getSelectionStatus, 1); // 已确认状态
        TopicSelection selection = topicSelectionMapper.selectOne(queryWrapper);

        TopicDocumentOverviewVO vo = new TopicDocumentOverviewVO();
        vo.setTopicId(topicId);
        vo.setTopicTitle(topic.getTopicTitle());

        if (selection != null) {
            vo.setStudentId(selection.getStudentId());

            // 获取所有文档
            List<DocumentInfoVO> allDocs = documentInfoMapper.selectByTopicId(topicId, null);
            allDocs.forEach(this::fillDocumentDesc);

            // 按类型分组
            Map<Integer, List<DocumentInfoVO>> groupedDocs = allDocs.stream()
                    .collect(Collectors.groupingBy(DocumentInfoVO::getDocumentType));

            vo.setProjectCodeDocs(groupedDocs.getOrDefault(
                    DocumentType.PROJECT_CODE.getCode(), Collections.emptyList()));
            vo.setThesisDocs(groupedDocs.getOrDefault(
                    DocumentType.THESIS_DOCUMENT.getCode(), Collections.emptyList()));
            vo.setOpeningReportDocs(groupedDocs.getOrDefault(
                    DocumentType.OPENING_REPORT.getCode(), Collections.emptyList()));
            vo.setMidtermCheckDocs(groupedDocs.getOrDefault(
                    DocumentType.MIDTERM_CHECK.getCode(), Collections.emptyList()));
            vo.setTeacherAnnotationDocs(groupedDocs.getOrDefault(
                    DocumentType.TEACHER_ANNOTATION.getCode(), Collections.emptyList()));

            vo.setTotalCount(allDocs.size());

            // 设置学生姓名
            if (!allDocs.isEmpty()) {
                vo.setStudentName(allDocs.get(0).getStudentName());
            }
        } else {
            vo.setProjectCodeDocs(Collections.emptyList());
            vo.setThesisDocs(Collections.emptyList());
            vo.setOpeningReportDocs(Collections.emptyList());
            vo.setMidtermCheckDocs(Collections.emptyList());
            vo.setTeacherAnnotationDocs(Collections.emptyList());
            vo.setTotalCount(0);
        }

        return vo;
    }

    // ==================== 私有方法 ====================

    /**
     * 保存文档
     */
    private DocumentInfoVO saveDocument(MultipartFile file, String studentId, String topicId,
                                        Integer documentType, String uploaderId, String remark) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名不能为空");
        }

        // 获取文件后缀
        String fileSuffix = getFileSuffix(originalFilename);

        // 生成存储路径: documents/{studentId}/{documentType}/{timestamp}_{filename}
        String objectName = String.format("documents/%s/%d/%d_%s",
                studentId, documentType, System.currentTimeMillis(), originalFilename);

        // 上传到MinIO
        minioService.uploadFile(file, objectName);

        // 获取新版本号
        Integer maxVersion = documentInfoMapper.selectMaxVersion(studentId, documentType);
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        // 将同类型的旧文档设置为非最新
        documentInfoMapper.updateNotLatest(studentId, documentType, null);

        // 保存文档记录
        DocumentInfo document = new DocumentInfo();
        document.setStudentId(studentId);
        document.setTopicId(topicId);
        document.setDocumentType(documentType);
        document.setFileName(originalFilename);
        document.setFilePath(objectName);
        document.setFileSize(file.getSize());
        document.setFileSuffix(fileSuffix);
        document.setUploaderId(uploaderId);
        document.setVersion(newVersion);
        document.setIsLatest(1);
        document.setRemark(remark);

        documentInfoMapper.insert(document);

        log.info("文档上传成功: {}, 学生: {}, 类型: {}, 版本: {}",
                document.getDocumentId(), studentId, documentType, newVersion);

        // 返回文档详情
        return documentInfoMapper.selectDocumentDetail(document.getDocumentId());
    }

    /**
     * 获取学生的课题选择
     */
    private TopicSelection getStudentTopicSelection(String studentId) {
        LambdaQueryWrapper<TopicSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicSelection::getStudentId, studentId)
                .eq(TopicSelection::getSelectionStatus, 1); // 已确认状态
        return topicSelectionMapper.selectOne(queryWrapper);
    }

    /**
     * 根据ID获取文档
     */
    private DocumentInfo getDocumentById(String documentId) {
        DocumentInfo document = documentInfoMapper.selectById(documentId);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException("文档不存在或已删除");
        }
        return document;
    }

    /**
     * 获取文件后缀
     */
    private String getFileSuffix(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 填充文档描述信息
     */
    private void fillDocumentDesc(DocumentInfoVO vo) {
        // 填充文档类型描述
        vo.setDocumentTypeDesc(DocumentType.getDescription(vo.getDocumentType()));
        // 填充文件大小描述
        vo.setFileSizeDesc(formatFileSize(vo.getFileSize()));
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    /**
     * 记录访问日志
     */
    private void logAccess(String documentId, Integer accessType, String ipAddress) {
        try {
            DocumentAccessLog log = new DocumentAccessLog();
            log.setDocumentId(documentId);
            log.setAccessorId(SecurityUtil.getCurrentUserId());
            log.setAccessType(accessType);
            log.setAccessTime(new Date());
            log.setIpAddress(ipAddress);
            documentAccessLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            DocumentServiceImpl.log.warn("记录访问日志失败: {}", e.getMessage());
        }
    }
}
