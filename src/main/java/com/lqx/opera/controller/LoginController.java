package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.LoginRequest;
import com.lqx.opera.common.dto.LoginResponse;
import com.lqx.opera.common.dto.RegisterRequest;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.InheritorProfileMapper;
import com.lqx.opera.service.SysUserService;
import com.lqx.opera.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");

    private final SysUserService sysUserService;
    private final InheritorProfileMapper inheritorProfileMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginController(SysUserService sysUserService,
                           InheritorProfileMapper inheritorProfileMapper,
                           JwtUtils jwtUtils) {
        this.sysUserService = sysUserService;
        this.inheritorProfileMapper = inheritorProfileMapper;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody RegisterRequest req) {
        String username = req.getUsername() == null ? null : req.getUsername().trim();
        if (username == null || username.isEmpty()
                || req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return Result.fail(400, "手机号和密码不能为空");
        }
        if (!MOBILE_PATTERN.matcher(username).matches()) {
            return Result.fail(400, "账号只能使用11位手机号注册");
        }

        Long count = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).count();
        if (count != null && count > 0) {
            return Result.fail(400, "手机号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole() == null ? 0 : req.getRole());
        user.setRealName(req.getRealName());
        user.setPhone(username);
        user.setEmail(req.getEmail());
        user.setStatus(1);
        boolean saved = sysUserService.save(user);
        return saved ? Result.success(true) : Result.fail("注册失败");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, req.getUsername())
                .one();
        if (user == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.fail(HttpStatus.FORBIDDEN.value(), "用户已被禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "密码错误");
        }
        // 传承人审核逻辑暂时移除，允许先登录后补充资料
        /*
        if (user.getRole() != null && user.getRole() == 1) {
            InheritorProfile profile = inheritorProfileMapper.selectOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, user.getUserId()));
            if (profile == null || profile.getVerifyStatus() == null || profile.getVerifyStatus() != 1) {
                return Result.fail(HttpStatus.FORBIDDEN.value(), "传承人尚未审核通过");
            }
        }
        */

        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        // 不返回密码
        user.setPassword(null);
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUser(user);
        return Result.success(resp);
    }
}

