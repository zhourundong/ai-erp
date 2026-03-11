package com.aierp.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AI模型配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIModelConfig {

    /**
     * 默认模型
     */
    private String defaultModel = "qwen-plus";

    /**
     * 模型配置列表
     */
    private Map<String, ModelConfig> models = new HashMap<>();

    /**
     * 能力路由配置
     */
    private Map<String, CapabilityRouting> capabilityRouting = new HashMap<>();

    /**
     * 获取指定能力的模型
     */
    public String getModelForCapability(String capability) {
        CapabilityRouting routing = capabilityRouting.get(capability);
        if (routing != null && routing.getPrimary() != null) {
            return routing.getPrimary();
        }
        return defaultModel;
    }

    /**
     * 获取备用模型
     */
    public String getFallbackModel(String capability) {
        CapabilityRouting routing = capabilityRouting.get(capability);
        if (routing != null && routing.getFallback() != null) {
            return routing.getFallback();
        }
        return defaultModel;
    }

    /**
     * 检查模型是否启用
     */
    public boolean isModelEnabled(String modelName) {
        ModelConfig config = models.get(modelName);
        return config != null && Boolean.TRUE.equals(config.getEnabled());
    }

    /**
     * 获取模型配置
     */
    public ModelConfig getModelConfig(String modelName) {
        return models.get(modelName);
    }

    /**
     * 模型配置
     */
    @Data
    public static class ModelConfig {
        private Boolean enabled;
        private String provider;
        private String apiKey;
        private String secretKey;
        private String endpoint;
        private String model;
        private Integer maxTokens = 2000;
        private Double temperature = 0.7;
    }

    /**
     * 能力路由配置
     */
    @Data
    public static class CapabilityRouting {
        private String primary;
        private String fallback;
    }
}
