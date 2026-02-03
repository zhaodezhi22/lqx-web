package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {
    List<UserAddress> getMyAddresses(Long userId);
    boolean addAddress(Long userId, UserAddress address);
    boolean updateAddress(Long userId, UserAddress address);
    boolean deleteAddress(Long userId, Long addressId);
    boolean setDefault(Long userId, Long addressId);
}
