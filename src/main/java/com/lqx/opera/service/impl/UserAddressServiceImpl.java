package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.UserAddress;
import com.lqx.opera.mapper.UserAddressMapper;
import com.lqx.opera.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddress> getMyAddresses(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault) // Default first
                .orderByDesc(UserAddress::getUpdateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAddress(Long userId, UserAddress address) {
        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        
        // If it's the first address, make it default automatically
        long count = count(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
        if (count == 0) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(address.getIsDefault())) {
            // If new one is default, clear others
            clearDefault(userId);
        } else {
            address.setIsDefault(false);
        }
        
        return save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long userId, UserAddress address) {
        UserAddress old = getById(address.getId());
        if (old == null || !old.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权操作");
        }
        
        address.setUserId(userId);
        address.setUpdateTime(LocalDateTime.now());
        
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefault(userId);
        }
        
        return updateById(address);
    }

    @Override
    public boolean deleteAddress(Long userId, Long addressId) {
        return remove(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getId, addressId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        
        clearDefault(userId);
        
        address.setIsDefault(true);
        address.setUpdateTime(LocalDateTime.now());
        return updateById(address);
    }

    private void clearDefault(Long userId) {
        update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, false));
    }
}
