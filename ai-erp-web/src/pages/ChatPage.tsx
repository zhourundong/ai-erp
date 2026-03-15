import { useRef, useEffect, useState } from 'react'
import { Input, Button, Avatar, Spin, Card, Tag, Space, Popconfirm, Collapse } from 'antd'
import { SendOutlined, RobotOutlined, UserOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined, StopOutlined, BulbOutlined, ToolOutlined, CheckCircleOutlined } from '@ant-design/icons'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { chatApi } from '../services/api'
import { useAuthStore } from '../stores/authStore'
import { useChatStore, StreamEvent, ToolExecution } from '../stores/chatStore'
import './ChatPage.css'

// 存储当前请求的 AbortController
const abortControllers = new Map<string, AbortController>()

// 解码 Base64 数据
function decodeBase64(encoded: string): string {
  try {
    const binaryString = atob(encoded)
    const bytes = new Uint8Array(binaryString.length)
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i)
    }
    return new TextDecoder('utf-8').decode(bytes)
  } catch (e) {
    console.error('[SSE] Base64解码失败:', e, encoded)
    return encoded
  }
}

// 格式化耗时显示
function formatDuration(ms: number | undefined): string {
  if (!ms) return ''
  if (ms < 1000) return `${ms}ms`
  if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`
  return `${Math.floor(ms / 60000)}m ${((ms % 60000) / 1000).toFixed(0)}s`
}

// 单个工具执行卡片
function ToolCard({ tool }: { tool: ToolExecution }) {
  return (
    <Card size="small" className="tool-card-inline">
      <div className="tool-header-inline">
        <ToolOutlined style={{ color: '#52c41a' }} />
        <span className="tool-name">{tool.name}</span>
        <Tag color="success" icon={<CheckCircleOutlined />}>已执行</Tag>
      </div>
      <Collapse
        size="small"
        ghost
        items={[
          {
            key: '1',
            label: <span style={{ color: '#666', fontSize: '12px' }}>查看结果</span>,
            children: (
              <pre className="tool-result">
                {typeof tool.result === 'string' ? tool.result : JSON.stringify(tool.result, null, 2)}
              </pre>
            ),
          },
        ]}
      />
    </Card>
  )
}

// 渲染流式事件列表（按顺序）
function StreamEventsDisplay({ events, isLoading }: { events: StreamEvent[]; isLoading?: boolean }) {
  if (!events || events.length === 0) return null

  return (
    <div className="stream-events">
      {events.map((event, index) => {
        if (event.type === 'tool' && event.tool) {
          return <ToolCard key={`tool-${index}`} tool={event.tool} />
        } else if (event.type === 'text' && event.content) {
          return (
            <ReactMarkdown key={`text-${index}`} remarkPlugins={[remarkGfm]}>
              {event.content}
            </ReactMarkdown>
          )
        }
        return null
      })}
      {isLoading && <span className="typing-cursor">▌</span>}
    </div>
  )
}

// 思考过程折叠组件
function ThinkingCollapse({ thinking, isLoading, thinkingTimeMs }: { thinking: string; isLoading?: boolean; thinkingTimeMs?: number }) {
  const [activeKey, setActiveKey] = useState<string[]>(isLoading ? ['thinking'] : [])

  useEffect(() => {
    setActiveKey(isLoading ? ['thinking'] : [])
  }, [isLoading])

  return (
    <Collapse
      className="thinking-collapse"
      activeKey={activeKey}
      onChange={(keys) => setActiveKey(keys as string[])}
      items={[
        {
          key: 'thinking',
          label: (
            <span className="thinking-header">
              <BulbOutlined /> 思考过程
              {thinkingTimeMs && !isLoading && (
                <span className="thinking-duration"> · {formatDuration(thinkingTimeMs)}</span>
              )}
            </span>
          ),
          children: (
            <div className="thinking-content">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{thinking}</ReactMarkdown>
            </div>
          ),
        },
      ]}
    />
  )
}

export default function ChatPage() {
  const [inputValue, setInputValue] = useState('')
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const { user } = useAuthStore()
  const {
    sessions,
    currentSessionId,
    createSession,
    getCurrentSession,
    switchSession,
    deleteSession,
    clearCurrentSession,
  } = useChatStore()

  const currentSession = getCurrentSession()
  const messages = currentSession?.messages || []
  const loading = messages.some(m => m.loading)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  useEffect(() => {
    const timer = setTimeout(() => {
      const state = useChatStore.getState()
      if (state.sessions.length === 0) {
        state.createSession()
      } else if (!state.currentSessionId) {
        state.switchSession(state.sessions[0].id)
      }
    }, 0)
    return () => clearTimeout(timer)
  }, [])

  const handleSend = async () => {
    const input = inputValue.trim()
    if (!input || loading) return

    setInputValue('')

    const store = useChatStore.getState()
    let sessionId = store.currentSessionId
    if (!sessionId) {
      sessionId = store.createSession()
    }

    store.addMessageToSession(sessionId, {
      role: 'user',
      content: input,
    })

    const loadingMsgId = store.addMessageToSession(sessionId, {
      role: 'assistant',
      content: '',
      events: [],
      loading: true,
    })

    const abortController = new AbortController()
    abortControllers.set(loadingMsgId, abortController)
    store.registerPendingRequest(loadingMsgId, sessionId)

    const session = store.getSession(sessionId)
    const historyMessages = (session?.messages || [])
      .filter(m => !m.loading)
      .slice(0, -1)
      .map(m => ({
        role: m.role,
        content: m.content,
      }))

    chatApi.stream(
      input,
      sessionId,
      historyMessages,
      // onToken
      (token: string) => {
        const decodedToken = decodeBase64(token)
        store.appendTextToMessage(sessionId, loadingMsgId, decodedToken)
      },
      // onComplete
      (response: any) => {
        store.updateMessageInSession(sessionId, loadingMsgId, {
          loading: false,
          thinkingTimeMs: response.thinkingTimeMs,
          processingTimeMs: response.processingTimeMs,
        })
        abortControllers.delete(loadingMsgId)
      },
      // onError
      (error: Error) => {
        if (abortController.signal.aborted) return
        store.updateMessageInSession(sessionId, loadingMsgId, {
          content: error.message || '抱歉，发生了错误，请稍后重试。',
          loading: false,
        })
        abortControllers.delete(loadingMsgId)
      },
      abortController.signal,
      // onThinking
      (thinking: string) => {
        const decodedThinking = decodeBase64(thinking)
        const currentSession = useChatStore.getState().getSession(sessionId)
        const currentMsg = currentSession?.messages.find(m => m.id === loadingMsgId)
        if (currentMsg) {
          store.updateMessageInSession(sessionId, loadingMsgId, {
            thinking: (currentMsg.thinking || '') + decodedThinking,
          })
        }
      },
      // onTool
      (toolExecution: ToolExecution) => {
        store.appendToolToMessage(sessionId, loadingMsgId, toolExecution)
      }
    )
  }

  const handleStop = () => {
    const store = useChatStore.getState()
    const session = store.getCurrentSession()
    if (!session) return

    const loadingMsg = session.messages.find(m => m.loading)
    if (loadingMsg) {
      const controller = abortControllers.get(loadingMsg.id)
      if (controller) {
        controller.abort()
        abortControllers.delete(loadingMsg.id)
      }
      store.updateMessageInSession(session.id, loadingMsg.id, {
        content: '已停止生成',
        loading: false,
      })
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="chat-page">
      <div className="sessions-sidebar">
        <div className="sessions-header">
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              // 如果当前会话存在且没有消息，不创建新会话
              if (currentSession && currentSession.messages.length === 0) {
                return
              }
              // 检查最新的会话是否为空（sessions按时间倒序排列，第一个是最新的）
              const latestSession = sessions[0]
              if (latestSession && latestSession.messages.length === 0) {
                switchSession(latestSession.id)
                return
              }
              // 否则创建新会话
              createSession()
            }}
            block
          >
            新对话
          </Button>
        </div>
        <div className="sessions-list">
          {sessions.map((session) => (
            <div
              key={session.id}
              className={`session-item ${session.id === currentSessionId ? 'active' : ''}`}
              onClick={() => switchSession(session.id)}
            >
              <div className="session-title">{session.title}</div>
              <div className="session-time">
                {new Date(session.updatedAt).toLocaleDateString()}
              </div>
              <Popconfirm
                title="确定删除这个对话？"
                onConfirm={(e) => {
                  e?.stopPropagation()
                  deleteSession(session.id)
                }}
                onCancel={(e) => e?.stopPropagation()}
              >
                <Button
                  type="text"
                  size="small"
                  icon={<DeleteOutlined />}
                  className="session-delete"
                  onClick={(e) => e.stopPropagation()}
                />
              </Popconfirm>
            </div>
          ))}
        </div>
      </div>

      <div className="chat-container">
        <div className="messages-container">
          {messages.length === 0 ? (
            <div className="empty-chat">
              <div className="welcome-icon">🤖</div>
              <h2>您好，{user?.realName || user?.username}！</h2>
              <p>我是您的AI采购助手，有什么可以帮您的吗？</p>
              <div className="quick-actions">
                <Card className="quick-action-card" onClick={() => setInputValue('帮我创建一个采购订单')}>
                  <p>📝 创建采购订单</p>
                </Card>
                <Card className="quick-action-card" onClick={() => setInputValue('推荐一些优质供应商')}>
                  <p>🏪 推荐供应商</p>
                </Card>
                <Card className="quick-action-card" onClick={() => setInputValue('查询最近的采购订单')}>
                  <p>📋 查询订单</p>
                </Card>
                <Card className="quick-action-card" onClick={() => setInputValue('查询库存情况')}>
                  <p>📊 库存查询</p>
                </Card>
              </div>
            </div>
          ) : (
            messages.map((message) => (
              <div key={message.id} className={`message-wrapper message-fade-in ${message.role}`}>
                <div className="message-avatar">
                  <Avatar
                    size={40}
                    style={{ backgroundColor: message.role === 'user' ? '#667eea' : '#8b5cf6' }}
                    icon={message.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                  >
                    {message.role === 'user' ? user?.realName?.[0] || user?.username?.[0] : 'AI'}
                  </Avatar>
                </div>
                <div className="message-content">
                  {message.loading && !message.content && !message.thinking && !(message.events?.length) ? (
                    <div className="message-loading">
                      <Spin size="small" />
                      <span>AI正在思考中...</span>
                    </div>
                  ) : (
                    <>
                      {/* 思考过程 */}
                      {message.thinking && (
                        <ThinkingCollapse
                          thinking={message.thinking}
                          isLoading={message.loading}
                          thinkingTimeMs={message.thinkingTimeMs}
                        />
                      )}

                      {/* 流式事件（文字+工具按顺序） */}
                      {message.events && message.events.length > 0 ? (
                        <StreamEventsDisplay events={message.events} isLoading={message.loading} />
                      ) : (
                        <div className="message-text">
                          <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {message.content}
                          </ReactMarkdown>
                          {message.loading && <span className="typing-cursor">▌</span>}
                        </div>
                      )}

                      <div className="message-meta">
                        <span className="message-time">
                          {new Date(message.timestamp).toLocaleTimeString()}
                        </span>
                        {message.processingTimeMs && (
                          <span className="message-duration">
                            耗时 {formatDuration(message.processingTimeMs)}
                          </span>
                        )}
                      </div>
                    </>
                  )}
                </div>
              </div>
            ))
          )}
          <div ref={messagesEndRef} />
        </div>

        <div className="input-container">
          <div className="input-wrapper">
            <Input.TextArea
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="输入消息... (Shift+Enter换行，Enter发送)"
              autoSize={{ minRows: 1, maxRows: 4 }}
              className="chat-input"
            />
            <Space>
              <Popconfirm title="确定清空当前对话？" onConfirm={clearCurrentSession}>
                <Button icon={<ReloadOutlined />} disabled={messages.length === 0 || loading}>
                  清空
                </Button>
              </Popconfirm>
              {loading ? (
                <Button danger icon={<StopOutlined />} onClick={handleStop}>
                  停止
                </Button>
              ) : (
                <Button type="primary" icon={<SendOutlined />} onClick={handleSend} disabled={!inputValue.trim()}>
                  发送
                </Button>
              )}
            </Space>
          </div>
        </div>
      </div>
    </div>
  )
}
