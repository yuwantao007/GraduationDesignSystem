package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课题流程实例映射实体
 * <p>
 * 将业务课题ID与 Flowable 流程实例ID关联，是业务表与 ACT_* 流程表的桥接。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@TableName("topic_process_instance")
@Schema(description = "课题流程实例映射")
public class TopicProcessInstance {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "Flowable 流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义Key")
    private String processDefKey;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班），用于判断审查路径")
    private Integer topicCategory;

    @Schema(description = "流程状态（0-运行中 1-已完成 2-已终止）")
    private Integer processStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
