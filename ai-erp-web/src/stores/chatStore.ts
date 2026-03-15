import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
  intent?: string
  toolResult?: any
  loading?: boolean
  thinking?: string  // 思考过程
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
  pendingRequests: Map<string, { sessionId: string; messageId: string }> // messageId -> { sessionId, messageId }

  // 创建新会话
  createSession: () => string

  // 获取当前会话
  getCurrentSession: () => ChatSession | null

  // 获取指定会话
  getSession: (sessionId: string) => ChatSession | null

  // 切换会话
  switchSession: (sessionId: string) => void

  // 添加消息到当前会话
  addMessage: (message: Omit<Message, 'id' | 'timestamp'>) => string

  // 添加消息到指定会话
  addMessageToSession: (sessionId: string, message: Omit<Message, 'id' | 'timestamp'>) => string

  // 更新消息（当前会话）
  updateMessage: (messageId: string, updates: Partial<Message>) => void

  // 更新指定会话中的消息
  updateMessageInSession: (sessionId: string, messageId: string, updates: Partial<Message>) => void

  // 注册待处理请求
  registerPendingRequest: (messageId: string, sessionId: string) => void

  // 取消请求并更新消息
  cancelPendingRequest: (messageId: string) => void

  // 获取待处理的请求
  getPendingRequest: (messageId: string) => { sessionId: string; messageId: string } | undefined

  // 删除会话
  deleteSession: (sessionId: string) => void

  // 清空当前会话消息
  clearCurrentSession: () => void

  // 更新会话标题
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
                      ? message.content.slice(0, 20) + (message.content.length > 20 ? '...' : '')
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

      registerPendingRequest: (messageId, sessionId) => {
        const { pendingRequests } = get()
        pendingRequests.set(messageId, { sessionId, messageId })
      },

      cancelPendingRequest: (messageId) => {
        const { pendingRequests } = get()
        const request = pendingRequests.get(messageId)
        if (request) {
          // 更新消息状态为已取消
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
