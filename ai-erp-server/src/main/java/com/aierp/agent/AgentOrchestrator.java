package com.aierp.agent;

import com.aierp.ai.ErpAssistant;
import com.aierp.ai.dto.ChatRequest;
import com.aierp.ai.dto.ChatResponse;
import com.aierp.context.UserContext;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Agent编排器
 *
 * 负责处理用户对话，协调工具执行
 * 使用 LangChain4j AI Services 框架，支持工具调用和流式响应
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final ErpAssistant erpAssistant;

    /**
     * 流式回调接口
     */
    public interface StreamCallback {
        default void onThinking(String thinking) {}
        void onToken(String token);
        void onComplete(ChatResponse response);
        void onError(Exception e);
        default void onToolExecuted(ToolExecution toolExecution) {}
    }

    /**
     * 处理对话请求（同步）
     */
    public ChatResponse process(ChatRequest request) {
        return ChatResponse.builder()
                .error("同步模式暂不支持，请使用流式模式")
                .build();
    }

    /**
     * 处理对话请求（流式）
     *
     * 统一使用 AI Services 模式，支持工具调用
     * 用户信息通过 InvocationParameters 传递 UserContext，在 Tool 中通过 InvocationContext 获取
     */
    public void processStream(ChatRequest request, StreamCallback callback) {
        long startTime = System.currentTimeMillis();
        log.info("[流式处理] 开始处理请求: {}", request.getMessage());

        if (erpAssistant == null) {
            callback.onError(new Exception("AI模型未配置，请检查配置"));
            return;
        }

        try {
            String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
            StringBuilder fullResponse = new StringBuilder();
            List<ToolExecution> toolExecutions = new ArrayList<>();
            StringBuilder thinkingBuilder = new StringBuilder();
            long[] thinkingStartTime = {0};
            long[] thinkingEndTime = {0};

            // 获取当前用户信息
            UserContext userContext = UserContext.get();
            Long userId = userContext != null ? userContext.getUserId() : null;
            String username = userContext != null ? userContext.getUsername() : "未知用户";

            log.info("[流式处理] 用户信息: userId={}, username={}", userId, username);

            // 将用户信息存储到会话上下文（备用）
            if (userId != null) {
                UserContext.setForSession(sessionId, userContext);
            }

            // 通过 InvocationParameters 传递 UserContext 对象
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("userContext", userContext);
            InvocationParameters parameters = InvocationParameters.from(paramsMap);

            TokenStream tokenStream = erpAssistant.chat(sessionId, request.getMessage(), parameters);

            tokenStream
                .onPartialResponse(token -> {
                    // 检查是否是思考过程（某些模型如 DeepSeek 会输出思考和...[思考内容]...SKU 标签）
                    if (isThinkingToken(token, thinkingBuilder, thinkingStartTime, thinkingEndTime)) {
                        callback.onThinking(token);
                    } else {
                        fullResponse.append(token);
                        callback.onToken(token);
                    }
                })
                .onCompleteResponse(response -> {
                    long processingTime = System.currentTimeMillis() - startTime;
                    Long thinkingTime = null;
                    if (thinkingStartTime[0] > 0 && thinkingEndTime[0] > 0) {
                        thinkingTime = thinkingEndTime[0] - thinkingStartTime[0];
                    }

                    log.info("[流式处理] 完成: 耗时={}ms, 内容长度={}, 工具调用次数={}, 思考耗时={}ms",
                            processingTime, fullResponse.length(), toolExecutions.size(), thinkingTime);

                    // 清理会话用户上下文
                    UserContext.clearForSession(sessionId);

                    ChatResponse chatResponse = ChatResponse.builder()
                            .content(fullResponse.toString())
                            .model("AI-Services")
                            .processingTimeMs(processingTime)
                            .thinkingTimeMs(thinkingTime)
                            .timestamp(LocalDateTime.now())
                            .build();
                    callback.onComplete(chatResponse);
                })
                .onToolExecuted(toolExecution -> {
                    log.info("[工具执行] name={}, result={}",
                            toolExecution.request().name(),
                            truncate(toolExecution.result(), 200));
                    toolExecutions.add(toolExecution);
                    callback.onToolExecuted(toolExecution);
                })
                .onError(error -> {
                    log.error("[流式处理] 错误", error);
                    // 清理会话用户上下文
                    UserContext.clearForSession(sessionId);
                    callback.onError(new Exception(error));
                })
                .start();

        } catch (Exception e) {
            log.error("[流式处理] 处理失败", e);
            callback.onError(e);
        }
    }

    /**
     * 检查是否是思考过程的 token
     *
     * 支持模型输出的 思考和...[思考内容]... SKU 标签格式
     */
    private boolean isThinkingToken(String token, StringBuilder thinkingBuilder, long[] thinkingStartTime, long[] thinkingEndTime) {
        // 检查是否进入思考模式
        if (token.contains("思考和")) {
            if (thinkingStartTime[0] == 0) {
                thinkingStartTime[0] = System.currentTimeMillis();
            }
            thinkingBuilder.append(token);
            return true;
        }

        // 已经在思考模式中
        if (thinkingStartTime[0] > 0 && thinkingEndTime[0] == 0) {
            thinkingBuilder.append(token);
            // 检查是否结束思考模式
            if (token.contains("SKU")) {
                thinkingEndTime[0] = System.currentTimeMillis();
            }
            return true;
        }

        return false;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}
