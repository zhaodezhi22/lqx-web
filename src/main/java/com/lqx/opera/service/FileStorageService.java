package com.lqx.opera.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.lqx.opera.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";

    private final OssProperties ossProperties;

    public String upload(MultipartFile file) throws IOException {
        if (useOss()) {
            return uploadToOss(file);
        }
        return uploadToLocal(file);
    }

    private boolean useOss() {
        return ossProperties.isEnabled()
                && StringUtils.hasText(ossProperties.getEndpoint())
                && StringUtils.hasText(ossProperties.getBucket())
                && StringUtils.hasText(ossProperties.getAccessKeyId())
                && StringUtils.hasText(ossProperties.getAccessKeySecret());
    }

    private String uploadToOss(MultipartFile file) throws IOException {
        String objectKey = buildObjectKey(file.getOriginalFilename());
        OSS ossClient = null;
        try (InputStream inputStream = file.getInputStream()) {
            ossClient = new OSSClientBuilder().build(
                    normalizeEndpoint(ossProperties.getEndpoint()),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (StringUtils.hasText(file.getContentType())) {
                metadata.setContentType(file.getContentType());
            }
            ossClient.putObject(ossProperties.getBucket(), objectKey, inputStream, metadata);
            return buildPublicUrl(objectKey);
        } catch (Exception e) {
            log.error("Upload to OSS failed", e);
            throw new IOException("上传到 OSS 失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private String uploadToLocal(MultipartFile file) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, UPLOAD_DIR);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("创建本地上传目录失败");
        }

        String newFilename = UUID.randomUUID().toString().replace("-", "")
                + getExtension(file.getOriginalFilename());
        File dest = new File(uploadDir, newFilename);
        file.transferTo(dest);
        return "/files/" + newFilename;
    }

    private String buildObjectKey(String originalFilename) {
        LocalDate today = LocalDate.now();
        String extension = getExtension(originalFilename);
        String basePath = StringUtils.hasText(ossProperties.getBasePath())
                ? trimSlashes(ossProperties.getBasePath())
                : "uploads";
        return String.format(
                "%s/%d/%02d/%02d/%s%s",
                basePath,
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                UUID.randomUUID().toString().replace("-", ""),
                extension
        );
    }

    private String buildPublicUrl(String objectKey) {
        String customBaseUrl = trimTrailingSlash(ossProperties.getPublicBaseUrl());
        if (StringUtils.hasText(customBaseUrl)) {
            return customBaseUrl + "/" + objectKey;
        }

        String endpoint = trimProtocol(normalizeEndpoint(ossProperties.getEndpoint()));
        return "https://" + ossProperties.getBucket() + "." + endpoint + "/" + objectKey;
    }

    private String getExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }

    private String normalizeEndpoint(String endpoint) {
        String trimmed = endpoint == null ? "" : endpoint.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private String trimProtocol(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceFirst("^https?://", "");
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    private String trimSlashes(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
