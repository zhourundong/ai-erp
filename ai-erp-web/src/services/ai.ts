import api from './api'

export interface ChatRequest {
  message: string
  sessionId?: string
  model?: string
  history?: Message[]
}

export interface Message {
  role: 'user' | 'assistant'
  content: string
}

export interface ChatResponse {
  sessionId: string
  content: string | null
  model: string | null
  tokensUsed: number | null
  success: boolean
  errorMessage: string | null
}

export interface Conversation {
  id: number
  tenantId: number
  userId: number
  sessionId: string
  role: string
  content: string
  model: string | null
  tokensUsed: number
  createdTime: string
}

export const aiApi = {
  chat: (data: ChatRequest) =>
    api.post<ChatResponse>('/ai/chat', data),

  getHistory: (sessionId: string) =>
    api.get<Conversation[]>(`/ai/chat/history/${sessionId}`),

  getModels: () =>
    api.get<{ available: boolean; models: string[] }>('/ai/models'),
}
