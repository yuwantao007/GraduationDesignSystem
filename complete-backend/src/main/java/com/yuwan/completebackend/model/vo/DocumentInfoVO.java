package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 文档信息VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "文档信息返回对象")
public class DocumentInfoVO {

    @Schema(description = "文档ID")
    private String documentId;

    @Schema(description = "所属学生ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "关联课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "文档类型（1-项目代码 2-论文文档 3-开题报告 4-中期检查表 5-教师批注文档）")
    private Integer documentType;

    @Schema(description = "文档类型描述")
    private String documentTypeDesc;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "MinIO存储路径")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件大小描述（如：1.5MB）")
    private String fileSizeDesc;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "上传人ID")
    private String uploaderId;

    @Schema(description = "上传人姓名")
    private String uploaderName;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "是否最新版本（1-是 0-否）")
    private Integer isLatest;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
