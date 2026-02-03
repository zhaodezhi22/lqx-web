package com.lqx.opera.service;

import com.lqx.opera.common.dto.SearchDTO;
import java.util.List;

public interface SearchService {
    /**
     * 全站搜索
     * @param keyword 关键词
     * @return 搜索结果列表
     */
    List<SearchDTO> globalSearch(String keyword);
}
