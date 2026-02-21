package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.entity.UserAddress;
import com.lqx.opera.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    private Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) return null;
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else {
            return Long.parseLong(userIdObj.toString());
        }
    }

    @GetMapping("/list")
    public Result<List<UserAddress>> list(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.fail("未登录");
        return Result.success(userAddressService.getMyAddresses(userId));
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody UserAddress address, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.fail("未登录");
        return Result.success(userAddressService.addAddress(userId, address));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody UserAddress address, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.fail("未登录");
        try {
            return Result.success(userAddressService.updateAddress(userId, address));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.fail("未登录");
        return Result.success(userAddressService.deleteAddress(userId, id));
    }

    @PutMapping("/default/{id}")
    public Result<Boolean> setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.fail("未登录");
        try {
            return Result.success(userAddressService.setDefault(userId, id));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
