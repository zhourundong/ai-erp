package com.aierp.ai;

import com.aierp.ai.adapter.ClaudeAdapter;
import com.aierp.ai.adapter.OpenAIAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI模型工厂
 *
 * 根据配置选择合适的AI模型适配器
 * 支持运行时切换模型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelFactory {

    private final ClaudeAdapter claudeAdapter;
    private final OpenAIAdapter openAIAdapter;

    @Value("${spring.ai.models.default:claude}")
    private String defaultModel;

    /**
     * 获取默认模型适配器
     */
    public ModelAdapter getDefaultModel() {
        return getModel(defaultModel);
    }

    /**
     * 根据名称获取模型适配器
     *
     * @param modelName 模型名称: claude, openai
     * @return 模型适配器
     */
    public ModelAdapter getModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            modelName = (defaultModel != null && !defaultModel.isEmpty()) ? defaultModel : "claude";
        }

        ModelAdapter adapter = switch (modelName.toLowerCase()) {
            case "claude" -> claudeAdapter;
            case "openai", "gpt" -> openAIAdapter;
            default -> {
                log.warn("未知的模型名称: {}, 使用默认模型", modelName);
                yield claudeAdapter;
            }
        };

        // 检查模型是否可用
        if (!adapter.isAvailable()) {
            log.warn("模型 {} 不可用, 尝试使用备选模型", modelName);
            // 尝试使用备选模型
            if (!modelName.equalsIgnoreCase("claude") && claudeAdapter.isAvailable()) {
                return claudeAdapter;
            }
            if (!modelName.equalsIgnoreCase("openai") && openAIAdapter.isAvailable()) {
                return openAIAdapter;
            }
        }

        return adapter;
    }

    /**
     * 获取所有可用的模型
     */
    public List<String> getAvailableModels() {
        return List.of("claude", "openai")
                .stream()
                .filter(name -> getModel(name).isAvailable())
                .toList();
    }

    /**
     * 检查是否有可用的模型
     */
    public boolean hasAvailableModel() {
        return claudeAdapter.isAvailable() || openAIAdapter.isAvailable();
    }
}
