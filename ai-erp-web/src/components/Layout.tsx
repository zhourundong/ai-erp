import { Layout, Menu, Avatar, Dropdown } from 'antd'
import { Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../stores/auth'
import AIChatFloat from './AIChatFloat'
import {
  SettingOutlined,
  ShoppingCartOutlined,
  DatabaseOutlined,
  AccountBookOutlined,
  TeamOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons'

const { Header, Sider, Content } = Layout

const menuItems = [
  { key: '/', icon: <SettingOutlined />, label: '仪表盘' },
  { key: '/purchase', icon: <ShoppingCartOutlined />, label: '采购管理' },
  { key: '/inventory', icon: <DatabaseOutlined />, label: '库存管理' },
  { key: '/finance', icon: <AccountBookOutlined />, label: '财务管理' },
  { key: '/hr', icon: <TeamOutlined />, label: '人事管理' },
  { key: '/system', icon: <SettingOutlined />, label: '系统设置' },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const userMenu = {
    items: [
      { key: 'profile', icon: <UserOutlined />, label: '个人中心' },
      { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: handleLogout },
    ],
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" width={200}>
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontSize: 18,
          fontWeight: 'bold',
        }}>
          AI ERP
        </div>
        <Menu
          theme="dark"
          mode="inline"
          items={menuItems}
          onClick={handleMenuClick}
          defaultSelectedKeys={['/']}
        />
      </Sider>
      <Layout>
        <Header style={{
          padding: '0 24px',
          background: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
        }}>
          <h3 style={{ margin: 0 }}>AI Native ERP v1.0</h3>
          <Dropdown menu={userMenu}>
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
              <Avatar icon={<UserOutlined />} />
              <span style={{ marginLeft: 8 }}>{user?.realName || '用户'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
      {/* AI 智能助手悬浮按钮 */}
      <AIChatFloat />
    </Layout>
  )
}
