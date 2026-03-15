import { create } from 'zustand'

export interface Tab {
  key: string        // 唯一标识，如 '/products'
  label: string      // 显示名称，如 '商品管理'
  closable: boolean  // 是否可关闭（首页不可关闭）
}

// 页面路径到名称的映射
export const pageLabels: Record<string, string> = {
  '/': '首页',
  '/purchase-orders': '采购订单',
  '/sales-orders': '销售订单',
  '/inventory': '库存查询',
  '/inventory-transactions': '库存流水',
  '/suppliers': '供应商管理',
  '/customers': '客户管理',
  '/products': '商品管理',
  '/warehouses': '仓库管理',
  '/users': '用户管理',
  '/organizations': '组织管理',
}

// 详情页路径映射（详情路径 -> 列表路径）
const detailPathMapping: Record<string, string> = {
  '/purchase-orders': '/purchase-orders',
  '/sales-orders': '/sales-orders',
  '/inventory': '/inventory',
  '/suppliers': '/suppliers',
  '/customers': '/customers',
  '/products': '/products',
}

// 待打开的详情信息
export interface PendingDetail {
  type: 'view' | 'create'  // 查看详情 或 创建新记录
  path: string       // 原始路径，如 '/purchase-orders/2'
  basePath: string   // 基础路径，如 '/purchase-orders'
  id?: string        // 详情 ID，如 '2'（仅 view 类型）
  formType?: string  // 表单类型（仅 create 类型）
}

interface TabState {
  tabs: Tab[]
  activeKey: string
  pendingDetail: PendingDetail | null  // 待打开的详情
  detailVersion: number  // 用于触发更新的版本号
  openTab: (key: string, label: string) => void
  openTabByPath: (path: string) => void  // 通过路径自动打开 Tab
  openCreateForm: (basePath: string, formType?: string) => void  // 打开创建表单
  closeTab: (key: string) => void
  setActiveTab: (key: string) => void
  consumePendingDetail: () => PendingDetail | null  // 消费待打开的详情
}

// 解析路径，判断是否为详情路径
function parsePath(path: string): { basePath: string; id: string | null } {
  const parts = path.split('/').filter(Boolean)

  // 检查是否是详情路径（如 /purchase-orders/2）
  if (parts.length === 2) {
    const potentialBase = '/' + parts[0]
    const potentialId = parts[1]

    // 检查是否是数字 ID
    if (/^\d+$/.test(potentialId) && detailPathMapping[potentialBase]) {
      return { basePath: potentialBase, id: potentialId }
    }
  }

  return { basePath: path, id: null }
}

export const useTabStore = create<TabState>((set, get) => ({
  tabs: [
    { key: '/', label: '首页', closable: false }
  ],
  activeKey: '/',
  pendingDetail: null,
  detailVersion: 0,

  openTab: (key: string, label: string) => {
    const { tabs } = get()
    const existingTab = tabs.find(tab => tab.key === key)

    if (existingTab) {
      // Tab 已存在，切换到该 Tab
      set({ activeKey: key })
    } else {
      // 添加新 Tab
      set({
        tabs: [...tabs, { key, label, closable: true }],
        activeKey: key
      })
    }
  },

  openTabByPath: (path: string) => {
    const { basePath, id } = parsePath(path)
    const label = pageLabels[basePath] || basePath

    // 打开 Tab
    get().openTab(basePath, label)

    // 如果是详情路径，设置待打开的详情，并递增版本号
    if (id) {
      set({
        pendingDetail: {
          type: 'view',
          path,
          basePath,
          id
        },
        detailVersion: get().detailVersion + 1
      })
    }
  },

  openCreateForm: (basePath: string, formType?: string) => {
    const label = pageLabels[basePath] || basePath

    // 打开 Tab
    get().openTab(basePath, label)

    // 设置待打开的创建表单
    set({
      pendingDetail: {
        type: 'create',
        path: basePath,
        basePath,
        formType
      },
      detailVersion: get().detailVersion + 1
    })
  },

  closeTab: (key: string) => {
    const { tabs, activeKey } = get()

    // 首页不可关闭
    if (key === '/') return

    const tabIndex = tabs.findIndex(tab => tab.key === key)
    const newTabs = tabs.filter(tab => tab.key !== key)

    // 如果关闭的是当前激活的 Tab，切换到前一个 Tab
    let newActiveKey = activeKey
    if (activeKey === key && newTabs.length > 0) {
      // 优先切换到后一个，没有则切换到前一个
      const newIndex = Math.min(tabIndex, newTabs.length - 1)
      newActiveKey = newTabs[newIndex].key
    }

    set({ tabs: newTabs, activeKey: newActiveKey })
  },

  setActiveTab: (key: string) => {
    set({ activeKey: key })
  },

  consumePendingDetail: () => {
    const { pendingDetail } = get()
    if (pendingDetail) {
      set({ pendingDetail: null })
    }
    return pendingDetail
  }
}))
