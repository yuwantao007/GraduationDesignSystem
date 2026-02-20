package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.LoginDTO;
import com.yuwan.completebackend.model.dto.RegisterDTO;
import com.yuwan.completebackend.model.vo.LoginVO;
import com.yuwan.completebackend.model.vo.UserVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证Controller
 * 提供登录、注册、登出、令牌刷新等认证接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理接口", description = "用户登录、注册、登出、令牌刷新")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名密码登录，返回JWT令牌")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO,
                                 HttpServletRequest request) {
        String loginIp = getClientIp(request);
        LoginVO loginVO = authService.login(loginDTO, loginIp);
        return Result.success("登录成功", loginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public Result<UserVO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        UserVO userVO = authService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "注销当前用户的令牌")
    public Result<Void> logout() {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            authService.logout(userId);
        }
        return Result.success("登出成功", null);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginVO> refreshToken(
            @Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        LoginVO loginVO = authService.refreshToken(refreshToken);
        return Result.success("令牌刷新成功", loginVO);
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
