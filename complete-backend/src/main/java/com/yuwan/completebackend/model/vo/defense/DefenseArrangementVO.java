package com.yuwan.completebackend.model.vo.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 答辩安排VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "答辩安排返回对象")
public class DefenseArrangementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "安排ID")
    private String arrangementId;

    @Schema(description = "答辩类型: 1=开题, 2=中期, 3=正式, 4=二次")
    private Integer defenseType;

    @Schema(description = "答辩类型名称")
    private String defenseTypeName;

    @Schema(description = "课题类别")
    private String topicCategory;

    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "答辩时间")
    private Date defenseTime;

    @Schema(description = "答辩地点")
    private String defenseLocation;

    @Schema(description = "答辩小组教师ID列表")
    private List<String> panelTeachers;

    @Schema(description = "答辩小组教师信息列表")
    private List<TeacherInfoVO> panelTeacherInfos;

    @Schema(description = "报告提交截止时间")
    private Date deadline;

    @Schema(description = "毕业届别")
    private String cohort;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "所属企业名称")
    private String enterpriseName;

    @Schema(description = "创建人ID")
    private String creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "状态: 1=启用, 0=禁用")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 教师简要信息VO
     */
    @Data
    @Schema(description = "教师简要信息")
    public static class TeacherInfoVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "教师ID")
        private String userId;

        @Schema(description = "教师姓名")
        private String realName;
    }
}
