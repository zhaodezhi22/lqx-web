package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.mapper.InheritorProfileMapper;
import com.lqx.opera.service.InheritorProfileService;
import org.springframework.stereotype.Service;

@Service
public class InheritorProfileServiceImpl extends ServiceImpl<InheritorProfileMapper, InheritorProfile> implements InheritorProfileService {
}

