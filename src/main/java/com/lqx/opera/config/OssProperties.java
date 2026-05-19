package com.lqx.opera.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    private boolean enabled;
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;
    private String publicBaseUrl;
    private String basePath;
}
