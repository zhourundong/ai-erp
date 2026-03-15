package com.aierp.config;

import com.aierp.ai.ErpAssistant;
import com.aierp.ai.tools.CustomerTools;
import com.aierp.ai.tools.InventoryTools;
import com.aierp.ai.tools.NavigationTools;
import com.aierp.ai.tools.ProductTools;
import com.aierp.ai.tools.PurchaseOrderTools;
import com.aierp.ai.tools.SalesOrderTools;
import com.aierp.ai.tools.SupplierTools;
import com.aierp.ai.tools.WarehouseTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Service 配置类
 *
 * 配置 LangChain4j AI Services
 * 支持工具调用、会话记忆、流式响应
 *
 * 优先使用 Claude，如果未配置则使用 OpenAI
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiServiceConfig {

    @Autowired(required = false)
    private StreamingChatModel claudeStreamingModel;

    @Autowired(required = false)
    private StreamingChatModel openAiStreamingModel;

    private final PurchaseOrderTools purchaseOrderTools;
    private final SalesOrderTools salesOrderTools;
    private final SupplierTools supplierTools;
    private final CustomerTools customerTools;
    private final InventoryTools inventoryTools;
    private final ProductTools productTools;
    private final WarehouseTools warehouseTools;
    private final NavigationTools navigationTools;

    /**
     * 创建 ERP 智能助手 AI Service
     *
     * 优先级：Claude > OpenAI
     */
    @Bean
    public ErpAssistant erpAssistant() {
        StreamingChatModel model = claudeStreamingModel != null ? claudeStreamingModel : openAiStreamingModel;

        if (model == null) {
            log.warn("No AI model configured, ErpAssistant will not be available");
            return null;
        }

        log.info("Creating ErpAssistant with {} streaming model",
            model.getClass().getSimpleName());

        return AiServices.builder(ErpAssistant.class)
                .streamingChatModel(model)
                .tools(purchaseOrderTools, salesOrderTools, supplierTools, customerTools, inventoryTools, productTools, warehouseTools, navigationTools)
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
