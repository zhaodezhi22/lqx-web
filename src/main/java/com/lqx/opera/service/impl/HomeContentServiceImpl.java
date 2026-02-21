package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.HomeContent;
import com.lqx.opera.mapper.HomeContentMapper;
import com.lqx.opera.service.HomeContentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeContentServiceImpl extends ServiceImpl<HomeContentMapper, HomeContent> implements HomeContentService {
    
    @Override
    public List<HomeContent> getActiveContentByType(String type) {
        return list(new LambdaQueryWrapper<HomeContent>()
                .eq(HomeContent::getType, type)
                .eq(HomeContent::getStatus, 1)
                .orderByAsc(HomeContent::getSortOrder)
                .orderByDesc(HomeContent::getCreateTime));
    }
}
