package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询参数VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@Schema(description = "用户查询参数")
public class UserQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录账号（模糊查询）")
    private String username;

    @Schema(description = "真实姓名（模糊查询）")
    private String realName;

    @Schema(description = "手机号码")
    private String userPhone;

    @Schema(description = "账号状态（0-禁用 1-正常 2-锁定）")
    private Integer userStatus;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "所属院系/企业（模糊查询）")
    private String department;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
