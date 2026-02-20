package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.dto.LoginDTO;
import com.yuwan.completebackend.model.dto.RegisterDTO;
import com.yuwan.completebackend.model.vo.LoginVO;
import com.yuwan.completebackend.model.vo.UserVO;

/**
 * 认证服务接口
 * 提供用户登录、注册、登出、令牌刷新等认证相关功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
public interface IAuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @param loginIp  登录IP
     * @return 登录结果（包含令牌和用户信息）
     */
    LoginVO login(LoginDTO loginDTO, String loginIp);

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 注册成功的用户信息
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    void logout(String userId);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录结果
     */
    LoginVO refreshToken(String refreshToken);
}
