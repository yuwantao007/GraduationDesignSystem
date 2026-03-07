package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 专业-企业老师关联实体类
 * 对应数据库表 major_teacher
 * <p>
 * 维护专业与企业教师的多对多关系，一个专业可以有多名企业老师
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@TableName("major_teacher")
@Schema(description = "专业-企业老师关联实体")
public class MajorTeacher implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "企业老师用户ID")
    private String userId;

    @Schema(description = "创建时间")
    private Date createTime;
}
