import { create } from 'zustand'
import { persist } from 'zustand/middleware'

// 工具执行记录
export interface ToolExecution {
  name: string
  arguments: Record<string, any>
  result: string
}

// 导航动作事件
export interface ActionEvent {
  action: 'navigate' | 'openCreateForm' | 'openModal'
  path?: string
  filter?: Record<string, any>
  formType?: string
  recordType?: string
  recordId?: number
  requiresConfirmation: boolean
  confirmText?: string
  description?: string
}

// 流式事件 - 按顺序存储
export interface StreamEvent {
  type: 'text' | 'tool' | 'action'  // 文字、工具调用或导航动作
  content?: string       // 文字内容（type='text'时）
  tool?: ToolExecution   // 工具信息（type='tool'时）
  action?: ActionEvent   // 导航动作（type='action'时）
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
  unreadCount: number  // 未读消息数
}

interface ChatState {
  sessions: ChatSession[]
  currentSessionId: string | null
  pendingRequests: Map<string, { sessionId: string; messageId: string }>
  chatOpen: boolean  // 聊天窗口是否打开

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
  appendActionToMessage: (sessionId: string, messageId: string, action: ActionEvent) => void
  registerPendingRequest: (messageId: string, sessionId: string) => void
  cancelPendingRequest: (messageId: string) => void
  getPendingRequest: (messageId: string) => { sessionId: string; messageId: string } | undefined
  deleteSession: (sessionId: string) => void
  clearCurrentSession: () => void
  updateSessionTitle: (sessionId: string, title: string) => void
  setChatOpen: (open: boolean) => void
  markCurrentSessionAsRead: () => void
  getTotalUnreadCount: () => number
}

export const useChatStore = create<ChatState>()(
  persist(
    (set, get) => ({
      sessions: [],
      currentSessionId: null,
      pendingRequests: new Map(),
      chatOpen: false,

      createSession: () => {
        const sessionId = `session_${Date.now()}`
        const newSession: ChatSession = {
          id: sessionId,
          title: '新对话',
          messages: [],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          unreadCount: 0,
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
        const { chatOpen } = get()

        set((state) => {
          // 查找当前消息
          const session = state.sessions.find(s => s.id === sessionId)
          const message = session?.messages.find(m => m.id === messageId)

          // 检查是否是 AI 回复完成（loading 从 true 变为 false）
          const isAIResponseComplete =
            message?.role === 'assistant' &&
            message?.loading === true &&
            updates.loading === false

          // 如果聊天窗口关闭且是 AI 回复完成，增加未读计数
          const shouldIncreaseUnread = !chatOpen && isAIResponseComplete

          return {
            sessions: state.sessions.map((session) =>
              session.id === sessionId
                ? {
                    ...session,
                    messages: session.messages.map((msg) =>
                      msg.id === messageId ? { ...msg, ...updates } : msg
                    ),
                    unreadCount: shouldIncreaseUnread
                      ? (session.unreadCount || 0) + 1
                      : (session.unreadCount || 0),
                  }
                : session
            ),
          }
        })
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

      // 追加导航动作到消息（流式）
      appendActionToMessage: (sessionId, messageId, action) => {
        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === sessionId
              ? {
                  ...session,
                  messages: session.messages.map((msg) => {
                    if (msg.id !== messageId) return msg
                    const events = [...(msg.events || [])]
                    events.push({ type: 'action', action })
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

      setChatOpen: (open) => {
        set({ chatOpen: open })
        // 打开聊天窗口时，清除当前会话的未读计数
        if (open) {
          const { currentSessionId } = get()
          if (currentSessionId) {
            set((state) => ({
              sessions: state.sessions.map((session) =>
                session.id === currentSessionId ? { ...session, unreadCount: 0 } : session
              ),
            }))
          }
        }
      },

      markCurrentSessionAsRead: () => {
        const { currentSessionId } = get()
        if (!currentSessionId) return
        set((state) => ({
          sessions: state.sessions.map((session) =>
            session.id === currentSessionId ? { ...session, unreadCount: 0 } : session
          ),
        }))
      },

      getTotalUnreadCount: () => {
        const { sessions } = get()
        return sessions.reduce((total, session) => total + (session.unreadCount || 0), 0)
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
