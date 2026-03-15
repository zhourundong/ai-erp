import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Avatar, Dropdown, Button, Space, Badge } from 'antd'
import {
  MessageOutlined,
  ShopOutlined,
  SettingOutlined,
  BellOutlined,
  LogoutOutlined,
  UserOutlined,
  ShoppingOutlined,
  InboxOutlined,
  DollarOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../stores/authStore'
import './MainLayout.css'

const { Header, Sider, Content } = Layout

const menuItems = [
  {
    key: '/chat',
    icon: <MessageOutlined />,
    label: 'AI对话',
  },
  {
    key: 'procurement',
    icon: <ShoppingOutlined />,
    label: '采购管理',
    children: [
      { key: '/purchase-requests', label: '采购申请' },
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
      { key: '/warehouses', label: '仓库管理' },
      { key: '/products', label: '商品管理' },
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
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
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
    return [location.pathname]
  }

  // 获取默认展开的菜单组
  const getOpenKeys = () => {
    const pathParts = location.pathname.split('/').filter(Boolean)
    if (pathParts.length > 0) {
      // 根据路径返回父菜单
      const parentMenus: Record<string, string> = {
        'purchase-requests': 'procurement',
        'purchase-orders': 'procurement',
        'sales-orders': 'sales',
        'customers': 'partners',
        'suppliers': 'partners',
        'inventory': 'inventory',
        'warehouses': 'inventory',
        'products': 'inventory',
        'users': 'system',
        'organizations': 'system',
      }
      const parent = parentMenus[pathParts[0]]
      return parent ? [parent] : []
    }
    return []
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
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header className="main-header">
          <div className="header-left">
            <Button
              type="text"
              className="trigger"
              onClick={() => setCollapsed(!collapsed)}
            >
              <span className="trigger-icon">{collapsed ? '→' : '←'}</span>
            </Button>
          </div>
          <div className="header-right">
            <Space size={16}>
              <Badge count={3} size="small">
                <Button type="text" icon={<BellOutlined />} className="header-icon" />
              </Badge>
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
            </Space>
          </div>
        </Header>
        <Content className="main-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
