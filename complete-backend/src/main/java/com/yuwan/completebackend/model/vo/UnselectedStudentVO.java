package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未选报学生VO
 * 用于企业负责人查看尚未选报任何课题的学生列表
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "未选报学生信息响应")
public class UnselectedStudentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学生用户ID")
    private String userId;

    @Schema(description = "学生姓名")
    private String realName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "手机号")
    private String userPhone;

    @Schema(description = "邮箱")
    private String userEmail;

    @Schema(description = "所属部门/企业")
    private String department;
}
