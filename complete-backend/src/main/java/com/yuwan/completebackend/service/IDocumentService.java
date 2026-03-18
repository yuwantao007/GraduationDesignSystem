package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.dto.UploadDocumentDTO;
import com.yuwan.completebackend.model.vo.DocumentInfoVO;
import com.yuwan.completebackend.model.vo.DocumentPreviewVO;
import com.yuwan.completebackend.model.vo.TopicDocumentOverviewVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文档管理服务接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
public interface IDocumentService {

    /**
     * 学生上传文档
     *
     * @param file 文件
     * @param dto  上传参数
     * @return 文档信息
     */
    DocumentInfoVO uploadDocument(MultipartFile file, UploadDocumentDTO dto);

    /**
     * 教师上传带批注文档
     *
     * @param file      文件
     * @param studentId 学生ID
     * @param remark    备注
     * @return 文档信息
     */
    DocumentInfoVO uploadTeacherAnnotation(MultipartFile file, String studentId, String remark);

    /**
     * 查看某学生的文档列表
     *
     * @param studentId    学生ID
     * @param documentType 文档类型（可选）
     * @param latestOnly   是否只显示最新版本
     * @return 文档列表
     */
    List<DocumentInfoVO> getStudentDocuments(String studentId, Integer documentType, Boolean latestOnly);

    /**
     * 获取我的文档列表（学生视角）
     *
     * @param documentType 文档类型（可选）
     * @return 文档列表
     */
    List<DocumentInfoVO> getMyDocuments(Integer documentType);

    /**
     * 获取文档预览URL
     *
     * @param documentId 文档ID
     * @param ipAddress  访问者IP地址
     * @return 预览信息
     */
    DocumentPreviewVO getPreviewUrl(String documentId, String ipAddress);

    /**
     * 下载文档
     *
     * @param documentId 文档ID
     * @param response   HTTP响应
     * @param ipAddress  访问者IP地址
     */
    void downloadDocument(String documentId, HttpServletResponse response, String ipAddress);

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     */
    void deleteDocument(String documentId);

    /**
     * 设置为最新版本
     *
     * @param documentId 文档ID
     */
    void setAsLatest(String documentId);

    /**
     * 获取课题文档总览
     *
     * @param topicId 课题ID
     * @return 文档总览
     */
    TopicDocumentOverviewVO getTopicDocumentOverview(String topicId);
}
