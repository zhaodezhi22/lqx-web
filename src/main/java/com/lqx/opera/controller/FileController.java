package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件为空");
        }

        try {
            return Result.success(fileStorageService.upload(file));

        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败: " + e.getMessage());
        }
    }
}
