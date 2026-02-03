package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.HomeContent;

import java.util.List;

public interface HomeContentService extends IService<HomeContent> {
    List<HomeContent> getActiveContentByType(String type);
}
