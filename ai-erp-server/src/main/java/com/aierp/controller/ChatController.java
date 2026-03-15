package com.aierp.controller;

import com.aierp.agent.AgentOrchestrator;
import com.aierp.ai.dto.ChatRequest;
import com.aierp.ai.dto.ChatResponse;
import com.aierp.ai.dto.NavigationResult;
import com.aierp.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AI对话控制器
 *
 * AI原生ERP的核心交互接口
 * 支持自然语言对话，实现采购供应链的智能操作
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AgentOrchestrator agentOrchestrator;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 发送对话消息（同步）
     *
     * 支持请求取消：当客户端断开连接时，会自动取消AI请求
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<ChatResponse>> chat(
            @RequestBody ChatRequest request) {

        log.info("收到同步对话请求: {}", request.getMessage());

        // 创建可取消的任务
        CompletableFuture<ChatResponse> future = CompletableFuture.supplyAsync(() -> {
            return agentOrchestrator.process(request);
        }, executorService);

        // 设置超时
        future.orTimeout(120, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("请求超时或被取消: {}", ex.getMessage());
                    return ChatResponse.builder()
                            .error("请求超时或已取消")
                            .build();
                });

        return future.thenApply(ResponseEntity::ok);
    }

    /**
     * 流式对话（SSE）
     *
     * 用于长响应，逐步返回结果
     * 客户端可以通过关闭连接来中断请求
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {

        log.info("收到流式对话请求: sessionId={}, message={}", request.getSessionId(), request.getMessage());

        // 捕获当前线程的用户上下文（用于传递给异步线程）
        UserContext capturedContext = UserContext.get();

        // 设置SSE响应头，禁用缓冲
        httpResponse.setHeader("Cache-Control", "no-cache, no-transform");
        httpResponse.setHeader("Connection", "keep-alive");
        httpResponse.setHeader("X-Accel-Buffering", "no");

        // 创建SSE发射器，超时10分钟（与前端超时一致）
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        // 标记是否已完成
        final boolean[] isCompleted = {false};

        // 在异步线程中处理
        executorService.execute(() -> {
            // 在异步线程中设置用户上下文
            if (capturedContext != null) {
                UserContext.set(capturedContext);
            }

            try {
                agentOrchestrator.processStream(request, new AgentOrchestrator.StreamCallback() {
                    @Override
                    public void onThinking(String thinking) {
                        if (isCompleted[0]) return;
                        try {
                            // 使用 Base64 编码确保数据完整性
                            String encoded = java.util.Base64.getEncoder().encodeToString(
                                thinking.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            emitter.send(SseEmitter.event()
                                    .name("thinking")
                                    .data(encoded));
                        } catch (IOException e) {
                            log.warn("发送thinking失败，客户端可能已断开: {}", e.getMessage());
                            isCompleted[0] = true;
                            emitter.completeWithError(e);
                        } catch (IllegalStateException e) {
                            log.warn("Emitter已完成，无法发送thinking: {}", e.getMessage());
                            isCompleted[0] = true;
                        }
                    }

                    @Override
                    public void onToken(String token) {
                        if (isCompleted[0]) return;
                        try {
                            // 使用 Base64 编码确保数据完整性
                            String encoded = java.util.Base64.getEncoder().encodeToString(
                                token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(encoded));
                        } catch (IOException e) {
                            log.warn("发送token失败，客户端可能已断开: {}", e.getMessage());
                            isCompleted[0] = true;
                            emitter.completeWithError(e);
                        } catch (IllegalStateException e) {
                            // Emitter已经完成
                            log.warn("Emitter已完成，无法发送token: {}", e.getMessage());
                            isCompleted[0] = true;
                        }
                    }

                    @Override
                    public void onComplete(ChatResponse response) {
                        if (isCompleted[0]) return;
                        isCompleted[0] = true;
                        try {
                            log.debug("发送complete事件，内容长度: {}", response.getContent() != null ? response.getContent().length() : 0);
                            // 发送完成事件
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(response));
                            log.info("流式响应完成，sessionId={}", request.getSessionId());
                        } catch (IOException e) {
                            log.warn("发送完成事件失败: {}", e.getMessage());
                        } catch (IllegalStateException e) {
                            log.warn("Emitter已完成，无法发送complete: {}", e.getMessage());
                        } finally {
                            try {
                                emitter.complete();
                            } catch (Exception e) {
                                log.debug("关闭emitter时出错（可忽略）: {}", e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (isCompleted[0]) return;
                        isCompleted[0] = true;
                        log.error("流式响应错误", e);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(e.getMessage() != null ? e.getMessage() : "未知错误"));
                        } catch (IOException ex) {
                            log.warn("发送错误事件失败: {}", ex.getMessage());
                        } catch (IllegalStateException ex) {
                            log.warn("Emitter已完成，无法发送error: {}", ex.getMessage());
                        } finally {
                            try {
                                emitter.completeWithError(e);
                            } catch (Exception ex) {
                                log.debug("关闭emitter时出错（可忽略）: {}", ex.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onToolExecuted(dev.langchain4j.service.tool.ToolExecution toolExecution) {
                        if (isCompleted[0]) return;
                        try {
                            log.info("工具执行: {}", toolExecution.request().name());
                            // 发送工具执行事件
                            java.util.Map<String, Object> toolInfo = new java.util.HashMap<>();
                            toolInfo.put("name", toolExecution.request().name());
                            toolInfo.put("arguments", toolExecution.request().arguments());
                            toolInfo.put("result", toolExecution.result());
                            emitter.send(SseEmitter.event()
                                    .name("tool")
                                    .data(toolInfo));
                        } catch (IOException e) {
                            log.warn("发送tool事件失败: {}", e.getMessage());
                        } catch (IllegalStateException e) {
                            log.warn("Emitter已完成，无法发送tool: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onAction(NavigationResult action) {
                        if (isCompleted[0]) return;
                        try {
                            log.info("发送action事件: action={}, path={}", action.getAction(), action.getPath());
                            emitter.send(SseEmitter.event()
                                    .name("action")
                                    .data(action));
                        } catch (IOException e) {
                            log.warn("发送action事件失败: {}", e.getMessage());
                        } catch (IllegalStateException e) {
                            log.warn("Emitter已完成，无法发送action: {}", e.getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                log.error("处理流式请求失败", e);
                if (!isCompleted[0]) {
                    isCompleted[0] = true;
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(e.getMessage() != null ? e.getMessage() : "服务器内部错误"));
                    } catch (IOException | IllegalStateException ex) {
                        // ignore
                    }
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            } finally {
                // 清理异步线程的用户上下文
                UserContext.clear();
            }
        });

        // 客户端断开连接时的回调
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成，sessionId={}", request.getSessionId());
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，sessionId={}", request.getSessionId());
            if (!isCompleted[0]) {
                isCompleted[0] = true;
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("连接超时"));
                } catch (Exception e) {
                    // ignore
                }
            }
            emitter.complete();
        });

        emitter.onError(throwable -> {
            log.warn("SSE连接错误，sessionId={}: {}", request.getSessionId(), throwable.getMessage());
            isCompleted[0] = true;
        });

        return emitter;
    }
}
