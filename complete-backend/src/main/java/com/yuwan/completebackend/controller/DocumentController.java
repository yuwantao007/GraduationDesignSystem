package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.UploadDocumentDTO;
import com.yuwan.completebackend.model.vo.DocumentInfoVO;
import com.yuwan.completebackend.model.vo.DocumentPreviewVO;
import com.yuwan.completebackend.model.vo.TopicDocumentOverviewVO;
import com.yuwan.completebackend.service.IDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档管理控制器
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Slf4j
@RestController
@RequestMapping("/document")
@Tag(name = "文档管理", description = "文档上传、下载、预览等接口")
public class DocumentController {

    @Autowired
    private IDocumentService documentService;

    /**
     * 上传文档（学生/教师）
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "学生上传项目代码、论文文档、开题报告、中期检查表等")
    @PreAuthorize("hasAuthority('document:upload')")
    public Result<DocumentInfoVO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档类型") @RequestParam("documentType") Integer documentType,
            @Parameter(description = "备注") @RequestParam(value = "remark", required = false) String remark) {

        UploadDocumentDTO dto = new UploadDocumentDTO();
        dto.setDocumentType(documentType);
        dto.setRemark(remark);

        DocumentInfoVO result = documentService.uploadDocument(file, dto);
        return Result.success(result);
    }

    /**
     * 教师上传带批注文档
     */
    @PostMapping("/upload/teacher")
    @Operation(summary = "教师上传批注文档", description = "教师上传带批注的文档关联到指定学生")
    @PreAuthorize("hasAuthority('document:upload:teacher')")
    public Result<DocumentInfoVO> uploadTeacherAnnotation(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "学生ID", required = true) @RequestParam("studentId") String studentId,
            @Parameter(description = "备注") @RequestParam(value = "remark", required = false) String remark) {

        DocumentInfoVO result = documentService.uploadTeacherAnnotation(file, studentId, remark);
        return Result.success(result);
    }

    /**
     * 查看某学生的文档列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "查看学生文档列表", description = "教师/负责人查看某学生的文档列表")
    @PreAuthorize("hasAuthority('document:view')")
    public Result<List<DocumentInfoVO>> getStudentDocuments(
            @Parameter(description = "学生ID") @PathVariable String studentId,
            @Parameter(description = "文档类型") @RequestParam(required = false) Integer documentType,
            @Parameter(description = "是否只显示最新版本") @RequestParam(required = false) Boolean latestOnly) {

        List<DocumentInfoVO> documents = documentService.getStudentDocuments(studentId, documentType, latestOnly);
        return Result.success(documents);
    }

    /**
     * 我的文档列表（学生视角）
     */
    @GetMapping("/my")
    @Operation(summary = "我的文档列表", description = "学生查看自己的文档列表")
    @PreAuthorize("hasAuthority('document:upload')")
    public Result<List<DocumentInfoVO>> getMyDocuments(
            @Parameter(description = "文档类型") @RequestParam(required = false) Integer documentType) {

        List<DocumentInfoVO> documents = documentService.getMyDocuments(documentType);
        return Result.success(documents);
    }

    /**
     * 获取文档预览URL
     */
    @GetMapping("/{documentId}/preview")
    @Operation(summary = "获取文档预览URL", description = "获取MinIO预签名URL，有效期15分钟")
    @PreAuthorize("hasAuthority('document:view') or hasAuthority('document:upload')")
    public Result<DocumentPreviewVO> getPreviewUrl(
            @Parameter(description = "文档ID") @PathVariable String documentId,
            HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        DocumentPreviewVO result = documentService.getPreviewUrl(documentId, ipAddress);
        return Result.success(result);
    }

    /**
     * 下载文档
     */
    @GetMapping("/{documentId}/download")
    @Operation(summary = "下载文档", description = "下载指定文档")
    @PreAuthorize("hasAuthority('document:download')")
    public void downloadDocument(
            @Parameter(description = "文档ID") @PathVariable String documentId,
            HttpServletRequest request,
            HttpServletResponse response) {

        String ipAddress = getClientIp(request);
        documentService.downloadDocument(documentId, response, ipAddress);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    @Operation(summary = "删除文档", description = "删除自己上传的文档（最新版本不可删除）")
    @PreAuthorize("hasAuthority('document:delete')")
    public Result<Void> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable String documentId) {

        documentService.deleteDocument(documentId);
        return Result.success();
    }

    /**
     * 设置为最新版本
     */
    @PutMapping("/{documentId}/latest")
    @Operation(summary = "设置为最新版本", description = "将指定文档设置为最新版本")
    @PreAuthorize("hasAuthority('document:upload')")
    public Result<Void> setAsLatest(
            @Parameter(description = "文档ID") @PathVariable String documentId) {

        documentService.setAsLatest(documentId);
        return Result.success();
    }

    /**
     * 课题文档总览
     */
    @GetMapping("/topic/{topicId}")
    @Operation(summary = "课题文档总览", description = "查看某课题下各类型文档的最新版本")
    @PreAuthorize("hasAuthority('document:view')")
    public Result<TopicDocumentOverviewVO> getTopicDocumentOverview(
            @Parameter(description = "课题ID") @PathVariable String topicId) {

        TopicDocumentOverviewVO result = documentService.getTopicDocumentOverview(topicId);
        return Result.success(result);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
