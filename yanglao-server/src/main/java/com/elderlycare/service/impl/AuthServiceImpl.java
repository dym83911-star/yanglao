package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.Result;
import com.elderlycare.dto.AuthLoginDTO;
import com.elderlycare.dto.AuthRegisterDTO;
import com.elderlycare.dto.AuthResetPasswordDTO;
import com.elderlycare.dto.AuthSmsLoginDTO;
import com.elderlycare.entity.*;
import com.elderlycare.mapper.*;
import com.elderlycare.security.JwtProperties;
import com.elderlycare.security.JwtUtil;
import com.elderlycare.service.AuthService;
import com.elderlycare.vo.LoginVO;
import com.elderlycare.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysLoginLogMapper sysLoginLogMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<LoginVO> login(AuthLoginDTO dto) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("phone", dto.getPhone());
        SysUser user = sysUserMapper.selectOne(qw);

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed: phone={}", dto.getPhone());
            saveLoginLog(null, dto.getPhone(), false, "账号或密码错误");
            throw new BusinessException(401, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            saveLoginLog(user.getId(), user.getPhone(), false, "账号已禁用");
            throw new BusinessException(403, "账号已禁用");
        }

        return Result.success(doLogin(user));
    }

    /** 校验通过后的统一登录处理：签发 JWT、写 Redis 会话、记录日志 */
    private LoginVO doLogin(SysUser user) {
        List<String> roleCodes = loadRoleCodes(user.getId());
        String token = jwtUtil.generate(user.getId(), user.getPhone(), roleCodes);

        String jti = jwtUtil.parse(token).get("jti", String.class);
        // 单位是毫秒：小时 ×60分 ×60秒 ×1000毫秒
        long expireMs = (long) jwtProperties.getExpireHours() * 60 * 60 * 1000;
        redisTemplate.opsForValue().set("token:" + jti, user.getId(), expireMs, TimeUnit.MILLISECONDS);

        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        saveLoginLog(user.getId(), user.getPhone(), true, "登录成功");
        log.info("User login success: phone={}", user.getPhone());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(buildUserVO(user, roleCodes));
        return vo;
    }

    @Override
    public Result<String> sendSmsCode(String phone) {
        if (phone == null || !phone.trim().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(400, "请输入正确的手机号");
        }
        String p = phone.trim();
        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("phone", p));
        if (user == null) {
            throw new BusinessException(404, "该手机号未注册");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey("sms:limit:" + p))) {
            throw new BusinessException(429, "验证码已发送，请 60 秒后再试");
        }

        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        redisTemplate.opsForValue().set("sms:login:" + p, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("sms:limit:" + p, "1", 60, TimeUnit.SECONDS);
        log.info("SMS login code generated: phone={}, code={}", p, code);

        // 演示环境没有接入短信网关，直接把验证码返回给前端展示
        return Result.success(code);
    }

    @Override
    public Result<LoginVO> loginBySms(AuthSmsLoginDTO dto) {
        if (dto.getPhone() == null || dto.getCode() == null
                || dto.getPhone().trim().isEmpty() || dto.getCode().trim().isEmpty()) {
            throw new BusinessException(400, "请输入手机号和验证码");
        }
        String p = dto.getPhone().trim();
        Object cached = redisTemplate.opsForValue().get("sms:login:" + p);
        if (cached == null) {
            throw new BusinessException(400, "验证码已失效，请重新获取");
        }
        if (!cached.toString().equals(dto.getCode().trim())) {
            throw new BusinessException(400, "验证码不正确");
        }
        redisTemplate.delete("sms:login:" + p);

        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("phone", p));
        if (user == null) {
            throw new BusinessException(404, "该手机号未注册");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            saveLoginLog(user.getId(), p, false, "账号已禁用");
            throw new BusinessException(403, "账号已禁用");
        }
        return Result.success(doLogin(user));
    }

    @Override
    public Result<Void> register(AuthRegisterDTO dto) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("phone", dto.getPhone());
        Long count = sysUserMapper.selectCount(qw);
        if (count != null && count > 0) {
            throw new BusinessException(400, "手机号已注册");
        }

        SysUser user = new SysUser();
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysRole userRole = sysRoleMapper.selectOne(new QueryWrapper<SysRole>().eq("code", "USER"));
        if (userRole == null) {
            userRole = sysRoleMapper.selectOne(new QueryWrapper<SysRole>().eq("code", "ROLE_USER"));
        }
        if (userRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(userRole.getId());
            sysUserRoleMapper.insert(ur);
        }
        return Result.success();
    }

    @Override
    public Result<Void> resetPassword(AuthResetPasswordDTO dto) {
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new BusinessException(400, "请输入手机号");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().trim().length() < 6) {
            throw new BusinessException(400, "新密码至少6位");
        }

        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("phone", dto.getPhone().trim()));
        if (user == null) {
            throw new BusinessException(404, "该手机号未注册");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        sysUserMapper.updateById(user);
        log.info("Password reset for phone={}", dto.getPhone());
        return Result.success();
    }

    @Override
    public Result<Void> logout(Long userId) {
        // 清除 Redis 中该用户的所有 token key（简单实现：按 userId 遍历删除太昂贵，实际生产应在登录时存映射）
        return Result.success();
    }

    @Override
    public Result<UserVO> getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        List<String> roleCodes = loadRoleCodes(userId);
        return Result.success(buildUserVO(user, roleCodes));
    }

    private List<String> loadRoleCodes(Long userId) {
        List<SysUserRole> urs = sysUserRoleMapper.selectList(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        List<String> codes = new ArrayList<>();
        for (SysUserRole ur : urs) {
            SysRole r = sysRoleMapper.selectById(ur.getRoleId());
            if (r != null && r.getCode() != null) {
                codes.add("ROLE_" + r.getCode());
            }
        }
        return codes;
    }

    private UserVO buildUserVO(SysUser user, List<String> roleCodes) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setRoles(roleCodes);
        return vo;
    }

    private void saveLoginLog(Long userId, String phone, boolean success, String message) {
        try {
            SysLoginLog logEntity = new SysLoginLog();
            logEntity.setUserId(userId);
            logEntity.setPhone(phone);
            logEntity.setStatus(success ? 1 : 0);
            logEntity.setMessage(message);
            logEntity.setLoginTime(LocalDateTime.now());
            sysLoginLogMapper.insert(logEntity);
        } catch (Exception e) {
            AuthServiceImpl.log.warn("saveLoginLog failed", e);
        }
    }
}
