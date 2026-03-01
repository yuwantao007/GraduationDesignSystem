package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 专业级联选择器VO
 * 用于课题创建等场景的级联选择组件数据
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业级联选择器数据")
public class MajorCascadeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "值（ID）")
    private String value;

    @Schema(description = "标签（名称）")
    private String label;

    @Schema(description = "子选项列表")
    private List<MajorCascadeVO> children;

    @Schema(description = "是否禁用")
    private Boolean disabled;
}
