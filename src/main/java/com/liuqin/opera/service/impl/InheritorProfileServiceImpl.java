package com.liuqin.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.InheritorProfile;
import com.liuqin.opera.mapper.InheritorProfileMapper;
import com.liuqin.opera.service.InheritorProfileService;
import org.springframework.stereotype.Service;

@Service
public class InheritorProfileServiceImpl extends ServiceImpl<InheritorProfileMapper, InheritorProfile> implements InheritorProfileService {
}

