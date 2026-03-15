package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课题状态分布 VO（饼图数据项）
 * <p>每条记录代表一个课题审查状态的统计数量</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课题状态分布统计项")
public class TopicStatusDistVO {

    @Schema(description = "审查状态码（0-草稿 1-待预审 2-预审通过 3-预审需修改 4-初审通过 5-初审需修改 6-终审通过 7-终审不通过）")
    private Integer statusCode;

    @Schema(description = "审查状态名称")
    private String statusName;

    @Schema(description = "该状态课题数量")
    private long count;
}
