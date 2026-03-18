package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 课题文档总览VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "课题文档总览返回对象")
public class TopicDocumentOverviewVO {

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "学生ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "项目代码文档列表")
    private List<DocumentInfoVO> projectCodeDocs;

    @Schema(description = "论文文档列表")
    private List<DocumentInfoVO> thesisDocs;

    @Schema(description = "开题报告列表")
    private List<DocumentInfoVO> openingReportDocs;

    @Schema(description = "中期检查表列表")
    private List<DocumentInfoVO> midtermCheckDocs;

    @Schema(description = "教师批注文档列表")
    private List<DocumentInfoVO> teacherAnnotationDocs;

    @Schema(description = "文档总数")
    private Integer totalCount;
}
