package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 更新答辩安排DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "更新答辩安排请求对象")
public class UpdateArrangementDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "安排ID不能为空")
    @Schema(description = "安排ID")
    private String arrangementId;

    @NotNull(message = "答辩类型不能为空")
    @Schema(description = "答辩类型: 1=开题, 2=中期, 3=正式, 4=二次", example = "1")
    private Integer defenseType;

    @NotBlank(message = "课题类别不能为空")
    @Schema(description = "课题类别", example = "高职升本")
    private String topicCategory;

    @NotNull(message = "答辩时间不能为空")
    @Schema(description = "答辩时间")
    private Date defenseTime;

    @NotBlank(message = "答辩地点不能为空")
    @Schema(description = "答辩地点", example = "教学楼A栋301")
    private String defenseLocation;

    @NotEmpty(message = "答辩小组教师不能为空")
    @Schema(description = "答辩小组教师ID列表")
    private List<String> panelTeachers;

    @Schema(description = "报告提交截止时间")
    private Date deadline;

    @NotBlank(message = "毕业届别不能为空")
    @Schema(description = "毕业届别", example = "2026届")
    private String cohort;

    @Schema(description = "备注说明")
    private String remark;
}
