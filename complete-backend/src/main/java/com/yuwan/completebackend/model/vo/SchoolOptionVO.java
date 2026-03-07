package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学校下拉选项VO（精简版）
 * 仅包含 ID 和名称，面向所有登录用户开放
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@Schema(description = "学校下拉选项（精简版）")
public class SchoolOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学校ID")
    private String schoolId;

    @Schema(description = "学校名称")
    private String schoolName;
}
