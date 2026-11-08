package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.dto.AuthLoginDTO;
import com.elderlycare.dto.AuthRegisterDTO;
import com.elderlycare.dto.AuthResetPasswordDTO;
import com.elderlycare.dto.AuthSmsLoginDTO;
import com.elderlycare.vo.LoginVO;
import com.elderlycare.vo.UserVO;

public interface AuthService {
    Result<LoginVO> login(AuthLoginDTO dto);

    Result<Void> register(AuthRegisterDTO dto);

    /** 忘记密码：通过手机号直接重置 */
    Result<Void> resetPassword(AuthResetPasswordDTO dto);

    /** 发送登录验证码（演示环境直接返回验证码） */
    Result<String> sendSmsCode(String phone);

    /** 验证码登录 */
    Result<LoginVO> loginBySms(AuthSmsLoginDTO dto);

    Result<Void> logout(Long userId);

    Result<UserVO> getCurrentUser(Long userId);
}
