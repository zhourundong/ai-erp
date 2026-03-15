import axios from 'axios'
import { useAuthStore } from '../stores/authStore'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const data = response.data
    // 检查业务状态码
    if (data.code && data.code !== 200) {
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
    }
    return Promise.reject(error.response?.data || error)
  }
)

export default api

// 认证相关API
export const authApi = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
  validate: () => api.get('/auth/validate'),
  me: () => api.get('/auth/me'),
}

// 用户管理API
export const userApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/users', { params }),
  get: (id: number) => api.get(`/users/${id}`),
  create: (data: any) => api.post('/users', data),
  update: (id: number, data: any) => api.put(`/users/${id}`, data),
  delete: (id: number) => api.delete(`/users/${id}`),
  resetPassword: (id: number, newPassword: string) =>
    api.post(`/users/${id}/reset-password`, null, { params: { newPassword } }),
}

// 组织管理API
export const organizationApi = {
  tree: () => api.get('/organizations/tree'),
  get: (id: number) => api.get(`/organizations/${id}`),
  create: (data: any) => api.post('/organizations', data),
  update: (id: number, data: any) => api.put(`/organizations/${id}`, data),
  delete: (id: number) => api.delete(`/organizations/${id}`),
}

// 商品管理API
export const productApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string; categoryId?: number }) =>
    api.get('/products', { params }),
  get: (id: number) => api.get(`/products/${id}`),
  create: (data: any) => api.post('/products', data),
  update: (id: number, data: any) => api.put(`/products/${id}`, data),
  delete: (id: number) => api.delete(`/products/${id}`),
}

// 供应商管理API
export const supplierApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/suppliers', { params }),
  get: (id: number) => api.get(`/suppliers/${id}`),
  create: (data: any) => api.post('/suppliers', data),
  update: (id: number, data: any) => api.put(`/suppliers/${id}`, data),
  delete: (id: number) => api.delete(`/suppliers/${id}`),
  recommend: (params?: { category?: string; minScore?: number }) =>
    api.post('/suppliers/recommend', null, { params }),
}

// 客户管理API
export const customerApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/customers', { params }),
  get: (id: number) => api.get(`/customers/${id}`),
  create: (data: any) => api.post('/customers', data),
  update: (id: number, data: any) => api.put(`/customers/${id}`, data),
  delete: (id: number) => api.delete(`/customers/${id}`),
}

// 仓库管理API
export const warehouseApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/warehouses', { params }),
  get: (id: number) => api.get(`/warehouses/${id}`),
  create: (data: any) => api.post('/warehouses', data),
  update: (id: number, data: any) => api.put(`/warehouses/${id}`, data),
  delete: (id: number) => api.delete(`/warehouses/${id}`),
}

// 库存管理API
export const inventoryApi = {
  list: (params: { pageNum: number; pageSize: number; warehouseId?: number; productId?: number }) =>
    api.get('/inventory', { params }),
  get: (warehouseId: number, productId: number) => api.get(`/inventory/${warehouseId}/${productId}`),
  stockIn: (data: { warehouseId: number; productId: number; quantity: number; costPrice?: number; productSku?: string; productName?: string; transactionType?: string; operator?: string }) =>
    api.post('/inventory/stock-in', data),
  stockOut: (data: { warehouseId: number; productId: number; quantity: number; unitPrice?: number; transactionType?: string; operator?: string }) =>
    api.post('/inventory/stock-out', data),
  transactions: (params: { pageNum: number; pageSize: number; warehouseId?: number; productId?: number; transactionType?: string }) =>
    api.get('/inventory/transactions', { params }),
}

// 采购申请API
export const purchaseRequestApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/purchase/requests', { params }),
  get: (id: number) => api.get(`/purchase/requests/${id}`),
  getItems: (id: number) => api.get(`/purchase/requests/${id}/items`),
  create: (data: any) => api.post('/purchase/requests', data),
  update: (id: number, data: any) => api.put(`/purchase/requests/${id}`, data),
  delete: (id: number) => api.delete(`/purchase/requests/${id}`),
  submit: (id: number) => api.post(`/purchase/requests/${id}/submit`),
  approve: (id: number, data: { approverId: number; approverName: string; comment?: string }) =>
    api.post(`/purchase/requests/${id}/approve`, null, { params: data }),
  reject: (id: number, data: { approverId: number; approverName: string; comment?: string }) =>
    api.post(`/purchase/requests/${id}/reject`, null, { params: data }),
  createOrder: (id: number, data: { supplierId: number; supplierName: string; buyerId?: number; buyerName?: string }) =>
    api.post(`/purchase/requests/${id}/create-order`, data),
}

// 采购订单API
export const purchaseOrderApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/purchase/orders', { params }),
  get: (id: number) => api.get(`/purchase/orders/${id}`),
  getItems: (id: number) => api.get(`/purchase/orders/${id}/items`),
  create: (data: any) => api.post('/purchase/orders', data),
  update: (id: number, data: any) => api.put(`/purchase/orders/${id}`, data),
  delete: (id: number) => api.delete(`/purchase/orders/${id}`),
  submit: (id: number) => api.post(`/purchase/orders/${id}/submit`),
  approve: (id: number) => api.post(`/purchase/orders/${id}/approve`),
  reject: (id: number) => api.post(`/purchase/orders/${id}/reject`),
  unapprove: (id: number) => api.post(`/purchase/orders/${id}/unapprove`),
  receive: (id: number, data: { warehouseId: number; items: { itemId: number; quantity: number }[]; operator?: string }) =>
    api.post(`/purchase/orders/${id}/receive`, data),
}

// 销售订单API
export const salesOrderApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; status?: string }) =>
    api.get('/sales/orders', { params }),
  get: (id: number) => api.get(`/sales/orders/${id}`),
  getItems: (id: number) => api.get(`/sales/orders/${id}/items`),
  create: (data: any) => api.post('/sales/orders', data),
  update: (id: number, data: any) => api.put(`/sales/orders/${id}`, data),
  delete: (id: number) => api.delete(`/sales/orders/${id}`),
  confirm: (id: number, warehouseId: number) =>
    api.post(`/sales/orders/${id}/confirm`, null, { params: { warehouseId } }),
  unconfirm: (id: number) => api.post(`/sales/orders/${id}/unconfirm`),
  cancel: (id: number) => api.post(`/sales/orders/${id}/cancel`),
  ship: (id: number, data: { warehouseId: number; items: { itemId: number; quantity: number }[]; operator?: string }) =>
    api.post(`/sales/orders/${id}/ship`, data),
}

// AI对话API
export const chatApi = {
  // 同步请求
  send: (message: string, sessionId?: string, history?: any[], signal?: AbortSignal) =>
    api.post('/chat', { message, sessionId, history }, { signal }),

  // 流式请求（SSE）
  stream: (
    message: string,
    sessionId: string | undefined,
    history: any[],
    onToken: (token: string) => void,
    onComplete: (response: any) => void,
    onError: (error: Error) => void,
    signal?: AbortSignal,
    onThinking?: (thinking: string) => void
  ) => {
    const eventSource = new EventSourcePolyfill('/api/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${useAuthStore.getState().token}`,
      },
      body: JSON.stringify({ message, sessionId, history }),
      signal,
    })

    // 处理思考过程事件
    eventSource.addEventListener('thinking', (event: Event) => {
      const customEvent = event as CustomEvent<string>
      const thinking = customEvent.detail || (customEvent as any).data
      if (onThinking && thinking) {
        onThinking(thinking)
      }
    })

    eventSource.addEventListener('token', (event: Event) => {
      const customEvent = event as CustomEvent<string>
      onToken(customEvent.detail || (customEvent as any).data)
    })

    eventSource.addEventListener('complete', (event: Event) => {
      const customEvent = event as CustomEvent<string>
      const data = customEvent.detail || (customEvent as any).data
      try {
        const response = JSON.parse(data)
        onComplete(response)
      } catch (e) {
        onComplete({ content: data })
      }
      eventSource.close()
    })

    eventSource.addEventListener('error', (event: Event) => {
      const customEvent = event as CustomEvent<string>
      const data = customEvent.detail || (customEvent as any).data
      if (data) {
        onError(new Error(data))
      } else if (signal?.aborted) {
        onError(new Error('请求已取消'))
      } else {
        onError(new Error('连接错误'))
      }
      eventSource.close()
    })

    return () => eventSource.close()
  },
}

// EventSource polyfill for POST requests
class EventSourcePolyfill extends EventTarget {
  private xhr: XMLHttpRequest
  private closed = false
  private completed = false
  private timeoutId: ReturnType<typeof setTimeout> | null = null
  // 跨数据包的解析状态
  private pendingEvent = ''
  private pendingDataLines: string[] = []
  private pendingLine = ''

  constructor(
    url: string,
    options: {
      method?: string
      headers?: Record<string, string>
      body?: string
      signal?: AbortSignal
      timeout?: number
    } = {}
  ) {
    super()

    this.xhr = new XMLHttpRequest()
    this.xhr.open(options.method || 'GET', url, true)

    // 设置headers
    if (options.headers) {
      for (const [key, value] of Object.entries(options.headers)) {
        this.xhr.setRequestHeader(key, value)
      }
    }

    // 处理abort signal
    if (options.signal) {
      options.signal.addEventListener('abort', () => {
        this.cleanup()
        this.xhr.abort()
        this.closed = true
      })
    }

    // 设置超时（默认10分钟，与后端5分钟超时配合）
    const timeout = options.timeout || 10 * 60 * 1000
    this.timeoutId = setTimeout(() => {
      if (!this.closed && !this.completed) {
        this.cleanup()
        this.closed = true
        this.dispatchEvent(new CustomEvent('error', { detail: '连接超时' }))
      }
    }, timeout)

    let buffer = ''

    this.xhr.onreadystatechange = () => {
      if (this.xhr.readyState === XMLHttpRequest.HEADERS_RECEIVED) {
        // 检查HTTP状态码
        if (this.xhr.status !== 200) {
          this.cleanup()
          this.closed = true
          const errorMsg = `HTTP错误: ${this.xhr.status}`
          const event = new CustomEvent('error', { detail: errorMsg })
          Object.defineProperty(event, 'data', { value: errorMsg })
          this.dispatchEvent(event)
        }
      }
    }

    this.xhr.onprogress = () => {
      if (this.closed) return

      const responseText = this.xhr.responseText
      const newContent = responseText.slice(buffer.length)
      buffer = responseText

      // 解析SSE事件 - 使用实例变量保存跨数据包状态
      // 将上一个数据包可能不完整的行与本次新内容合并
      const fullContent = this.pendingLine + newContent
      const lines = fullContent.split('\n')

      // 最后一行可能不完整，先保存起来
      this.pendingLine = lines.pop() || ''

      for (let i = 0; i < lines.length; i++) {
        const line = lines[i]

        if (line.startsWith('event:')) {
          this.pendingEvent = line.slice(6).trim()
          console.log('[SSE解析] event:', this.pendingEvent)
        } else if (line.startsWith('data:')) {
          // 累积 data 行
          const dataContent = line.slice(5)
          this.pendingDataLines.push(dataContent)
          console.log('[SSE解析] data行:', dataContent)
        } else if (line === '' && (this.pendingEvent || this.pendingDataLines.length > 0)) {
          // 空行表示事件结束
          const currentData = this.pendingDataLines.join('\n')

          // 派发事件
          console.log('[SSE解析] 派发事件:', this.pendingEvent || 'message', '数据长度:', currentData.length)

          // 如果收到complete事件，标记为已完成
          if (this.pendingEvent === 'complete') {
            this.completed = true
          }

          // 派发事件
          const eventName = this.pendingEvent || 'message'
          const event = new CustomEvent(eventName, { detail: currentData })
          Object.defineProperty(event, 'data', { value: currentData })
          this.dispatchEvent(event)

          // 重置状态
          this.pendingEvent = ''
          this.pendingDataLines = []
        }
      }
    }

    this.xhr.onload = () => {
      this.cleanup()
      if (this.closed) return
      // 检查HTTP状态码和完成状态
      if (this.xhr.status !== 200) {
        // 已经在 onreadystatechange 中处理过，这里不再重复
        return
      }
      // 正常完成但没有收到complete事件，可能是数据传输问题
      if (!this.completed && buffer.length > 0) {
        // 尝试处理缓冲区中剩余的数据
        const event = new CustomEvent('complete', { detail: buffer })
        Object.defineProperty(event, 'data', { value: buffer })
        this.dispatchEvent(event)
      }
    }

    this.xhr.onerror = () => {
      this.cleanup()
      if (this.closed) return
      const errorMsg = '网络连接错误'
      const event = new CustomEvent('error', { detail: errorMsg })
      Object.defineProperty(event, 'data', { value: errorMsg })
      this.dispatchEvent(event)
    }

    this.xhr.ontimeout = () => {
      this.cleanup()
      if (this.closed) return
      const errorMsg = '请求超时'
      const event = new CustomEvent('error', { detail: errorMsg })
      Object.defineProperty(event, 'data', { value: errorMsg })
      this.dispatchEvent(event)
    }

    // 设置XHR超时
    this.xhr.timeout = timeout

    this.xhr.send(options.body || null)
  }

  private cleanup() {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId)
      this.timeoutId = null
    }
  }

  close() {
    this.cleanup()
    this.closed = true
    this.xhr.abort()
  }
}
