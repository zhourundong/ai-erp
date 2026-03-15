import { useState, lazy, Suspense } from 'react'
import { Layout, Menu, Avatar, Dropdown, Button, Tabs, Spin } from 'antd'
import {
  ShopOutlined,
  SettingOutlined,
  LogoutOutlined,
  UserOutlined,
  ShoppingOutlined,
  InboxOutlined,
  DollarOutlined,
  DatabaseOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../stores/authStore'
import { useTabStore, pageLabels } from '../stores/tabStore'
import FloatChat from '../components/FloatChat'
import './MainLayout.css'

const { Header, Sider, Content } = Layout

// 懒加载页面组件
const pageComponents: Record<string, React.LazyExoticComponent<React.FC>> = {
  '/': lazy(() => import('../pages/DashboardPage')),
  '/users': lazy(() => import('../pages/UserManagePage')),
  '/organizations': lazy(() => import('../pages/OrganizationPage')),
  '/products': lazy(() => import('../pages/ProductPage')),
  '/suppliers': lazy(() => import('../pages/SupplierPage')),
  '/customers': lazy(() => import('../pages/CustomerPage')),
  '/warehouses': lazy(() => import('../pages/WarehousePage')),
  '/inventory': lazy(() => import('../pages/InventoryPage')),
  '/inventory-transactions': lazy(() => import('../pages/InventoryTransactionPage')),
  '/purchase-orders': lazy(() => import('../pages/PurchaseOrderPage')),
  '/sales-orders': lazy(() => import('../pages/SalesOrderPage')),
}

// 菜单配置（包含 label 用于 Tab 显示）
const menuItems = [
  {
    key: '/',
    icon: <HomeOutlined />,
    label: '首页',
  },
  {
    key: 'procurement',
    icon: <ShoppingOutlined />,
    label: '采购管理',
    children: [
      { key: '/purchase-orders', label: '采购订单' },
    ],
  },
  {
    key: 'sales',
    icon: <DollarOutlined />,
    label: '销售管理',
    children: [
      { key: '/sales-orders', label: '销售订单' },
    ],
  },
  {
    key: 'inventory',
    icon: <InboxOutlined />,
    label: '库存管理',
    children: [
      { key: '/inventory', label: '库存查询' },
      { key: '/inventory-transactions', label: '库存流水' },
    ],
  },
  {
    key: 'partners',
    icon: <ShopOutlined />,
    label: '合作伙伴',
    children: [
      { key: '/suppliers', label: '供应商管理' },
      { key: '/customers', label: '客户管理' },
    ],
  },
  {
    key: 'basic',
    icon: <DatabaseOutlined />,
    label: '基础资料',
    children: [
      { key: '/products', label: '商品管理' },
      { key: '/warehouses', label: '仓库管理' },
    ],
  },
  {
    key: 'system',
    icon: <SettingOutlined />,
    label: '系统管理',
    children: [
      { key: '/users', label: '用户管理' },
      { key: '/organizations', label: '组织管理' },
    ],
  },
]

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const { user, logout } = useAuthStore()
  const { tabs, activeKey, openTab, closeTab, setActiveTab } = useTabStore()

  // 处理菜单点击
  const handleMenuClick = ({ key }: { key: string }) => {
    if (key.startsWith('/')) {
      openTab(key, pageLabels[key] || key)
    }
  }

  // 处理 Tab 切换
  const handleTabChange = (key: string) => {
    setActiveTab(key)
  }

  // 处理 Tab 关闭
  const handleTabClose = (targetKey: string) => {
    closeTab(targetKey)
  }

  const handleLogout = () => {
    logout()
    window.location.href = '/login'
  }

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '设置',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
      onClick: handleLogout,
    },
  ]

  // 获取当前选中的菜单项
  const getSelectedKeys = () => {
    return [activeKey]
  }

  // 获取默认展开的菜单组
  const getOpenKeys = () => {
    const parentMenus: Record<string, string> = {
      'purchase-orders': 'procurement',
      'sales-orders': 'sales',
      'customers': 'partners',
      'suppliers': 'partners',
      'inventory': 'inventory',
      'inventory-transactions': 'inventory',
      'warehouses': 'basic',
      'products': 'basic',
      'users': 'system',
      'organizations': 'system',
    }
    const key = activeKey.replace('/', '')
    const parent = parentMenus[key]
    return parent ? [parent] : []
  }

  return (
    <Layout className="main-layout">
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        className="main-sider"
        width={240}
      >
        <div className="logo">
          <span className="logo-icon">🤖</span>
          {!collapsed && <span className="logo-text">AI-ERP</span>}
        </div>
        <div className="sider-menu-wrapper">
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={getSelectedKeys()}
            defaultOpenKeys={getOpenKeys()}
            items={menuItems}
            onClick={handleMenuClick}
          />
        </div>
        <div className="sider-footer">
          <Button
            type="text"
            className="collapse-trigger"
            onClick={() => setCollapsed(!collapsed)}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </Button>
        </div>
      </Sider>
      <Layout className={collapsed ? 'ant-layout-has-sider-collapsed' : ''}>
        <Header className="main-header">
          <div className="header-left">
            {/* 标题或面包屑 */}
          </div>
          <div className="header-right">
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div className="user-info">
                <Avatar
                  style={{ backgroundColor: '#667eea' }}
                  icon={<UserOutlined />}
                >
                  {user?.realName?.[0] || user?.username?.[0]}
                </Avatar>
                <span className="user-name">{user?.realName || user?.username}</span>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content className="main-content">
          {/* Tab 页签区域 */}
          <Tabs
            type="editable-card"
            hideAdd
            activeKey={activeKey}
            onChange={handleTabChange}
            onEdit={(targetKey, action) => {
              if (action === 'remove') {
                handleTabClose(targetKey as string)
              }
            }}
            items={tabs.map(tab => ({
              key: tab.key,
              label: tab.label,
              closable: tab.closable,
              children: (
                <Suspense
                  fallback={
                    <div className="page-loading">
                      <Spin size="large" />
                    </div>
                  }
                >
                  {(() => {
                    const PageComponent = pageComponents[tab.key]
                    return PageComponent ? <PageComponent /> : <div>页面不存在</div>
                  })()}
                </Suspense>
              )
            }))}
            className="main-tabs"
          />
        </Content>
      </Layout>
      {/* 悬浮聊天窗口 */}
      <FloatChat />
    </Layout>
  )
}
