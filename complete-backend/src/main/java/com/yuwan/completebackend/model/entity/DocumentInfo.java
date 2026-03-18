package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 文档信息实体类
 * 对应数据库表 document_info
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@TableName("document_info")
@Schema(description = "文档信息实体")
public class DocumentInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "document_id", type = IdType.ASSIGN_ID)
    @Schema(description = "文档ID")
    private String documentId;

    @Schema(description = "所属学生ID")
    private String studentId;

    @Schema(description = "关联课题ID")
    private String topicId;

    @Schema(description = "文档类型（1-项目代码 2-论文文档 3-开题报告 4-中期检查表 5-教师批注文档）")
    private Integer documentType;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "MinIO存储路径")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "上传人ID")
    private String uploaderId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "是否最新版本（1-是 0-否）")
    private Integer isLatest;

    @Schema(description = "备注")
    private String remark;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}
