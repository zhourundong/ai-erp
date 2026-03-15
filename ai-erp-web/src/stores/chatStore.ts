import { create } from 'zustand'
import { persist } from 'zustand/middleware'

// 工具执行记录
export interface ToolExecution {
  name: string
  arguments: Record<string, any>
  result: string
}

// 流式事件 - 按顺序存储
export interface StreamEvent {
  type: 'text' | 'tool'  // 文字或工具调用
  content?: string       // 文字内容（type='text'时）
  tool?: ToolExecution   // 工具信息（type='tool'时）
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string           // 完整文字内容（用于搜索、复制等）
  events?: StreamEvent[]    // 按顺序的事件列表（文字+工具调用）
  timestamp: string
  intent?: string
  loading?: boolean
  thinking?: string         // 思考过程
  thinkingTimeMs?: number   // 思考耗时(毫秒)
  processingTimeMs?: number // 总响应耗时(毫秒)
}

export interface ChatSession {
  id: string
  title: string
  messages: Message[]
  createdAt: string
  updatedAt: string
}

interface ChatState {
  sessions: ChatSession[]
  currentSessionId: string | null
  pendingRequests: Map<string, { sessionId: string; messageId: string }>

  createSession: () => string
  getCurrentSession: () => ChatSession | null
  getSession: (sessionId: string) => ChatSession | null
  switchSession: (sessionId: string) => void
  addMessage: (message: Omit<Message, 'id' | 'timestamp'>) => string
  addMessageToSession: (sessionId: string, message: Omit<Message, 'id' | 'timestamp'>) => string
  updateMessage: (messageId: string, updates: Partial<Message>) => void
  updateMessageInSession: (sessionId: string, messageId: string, updates: Partial<Message>) => void
  appendTextToMessage: (sessionId: string, messageId: string, text: string) => void
  appendToolToMessage: (sessionId: string, messageId: string, tool: ToolExecution) => void
  registerPendingRequest: (messageId: string, sessionId: string) => void
  cancelPendingRequest: (messageId: string) => void
  getPendingRequest: (messageId: string) => { sessionId: string; messageId: string } | undefined
  deleteSession: (sessionId: string) => void
  clearCurrentSession: () => void
  updateSessionTitle: (sessionId: string, title: string) => void
}

export const useChatStore = create<ChatState>()(
  persist(
    (set, get) => ({
      sessions: [],
      currentSessionId: null,
      pendingRequests: new Map(),

      createSession: () => {
        const sessionId = `session_${Date.now()}`
        const newSession: ChatSession = {
          id: sessionId,
          title: '新对话',
          messages: [],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        }
        set((state) => ({
          sessions: [newSession, ...state.sessions],
          currentSessionId: sessionId,
        }))
        return sessionId
      },

      getCurrentSession: () => {
        const { sessions, currentSessionId } = get()
        return sessions.find((s) => s.id === currentSessionId) || null
      },

      getSession: (sessionId) => {
        const { sessions } = get()
        return sessions.find((s) => s.id === sessionId) || null
      },

      switchSession: (sessionId) => {
        set({ currentSessionId: sessionId })
      },

      addMessage: (message) => {
        const { currentSessionId } = get()
        if (!currentSessionId) return ''
        return get().addMessageToSession(currentSessionId, message)
      },

      addMessageToSession: (sessionId, message) => {
        const newMessage: Message = {
          ...message,
          id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
          timestamp: new Date().toISOString(),
          events: message.events || [],
          content: message.content || '',
        }

        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === sessionId
              ? {
                  ...session,
                  messages: [...session.messages, newMessage],
                  updatedAt: new Date().toISOString(),
                  title:
                    session.messages.length === 0 && message.role === 'user'
                      ? (message.content || '').slice(0, 20) + ((message.content || '').length > 20 ? '...' : '')
                      : session.title,
                }
              : session
          ),
        }))

        return newMessage.id
      },

      updateMessage: (messageId, updates) => {
        const { currentSessionId } = get()
        if (!currentSessionId) return
        get().updateMessageInSession(currentSessionId, messageId, updates)
      },

      updateMessageInSession: (sessionId, messageId, updates) => {
        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === sessionId
              ? {
                  ...session,
                  messages: session.messages.map((msg) =>
                    msg.id === messageId ? { ...msg, ...updates } : msg
                  ),
                }
              : session
          ),
        }))
      },

      // 追加文字到消息（流式）
      appendTextToMessage: (sessionId, messageId, text) => {
        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === sessionId
              ? {
                  ...session,
                  messages: session.messages.map((msg) => {
                    if (msg.id !== messageId) return msg
                    // 检查最后一个事件是否是文字，如果是则合并
                    const events = [...(msg.events || [])]
                    if (events.length > 0 && events[events.length - 1].type === 'text') {
                      events[events.length - 1] = {
                        ...events[events.length - 1],
                        content: (events[events.length - 1].content || '') + text
                      }
                    } else {
                      events.push({ type: 'text', content: text })
                    }
                    return {
                      ...msg,
                      content: (msg.content || '') + text,
                      events
                    }
                  }),
                }
              : session
          ),
        }))
      },

      // 追加工具调用到消息（流式）
      appendToolToMessage: (sessionId, messageId, tool) => {
        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === sessionId
              ? {
                  ...session,
                  messages: session.messages.map((msg) => {
                    if (msg.id !== messageId) return msg
                    const events = [...(msg.events || [])]
                    events.push({ type: 'tool', tool })
                    return { ...msg, events }
                  }),
                }
              : session
          ),
        }))
      },

      registerPendingRequest: (messageId, sessionId) => {
        const { pendingRequests } = get()
        pendingRequests.set(messageId, { sessionId, messageId })
      },

      cancelPendingRequest: (messageId) => {
        const { pendingRequests } = get()
        const request = pendingRequests.get(messageId)
        if (request) {
          get().updateMessageInSession(request.sessionId, messageId, {
            content: '已停止生成',
            loading: false,
          })
          pendingRequests.delete(messageId)
        }
      },

      getPendingRequest: (messageId) => {
        return get().pendingRequests.get(messageId)
      },

      deleteSession: (sessionId) => {
        set((state) => {
          const newSessions = state.sessions.filter((s) => s.id !== sessionId)
          const newCurrentId =
            state.currentSessionId === sessionId
              ? newSessions[0]?.id || null
              : state.currentSessionId
          return { sessions: newSessions, currentSessionId: newCurrentId }
        })
      },

      clearCurrentSession: () => {
        const { currentSessionId } = get()
        if (!currentSessionId) return

        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === currentSessionId
              ? { ...session, messages: [], title: '新对话', updatedAt: new Date().toISOString() }
              : session
          ),
        }))
      },

      updateSessionTitle: (sessionId, title) => {
        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === sessionId ? { ...session, title } : session
          ),
        }))
      },
    }),
    {
      name: 'ai-erp-chat-storage',
      partialize: (state) => ({
        sessions: state.sessions,
        currentSessionId: state.currentSessionId,
      }),
    }
  )
)
