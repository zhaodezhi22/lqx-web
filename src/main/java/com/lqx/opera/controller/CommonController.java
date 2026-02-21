package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.SearchDTO;
import com.lqx.opera.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private final SearchService searchService;

    public CommonController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public Result<List<SearchDTO>> search(@RequestParam String keyword) {
        return Result.success(searchService.globalSearch(keyword));
    }
}
