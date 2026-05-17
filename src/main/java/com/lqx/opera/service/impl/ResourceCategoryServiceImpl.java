package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.ResourceCategory;
import com.lqx.opera.mapper.ResourceCategoryMapper;
import com.lqx.opera.service.ResourceCategoryService;
import org.springframework.stereotype.Service;

@Service
public class ResourceCategoryServiceImpl extends ServiceImpl<ResourceCategoryMapper, ResourceCategory> implements ResourceCategoryService {
}
