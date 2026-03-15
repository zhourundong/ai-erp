package com.aierp.agent;

import com.aierp.ai.ModelAdapter;
import com.aierp.ai.ModelFactory;
import com.aierp.ai.dto.ChatRequest;
import com.aierp.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * AI Agent编排器
 *
 * 负责处理用户对话，协调意图识别、任务规划和工具执行
 * 这是AI原生ERP的核心智能引擎
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final ModelFactory modelFactory;
    private final IntentClassifier intentClassifier;
    private final TaskPlanner taskPlanner;

    /**
     * 流式回调接口
     */
    public interface StreamCallback {
        /**
         * 收到思考过程
         */
        default void onThinking(String thinking) {}

        /**
         * 收到实际内容
         */
        void onToken(String token);

        /**
         * 流式完成
         */
        void onComplete(ChatResponse response);

        /**
         * 发生错误
         */
        void onError(Exception e);
    }

    /**
     * 处理对话请求（同步）
     */
    public ChatResponse process(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取AI模型
            ModelAdapter model = modelFactory.getDefaultModel();
            if (!model.isAvailable()) {
                return ChatResponse.builder()
                        .error("AI模型不可用，请检查配置")
                        .build();
            }

            // 2. 意图识别
            String intent = intentClassifier.classify(request.getMessage(), model);
            log.info("识别到意图: {}", intent);

            // 3. 任务规划
            TaskPlan plan = taskPlanner.plan(intent, request.getMessage());
            log.info("任务计划: {}", plan);

            // 4. 设置系统提示
            String systemPrompt = buildSystemPrompt();
            ChatRequest enrichedRequest = ChatRequest.builder()
                    .message(request.getMessage())
                    .sessionId(request.getSessionId())
                    .history(request.getHistory())
                    .systemPrompt(systemPrompt)
                    .build();

            // 5. 调用AI模型处理
            ChatResponse response = model.chat(enrichedRequest);
            response.setIntent(intent);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            log.error("处理对话请求失败", e);
            return ChatResponse.builder()
                    .error("处理请求失败: " + e.getMessage())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 处理对话请求（流式）
     */
    public void processStream(ChatRequest request, StreamCallback callback) {
        long startTime = System.currentTimeMillis();
        log.info("[流式处理] 开始处理请求: {}", request.getMessage());

        try {
            // 1. 获取AI模型
            ModelAdapter model = modelFactory.getDefaultModel();
            log.info("[流式处理] 获取模型: {}, isAvailable={}, isStreamingAvailable={}",
                model.getModelName(), model.isAvailable(), model.isStreamingAvailable());

            if (!model.isAvailable()) {
                callback.onError(new Exception("AI模型不可用，请检查配置"));
                return;
            }

            // 2. 意图识别
            long intentStart = System.currentTimeMillis();
            String intent = intentClassifier.classify(request.getMessage(), model);
            log.info("[流式处理] 意图识别完成: {} (耗时: {}ms)", intent, System.currentTimeMillis() - intentStart);

            // 3. 任务规划
            long planStart = System.currentTimeMillis();
            TaskPlan plan = taskPlanner.plan(intent, request.getMessage());
            log.info("[流式处理] 任务规划完成: {} (耗时: {}ms)", plan, System.currentTimeMillis() - planStart);

            // 4. 设置系统提示
            String systemPrompt = buildSystemPrompt();
            ChatRequest enrichedRequest = ChatRequest.builder()
                    .message(request.getMessage())
                    .sessionId(request.getSessionId())
                    .history(request.getHistory())
                    .systemPrompt(systemPrompt)
                    .build();

            // 5. 调用流式AI模型处理
            final String finalIntent = intent;
            final int[] tokenCount = {0};

            log.info("[流式处理] 开始调用 model.streamChat()...");
            long streamStart = System.currentTimeMillis();

            model.streamChat(enrichedRequest, new ModelAdapter.StreamCallback() {
                @Override
                public void onThinking(String thinking) {
                    callback.onThinking(thinking);
                }

                @Override
                public void onToken(String token) {
                    tokenCount[0]++;
                    if (tokenCount[0] == 1) {
                        log.info("[流式处理] 收到第一个token (耗时: {}ms)", System.currentTimeMillis() - streamStart);
                    }
                    callback.onToken(token);
                }

                @Override
                public void onComplete(String fullResponse, Long thinkingTimeMs) {
                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("[流式处理] 完成: 总耗时={}ms, 思考耗时={}ms, token数={}, 内容长度={}",
                        totalTime, thinkingTimeMs, tokenCount[0], fullResponse != null ? fullResponse.length() : 0);
                    ChatResponse response = ChatResponse.builder()
                            .content(fullResponse)
                            .model(model.getModelName())
                            .intent(finalIntent)
                            .processingTimeMs(totalTime)
                            .thinkingTimeMs(thinkingTimeMs)
                            .timestamp(LocalDateTime.now())
                            .build();
                    callback.onComplete(response);
                }

                @Override
                public void onError(Exception e) {
                    log.error("[流式处理] 错误", e);
                    callback.onError(e);
                }
            });

            log.info("[流式处理] model.streamChat() 方法返回 (同步阻塞结束)");

        } catch (Exception e) {
            log.error("处理流式对话请求失败", e);
            callback.onError(e);
        }
    }

    /**
     * 构建系统提示
     */
    private String buildSystemPrompt() {
        return """
            你是AI原生ERP系统的智能助手，专注于采购供应链管理。

            你的职责：
            1. 帮助用户创建和管理采购申请
            2. 推荐合适的供应商
            3. 分析采购数据和风险
            4. 回答采购相关问题

            回答要求：
            - 专业、准确、简洁
            - 涉及敏感操作时需要提醒用户确认
            - 提供数据时要有清晰的格式
            - 如果信息不足，主动询问用户
            """;
    }
}
