package com.liuqin.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.SysUser;
import com.liuqin.opera.mapper.SysUserMapper;
import com.liuqin.opera.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}

