import { useRef, useEffect, useState } from 'react'
import { Input, Button, Avatar, Spin, Card, Tag, Space, Popconfirm, Collapse } from 'antd'
import { SendOutlined, RobotOutlined, UserOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined, StopOutlined, BulbOutlined } from '@ant-design/icons'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { chatApi } from '../services/api'
import { useAuthStore } from '../stores/authStore'
import { useChatStore } from '../stores/chatStore'
import './ChatPage.css'

// 存储当前请求的 AbortController
const abortControllers = new Map<string, AbortController>()

// 解码 Base64 数据
function decodeBase64(encoded: string): string {
  try {
    // Base64 解码
    const binaryString = atob(encoded)
    // 转换为 UTF-8
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

// 处理思考过程中的换行符
function processThinkingText(text: string): string {
  return text
}

// 思考过程折叠组件 - 管理自己的展开/折叠状态
function ThinkingCollapse({ thinking, isLoading }: { thinking: string; isLoading?: boolean }) {
  const [activeKey, setActiveKey] = useState<string[]>(isLoading ? ['thinking'] : [])

  // 当 loading 状态变化时更新展开状态
  useEffect(() => {
    if (isLoading) {
      setActiveKey(['thinking'])
    } else {
      setActiveKey([])
    }
  }, [isLoading])

  // 处理换行符
  const processedThinking = processThinkingText(thinking)

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
            </span>
          ),
          children: (
            <div className="thinking-content">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {processedThinking}
              </ReactMarkdown>
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

  // 初始化：如果没有会话则创建一个
  useEffect(() => {
    // 使用 setTimeout 确保 zustand persist 中间件已经从 localStorage 恢复状态
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

    if (!input || loading) {
      return
    }

    // 清空输入框
    setInputValue('')

    // 确保有会话 - 直接从 store 获取最新状态
    const store = useChatStore.getState()
    let sessionId = store.currentSessionId
    if (!sessionId) {
      sessionId = store.createSession()
    }

    // 添加用户消息到指定会话
    store.addMessageToSession(sessionId, {
      role: 'user',
      content: input,
    })

    // 添加AI加载消息，保存消息ID用于后续更新
    const loadingMsgId = store.addMessageToSession(sessionId, {
      role: 'assistant',
      content: '',
      loading: true,
    })

    // 创建 AbortController 用于取消请求
    const abortController = new AbortController()
    abortControllers.set(loadingMsgId, abortController)

    // 注册待处理请求
    store.registerPendingRequest(loadingMsgId, sessionId)

    // 获取消息历史
    const session = store.getSession(sessionId)
    const historyMessages = (session?.messages || [])
      .filter(m => !m.loading)
      .slice(0, -1)
      .map(m => ({
        role: m.role,
        content: m.content,
      }))

    // 使用流式响应
    chatApi.stream(
      input,
      sessionId,
      historyMessages,
      // onToken - 逐步更新内容
      (token: string) => {
        console.log('[SSE] 收到token:', token)
        const currentSession = useChatStore.getState().getSession(sessionId)
        const currentMsg = currentSession?.messages.find(m => m.id === loadingMsgId)
        if (currentMsg) {
          // Base64 解码
          const decodedToken = decodeBase64(token)
          console.log('[SSE] 解析后:', decodedToken)
          store.updateMessageInSession(sessionId, loadingMsgId, {
            content: currentMsg.content + decodedToken,
          })
        }
      },
      // onComplete - 完成响应
      (response: any) => {
        console.log('[SSE] 完成:', response)
        store.updateMessageInSession(sessionId, loadingMsgId, {
          loading: false,
          intent: response.intent,
          toolResult: response.toolResult,
        })
        abortControllers.delete(loadingMsgId)
      },
      // onError - 错误处理
      (error: Error) => {
        console.error('[SSE] 错误:', error)
        if (abortController.signal.aborted) {
          return
        }
        store.updateMessageInSession(sessionId, loadingMsgId, {
          content: error.message || '抱歉，发生了错误，请稍后重试。',
          loading: false,
        })
        abortControllers.delete(loadingMsgId)
      },
      abortController.signal,
      // onThinking - 思考过程
      (thinking: string) => {
        console.log('[SSE] 收到thinking:', thinking)
        const currentSession = useChatStore.getState().getSession(sessionId)
        const currentMsg = currentSession?.messages.find(m => m.id === loadingMsgId)
        if (currentMsg) {
          // Base64 解码
          const decodedThinking = decodeBase64(thinking)
          console.log('[SSE] thinking解析后:', decodedThinking)
          store.updateMessageInSession(sessionId, loadingMsgId, {
            thinking: (currentMsg.thinking || '') + decodedThinking,
          })
        }
      }
    )
  }

  // 停止当前响应
  const handleStop = () => {
    const store = useChatStore.getState()
    const session = store.getCurrentSession()
    if (!session) return

    // 找到正在加载的消息
    const loadingMsg = session.messages.find(m => m.loading)
    if (loadingMsg) {
      // 取消请求
      const controller = abortControllers.get(loadingMsg.id)
      if (controller) {
        controller.abort()
        abortControllers.delete(loadingMsg.id)
      }

      // 更新消息状态
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

  const handleNewChat = () => {
    createSession()
  }

  const handleClearChat = () => {
    clearCurrentSession()
  }

  const handleDeleteSession = (sessionId: string) => {
    deleteSession(sessionId)
  }

  return (
    <div className="chat-page">
      {/* 左侧会话列表 */}
      <div className="sessions-sidebar">
        <div className="sessions-header">
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleNewChat}
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
                  handleDeleteSession(session.id)
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

      {/* 右侧对话区域 */}
      <div className="chat-container">
        {/* 消息列表 */}
        <div className="messages-container">
          {messages.length === 0 ? (
            <div className="empty-chat">
              <div className="welcome-icon">🤖</div>
              <h2>您好，{user?.realName || user?.username}！</h2>
              <p>我是您的AI采购助手，有什么可以帮您的吗？</p>
              <div className="quick-actions">
                <Card
                  className="quick-action-card"
                  onClick={() => setInputValue('帮我创建一个采购申请')}
                >
                  <p>📝 创建采购申请</p>
                </Card>
                <Card
                  className="quick-action-card"
                  onClick={() => setInputValue('推荐一些优质供应商')}
                >
                  <p>🏪 推荐供应商</p>
                </Card>
                <Card
                  className="quick-action-card"
                  onClick={() => setInputValue('查询最近的采购订单')}
                >
                  <p>📋 查询订单</p>
                </Card>
                <Card
                  className="quick-action-card"
                  onClick={() => setInputValue('分析最近的采购数据')}
                >
                  <p>📊 数据分析</p>
                </Card>
              </div>
            </div>
          ) : (
            messages.map((message) => (
              <div
                key={message.id}
                className={`message-wrapper message-fade-in ${message.role}`}
              >
                <div className="message-avatar">
                  <Avatar
                    size={40}
                    style={{
                      backgroundColor:
                        message.role === 'user' ? '#667eea' : '#8b5cf6',
                    }}
                    icon={
                      message.role === 'user' ? (
                        <UserOutlined />
                      ) : (
                        <RobotOutlined />
                      )
                    }
                  >
                    {message.role === 'user'
                      ? user?.realName?.[0] || user?.username?.[0]
                      : 'AI'}
                  </Avatar>
                </div>
                <div className="message-content">
                  {message.loading && !message.content && !message.thinking ? (
                    <div className="message-loading">
                      <Spin size="small" />
                      <span>AI正在思考中...</span>
                    </div>
                  ) : (
                    <>
                      {/* 思考过程（可折叠） - 加载时默认展开，完成后折叠 */}
                      {message.thinking && (
                        <ThinkingCollapse
                          thinking={message.thinking}
                          isLoading={message.loading}
                        />
                      )}
                      {message.intent && (
                        <Tag color="blue" className="intent-tag">
                          意图: {message.intent}
                        </Tag>
                      )}
                      <div className="message-text">
                        {message.role === 'assistant' ? (
                          <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {message.content}
                          </ReactMarkdown>
                        ) : (
                          message.content
                        )}
                        {message.loading && <span className="typing-cursor">▌</span>}
                      </div>
                      <div className="message-time">
                        {new Date(message.timestamp).toLocaleTimeString()}
                      </div>
                    </>
                  )}
                </div>
              </div>
            ))
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* 输入区域 */}
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
              <Popconfirm
                title="确定清空当前对话？"
                onConfirm={handleClearChat}
              >
                <Button
                  icon={<ReloadOutlined />}
                  disabled={messages.length === 0 || loading}
                >
                  清空
                </Button>
              </Popconfirm>
              {loading ? (
                <Button
                  danger
                  icon={<StopOutlined />}
                  onClick={handleStop}
                  className="stop-button"
                >
                  停止
                </Button>
              ) : (
                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={handleSend}
                  disabled={!inputValue.trim()}
                  className="send-button"
                >
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
