import { useState, useRef, useEffect, useCallback } from 'react'
import { Badge, Button, Input, Avatar, Spin, Card, Tag, Collapse, Drawer, Popconfirm, Tooltip } from 'antd'
import {
  RobotOutlined,
  UserOutlined,
  SendOutlined,
  StopOutlined,
  BulbOutlined,
  ToolOutlined,
  CheckCircleOutlined,
  ExportOutlined,
  CloseOutlined,
  MessageOutlined,
  PlusOutlined,
  DeleteOutlined,
  HistoryOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
} from '@ant-design/icons'
import ReactMarkdown, { Components } from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { chatApi, ActionEvent } from '../services/api'
import { useAuthStore } from '../stores/authStore'
import { useChatStore, StreamEvent, ToolExecution, ChatSession } from '../stores/chatStore'
import { useTabStore } from '../stores/tabStore'
import './FloatChat.css'

// 存储当前请求的 AbortController
const abortControllers = new Map<string, AbortController>()

// Markdown 自定义组件配置 - 包装表格使其可滚动
const markdownComponents: Components = {
  table: ({ children }) => (
    <div style={{ width: '100%', overflowX: 'auto', margin: '8px 0' }}>
      <table style={{ width: 'max-content', minWidth: '100%' }}>{children}</table>
    </div>
  ),
  pre: ({ children }) => (
    <pre style={{ maxWidth: '100%', overflowX: 'auto', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>{children}</pre>
  ),
  code: ({ className, children, ...props }) => {
    const isInline = !className
    return isInline ? (
      <code style={{ wordBreak: 'break-all' }} {...props}>{children}</code>
    ) : (
      <code {...props}>{children}</code>
    )
  },
  a: ({ href, children }) => (
    <a href={href} target="_blank" rel="noopener noreferrer" style={{ wordBreak: 'break-all' }}>{children}</a>
  ),
}

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
  // 优先显示中文名称
  const displayName = tool.chineseName || tool.name

  return (
    <Card size="small" className="tool-card-inline">
      <div className="tool-header-inline">
        <ToolOutlined style={{ color: '#52c41a' }} />
        <span className="tool-name">{displayName}</span>
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

// 导航动作卡片
function ActionCard({ action, onExecute }: { action: ActionEvent; onExecute: () => void }) {
  return (
    <Card size="small" className="action-card" style={{ marginTop: 8, marginBottom: 8 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <ExportOutlined style={{ color: '#1890ff' }} />
          <span>{action.description || '导航到目标页面'}</span>
        </div>
        <Button type="primary" size="small" onClick={onExecute}>
          {action.confirmText || '前往'}
        </Button>
      </div>
    </Card>
  )
}

// 渲染流式事件列表
function StreamEventsDisplay({ events, isLoading, onAction }: {
  events: StreamEvent[];
  isLoading?: boolean;
  onAction?: (action: ActionEvent) => void
}) {
  if (!events || events.length === 0) return null

  return (
    <div className="stream-events">
      {events.map((event, index) => {
        if (event.type === 'tool' && event.tool) {
          return <ToolCard key={`tool-${index}`} tool={event.tool} />
        } else if (event.type === 'action' && event.action) {
          return (
            <ActionCard
              key={`action-${index}`}
              action={event.action}
              onExecute={() => onAction?.(event.action!)}
            />
          )
        } else if (event.type === 'text' && event.content) {
          return (
            <ReactMarkdown key={`text-${index}`} remarkPlugins={[remarkGfm]} components={markdownComponents}>
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
function ThinkingCollapse({ thinking, isLoading, thinkingTimeMs }: {
  thinking: string;
  isLoading?: boolean;
  thinkingTimeMs?: number
}) {
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
              <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>{thinking}</ReactMarkdown>
            </div>
          ),
        },
      ]}
    />
  )
}

// 历史会话列表组件
function SessionList({
  sessions,
  currentSessionId,
  onSelect,
  onDelete,
  showHistory
}: {
  sessions: ChatSession[];
  currentSessionId: string | null;
  onSelect: (sessionId: string) => void;
  onDelete: (sessionId: string) => void;
  showHistory: boolean;
}) {
  const listRef = useRef<HTMLDivElement>(null)

  // 当历史面板打开时，滚动到最底部
  useEffect(() => {
    if (showHistory && listRef.current && sessions.length > 0) {
      // 使用 setTimeout 确保 DOM 已渲染
      setTimeout(() => {
        if (listRef.current) {
          listRef.current.scrollTop = listRef.current.scrollHeight
        }
      }, 50)
    }
  }, [showHistory, sessions])

  if (sessions.length === 0) return null

  return (
    <div className="session-list-float" ref={listRef}>
      {sessions.map((session) => (
        <div
          key={session.id}
          className={`session-item-float ${session.id === currentSessionId ? 'active' : ''}`}
          onClick={() => onSelect(session.id)}
        >
          <div className="session-info">
            <div className="session-title-float">{session.title}</div>
            <div className="session-time-float">
              {new Date(session.updatedAt).toLocaleDateString()}
            </div>
          </div>
          <Popconfirm
            title="确定删除这个对话？"
            onConfirm={(e) => {
              e?.stopPropagation()
              onDelete(session.id)
            }}
            onCancel={(e) => e?.stopPropagation()}
          >
            <Button
              type="text"
              size="small"
              icon={<DeleteOutlined />}
              className="session-delete-float"
              onClick={(e) => e.stopPropagation()}
            />
          </Popconfirm>
        </div>
      ))}
    </div>
  )
}

export default function FloatChat() {
  const [open, setOpen] = useState(false)
  const [showHistory, setShowHistory] = useState(false)
  const [fullscreen, setFullscreen] = useState(false)
  const [inputValue, setInputValue] = useState('')
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const { user } = useAuthStore()
  const { openTabByPath, openCreateForm } = useTabStore()
  const {
    sessions,
    currentSessionId,
    getCurrentSession,
    switchSession,
    deleteSession,
    getTotalUnreadCount,
    setChatOpen,
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

  // 打开聊天窗口时滚动到底部
  useEffect(() => {
    if (open) {
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    }
  }, [open])

  useEffect(() => {
    const state = useChatStore.getState()
    if (state.sessions.length === 0) {
      state.createSession()
    } else if (!state.currentSessionId) {
      state.switchSession(state.sessions[0].id)
    }
  }, [])

  // 同步聊天窗口打开状态到 store
  useEffect(() => {
    setChatOpen(open)
  }, [open])

  // 执行导航动作
  const executeAction = useCallback((action: ActionEvent) => {
    if (action.action === 'openCreateForm' && action.path) {
      // 解析路径，去掉 ?create=true 参数，获取 basePath
      const basePath = action.path.split('?')[0]
      // 打开创建表单
      openCreateForm(basePath, action.formType)
      setOpen(false)
    } else if (action.path) {
      // 导航到页面
      openTabByPath(action.path)
      setOpen(false)
    }
  }, []) // 依赖为空，openCreateForm 和 openTabByPath 是 zustand store 的稳定函数

  // 新建对话
  const handleNewSession = () => {
    const state = useChatStore.getState()
    // 检查当前会话是否为空
    const current = state.getCurrentSession()
    if (current && current.messages.length === 0) {
      setShowHistory(false)
      return
    }
    // 检查最新会话是否为空
    const latestSession = state.sessions[0]
    if (latestSession && latestSession.messages.length === 0) {
      state.switchSession(latestSession.id)
      setShowHistory(false)
      return
    }
    // 创建新会话
    state.createSession()
    setShowHistory(false)
  }

  // 切换会话
  const handleSwitchSession = (sessionId: string) => {
    switchSession(sessionId)
    setShowHistory(false)
  }

  // 删除会话
  const handleDeleteSession = (sessionId: string) => {
    deleteSession(sessionId)
  }

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
      (token: string) => {
        const decodedToken = decodeBase64(token)
        store.appendTextToMessage(sessionId, loadingMsgId, decodedToken)
      },
      (response: any) => {
        store.updateMessageInSession(sessionId, loadingMsgId, {
          loading: false,
          thinkingTimeMs: response.thinkingTimeMs,
          processingTimeMs: response.processingTimeMs,
        })
        abortControllers.delete(loadingMsgId)
      },
      (error: Error) => {
        if (abortController.signal.aborted) return
        store.updateMessageInSession(sessionId, loadingMsgId, {
          content: error.message || '抱歉，发生了错误，请稍后重试。',
          loading: false,
        })
        abortControllers.delete(loadingMsgId)
      },
      abortController.signal,
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
      (toolExecution: ToolExecution) => {
        store.appendToolToMessage(sessionId, loadingMsgId, toolExecution)
      },
      (action: ActionEvent) => {
        store.appendActionToMessage(sessionId, loadingMsgId, action)
        if (!action.requiresConfirmation) {
          executeAction(action)
        }
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

  // 抽屉标题栏
  const drawerTitle = (
    <div className="drawer-title-bar">
      <div className="drawer-title-left">
        <RobotOutlined style={{ color: 'white' }} />
        <span>AI 助手</span>
      </div>
      <div className="drawer-title-actions">
        <Tooltip title={fullscreen ? "退出全屏" : "全屏"}>
          <Button
            type="text"
            size="small"
            icon={fullscreen ? <FullscreenExitOutlined style={{ color: 'white' }} /> : <FullscreenOutlined style={{ color: 'white' }} />}
            onClick={() => setFullscreen(!fullscreen)}
          />
        </Tooltip>
        <Tooltip title="历史对话">
          <Button
            type="text"
            size="small"
            icon={<HistoryOutlined style={{ color: 'white' }} />}
            onClick={() => setShowHistory(!showHistory)}
            className={showHistory ? 'active' : ''}
          />
        </Tooltip>
        <Tooltip title="新对话">
          <Button
            type="text"
            size="small"
            icon={<PlusOutlined style={{ color: 'white' }} />}
            onClick={handleNewSession}
          />
        </Tooltip>
      </div>
    </div>
  )

  // 悬浮按钮
  const unreadCount = getTotalUnreadCount()
  const floatButton = (
    <div className="float-chat-button" onClick={() => setOpen(true)}>
      <Badge count={unreadCount} size="small">
        <Button
          type="primary"
          shape="circle"
          size="large"
          icon={<MessageOutlined />}
          className="float-button"
        />
      </Badge>
    </div>
  )

  return (
    <>
      {!open && floatButton}

      <Drawer
        title={drawerTitle}
        placement="right"
        width={fullscreen ? 'calc(100vw - 240px)' : 420}
        onClose={() => {
          setOpen(false)
          setFullscreen(false)
        }}
        open={open}
        className={`float-chat-drawer ${fullscreen ? 'fullscreen' : ''}`}
        closable={true}
        closeIcon={<CloseOutlined style={{ color: 'white' }} />}
        styles={{
          body: { display: 'flex', flexDirection: 'column', padding: 0 },
        }}
      >
        {/* 历史会话列表 */}
        {showHistory && (
          <div className="history-panel">
            <div className="history-header">
              <span>历史对话</span>
              <Badge count={sessions.length} style={{ backgroundColor: '#8b5cf6' }} />
            </div>
            <SessionList
              sessions={sessions}
              currentSessionId={currentSessionId}
              onSelect={handleSwitchSession}
              onDelete={handleDeleteSession}
              showHistory={showHistory}
            />
          </div>
        )}

        {/* 消息区域 */}
        <div className="float-chat-messages">
          {messages.length === 0 ? (
            <div className="float-chat-empty">
              <div className="empty-icon">🤖</div>
              <h3>您好，{user?.realName || user?.username}！</h3>
              <p>我是您的AI助手，有什么可以帮您的吗？</p>
              <div className="quick-actions-float">
                <div className="quick-action-item" onClick={() => setInputValue('打开采购订单列表')}>
                  📋 查看采购订单
                </div>
                <div className="quick-action-item" onClick={() => setInputValue('查询库存情况')}>
                  📊 库存查询
                </div>
                <div className="quick-action-item" onClick={() => setInputValue('创建销售订单')}>
                  📝 创建销售订单
                </div>
                <div className="quick-action-item" onClick={() => setInputValue('推荐优质供应商')}>
                  🏪 推荐供应商
                </div>
              </div>
            </div>
          ) : (
            messages.map((message) => (
              <div key={message.id} className={`float-message ${message.role}`}>
                <div className="message-avatar-float">
                  <Avatar
                    size={32}
                    style={{ backgroundColor: message.role === 'user' ? '#667eea' : '#8b5cf6' }}
                    icon={message.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                  >
                    {message.role === 'user' ? user?.realName?.[0] || user?.username?.[0] : 'AI'}
                  </Avatar>
                </div>
                <div className="message-body">
                  {message.loading && !message.content && !message.thinking && !(message.events?.length) ? (
                    <div className="message-loading-float">
                      <Spin size="small" />
                      <span>AI正在思考中...</span>
                    </div>
                  ) : (
                    <>
                      {message.thinking && (
                        <ThinkingCollapse
                          thinking={message.thinking}
                          isLoading={message.loading}
                          thinkingTimeMs={message.thinkingTimeMs}
                        />
                      )}
                      {message.events && message.events.length > 0 ? (
                        <div className="message-text-float">
                          <StreamEventsDisplay
                            events={message.events}
                            isLoading={message.loading}
                            onAction={executeAction}
                          />
                          {message.role === 'assistant' && !message.loading && (
                            <div className="message-meta-float">
                              <span>{new Date(message.timestamp).toLocaleTimeString()}</span>
                              {message.processingTimeMs && (
                                <span className="meta-duration">
                                  {formatDuration(message.processingTimeMs)}
                                </span>
                              )}
                            </div>
                          )}
                        </div>
                      ) : (
                        <div className="message-text-float">
                          <div className="message-content-inner">
                            <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
                              {message.content}
                            </ReactMarkdown>
                            {message.loading && <span className="typing-cursor">▌</span>}
                          </div>
                          {message.role === 'assistant' && !message.loading && (
                            <div className="message-meta-float">
                              <span>{new Date(message.timestamp).toLocaleTimeString()}</span>
                              {message.processingTimeMs && (
                                <span className="meta-duration">
                                  {formatDuration(message.processingTimeMs)}
                                </span>
                              )}
                            </div>
                          )}
                        </div>
                      )}
                    </>
                  )}
                </div>
              </div>
            ))
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* 输入区域 */}
        <div className="float-chat-input">
          <Input.TextArea
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="输入消息... (Shift+Enter换行)"
            autoSize={{ minRows: 1, maxRows: 3 }}
            className="chat-input-float"
          />
          <div className="input-actions">
            {loading ? (
              <Button type="primary" danger icon={<StopOutlined />} onClick={handleStop}>
                停止
              </Button>
            ) : (
              <Button type="primary" icon={<SendOutlined />} onClick={handleSend} disabled={!inputValue.trim()}>
                发送
              </Button>
            )}
          </div>
        </div>
      </Drawer>
    </>
  )
}
