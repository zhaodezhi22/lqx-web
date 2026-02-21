package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件为空");
        }

        try {
            // Ensure upload directory exists
            String projectRoot = System.getProperty("user.dir");
            File uploadDir = new File(projectRoot, UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            File dest = new File(uploadDir, newFilename);
            file.transferTo(dest);

            // Return URL (Assuming static resource mapping is configured for /files/**)
            // Note: The frontend expects a full URL or relative path.
            // If we map /files/** to file:./uploads/, then return /files/filename
            String fileUrl = "/files/" + newFilename;
            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败: " + e.getMessage());
        }
    }
}
