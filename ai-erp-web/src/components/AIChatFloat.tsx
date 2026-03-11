import { useState, useRef, useEffect } from 'react'
import { Input, Button, Spin, message, Avatar } from 'antd'
import { SendOutlined, RobotOutlined, UserOutlined, CloseOutlined, DeleteOutlined } from '@ant-design/icons'

const { TextArea } = Input

interface MessageItem {
  id: string
  role: 'user' | 'assistant'
  content: string
  loading?: boolean
  error?: boolean
}

export default function AIChatFloat() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState<MessageItem[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [sessionId] = useState(() => `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const getAuthHeaders = () => {
    const authStorage = localStorage.getItem('auth-storage')
    if (authStorage) {
      const { state } = JSON.parse(authStorage)
      if (state?.token) {
        return { 'Authorization': `Bearer ${state.token}` }
      }
    }
    return {}
  }

  const handleSend = async () => {
    if (!input.trim() || loading) return

    const userMessage = input.trim()
    setInput('')

    const userMsgId = `user-${Date.now()}`
    setMessages(prev => [...prev, {
      id: userMsgId,
      role: 'user',
      content: userMessage
    }])

    const assistantMsgId = `assistant-${Date.now()}`
    setMessages(prev => [...prev, {
      id: assistantMsgId,
      role: 'assistant',
      content: '',
      loading: true
    }])

    setLoading(true)

    // 构建历史消息
    const history = messages.map(m => ({
      role: m.role,
      content: m.content
    }))

    try {
      // 使用 fetch 发送 POST 请求，接收 SSE 流
      const response = await fetch('/api/v1/ai/chat/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders(),
        },
        body: JSON.stringify({
          message: userMessage,
          sessionId,
          history
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('No reader available')
      }

      const decoder = new TextDecoder()
      let accumulatedContent = ''

      // 移除 loading 状态
      setMessages(prev => prev.map(m =>
        m.id === assistantMsgId
          ? { ...m, loading: false }
          : m
      ))

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('event:message')) {
            // 下一个行是数据
            continue
          }
          if (line.startsWith('data:')) {
            const data = line.substring(5).trim()
            if (data === '[DONE]') {
              break
            }
            // 跳过 null 和空数据
            if (data && data !== 'null' && !data.startsWith('[ERROR]')) {
              accumulatedContent += data
              setMessages(prev => prev.map(m =>
                m.id === assistantMsgId
                  ? { ...m, content: accumulatedContent }
                  : m
              ))
            }
          }
          if (line.startsWith('event:error')) {
            // 错误事件
          }
        }
      }

    } catch (error: any) {
      message.error('发送失败: ' + (error.message || '网络错误'))
      setMessages(prev => prev.map(m =>
        m.id === assistantMsgId
          ? { ...m, content: '网络错误，请重试', loading: false, error: true }
          : m
      ))
    } finally {
      setLoading(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const handleClear = () => {
    setMessages([])
  }

  return (
    <>
      {/* 悬浮按钮 */}
      <div
        onClick={() => setOpen(!open)}
        style={{
          position: 'fixed',
          right: 24,
          bottom: 24,
          width: 56,
          height: 56,
          borderRadius: '50%',
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          boxShadow: '0 4px 12px rgba(102, 126, 234, 0.4)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          zIndex: 1000,
          transition: 'transform 0.2s, box-shadow 0.2s',
        }}
        onMouseEnter={e => {
          e.currentTarget.style.transform = 'scale(1.1)'
          e.currentTarget.style.boxShadow = '0 6px 16px rgba(102, 126, 234, 0.5)'
        }}
        onMouseLeave={e => {
          e.currentTarget.style.transform = 'scale(1)'
          e.currentTarget.style.boxShadow = '0 4px 12px rgba(102, 126, 234, 0.4)'
        }}
      >
        <RobotOutlined style={{ fontSize: 28, color: '#fff' }} />
      </div>

      {/* 对话框 */}
      {open && (
        <div
          style={{
            position: 'fixed',
            right: 24,
            bottom: 96,
            width: 380,
            height: 520,
            background: '#fff',
            borderRadius: 12,
            boxShadow: '0 8px 32px rgba(0,0,0,0.15)',
            display: 'flex',
            flexDirection: 'column',
            zIndex: 1000,
            overflow: 'hidden',
          }}
        >
          {/* 头部 */}
          <div
            style={{
              padding: '12px 16px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <RobotOutlined style={{ fontSize: 20 }} />
              <span style={{ fontSize: 16, fontWeight: 500 }}>AI 智能助手</span>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <Button
                type="text"
                icon={<DeleteOutlined />}
                size="small"
                style={{ color: '#fff' }}
                onClick={handleClear}
                title="清空对话"
              />
              <Button
                type="text"
                icon={<CloseOutlined />}
                size="small"
                style={{ color: '#fff' }}
                onClick={() => setOpen(false)}
              />
            </div>
          </div>

          {/* 消息区域 */}
          <div
            style={{
              flex: 1,
              overflow: 'auto',
              padding: 16,
              background: '#f5f5f5',
            }}
          >
            {messages.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#999', marginTop: 80 }}>
                <RobotOutlined style={{ fontSize: 48, marginBottom: 16, color: '#ddd' }} />
                <p>你好！我是 AI 助手</p>
                <p style={{ fontSize: 12 }}>有什么可以帮助你的吗？</p>
              </div>
            ) : (
              messages.map(msg => (
                <div
                  key={msg.id}
                  style={{
                    display: 'flex',
                    marginBottom: 12,
                    flexDirection: msg.role === 'user' ? 'row-reverse' : 'row',
                  }}
                >
                  <Avatar
                    size={32}
                    icon={msg.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                    style={{
                      backgroundColor: msg.role === 'user' ? '#1890ff' : '#722ed1',
                      flexShrink: 0,
                    }}
                  />
                  <div
                    style={{
                      maxWidth: '75%',
                      marginLeft: msg.role === 'user' ? 0 : 8,
                      marginRight: msg.role === 'user' ? 8 : 0,
                      padding: '8px 12px',
                      borderRadius: msg.role === 'user' ? '12px 12px 4px 12px' : '12px 12px 12px 4px',
                      background: msg.role === 'user' ? '#1890ff' : '#fff',
                      color: msg.role === 'user' ? '#fff' : '#333',
                      boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      fontSize: 14,
                    }}
                  >
                    {msg.loading ? (
                      <Spin size="small" />
                    ) : (
                      <span style={{ color: msg.error ? '#ff4d4f' : 'inherit' }}>
                        {msg.content}
                        {msg.role === 'assistant' && loading && (
                          <span style={{ display: 'inline-block', width: 2, height: 14, background: '#722ed1', marginLeft: 2, animation: 'blink 1s infinite' }} />
                        )}
                      </span>
                    )}
                  </div>
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* 输入区域 */}
          <div
            style={{
              padding: 12,
              background: '#fff',
              borderTop: '1px solid #f0f0f0',
              display: 'flex',
              gap: 8,
            }}
          >
            <TextArea
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="输入消息..."
              autoSize={{ minRows: 1, maxRows: 3 }}
              style={{ flex: 1, borderRadius: 20, resize: 'none' }}
              disabled={loading}
            />
            <Button
              type="primary"
              shape="circle"
              icon={<SendOutlined />}
              onClick={handleSend}
              loading={loading}
              style={{ background: '#722ed1', borderColor: '#722ed1' }}
            />
          </div>

          {/* 光标闪烁动画 */}
          <style>{`
            @keyframes blink {
              0%, 50% { opacity: 1; }
              51%, 100% { opacity: 0; }
            }
          `}</style>
        </div>
      )}
    </>
  )
}
