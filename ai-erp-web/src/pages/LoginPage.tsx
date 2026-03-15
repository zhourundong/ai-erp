import { useState } from 'react'
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'
import { useAuthStore } from '../stores/authStore'
import './LoginPage.css'

export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { setAuth } = useAuthStore()

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const response: any = await authApi.login(values.username, values.password)
      setAuth(response.token, response.user)
      message.success('登录成功')
      navigate('/')
    } catch (error: any) {
      message.error(error.message || '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-background">
        <div className="login-shape login-shape-1"></div>
        <div className="login-shape login-shape-2"></div>
      </div>
      <Card className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <span className="logo-icon">🤖</span>
            <span className="logo-text">AI-ERP</span>
          </div>
          <h1>AI原生企业资源规划系统</h1>
          <p>智能驱动 · 高效协作 · 创新体验</p>
        </div>
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
          initialValues={{ username: 'admin', password: 'admin123' }}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名: admin"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码: admin123"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className="login-button"
            >
              登录
            </Button>
          </Form.Item>
        </Form>
        <div className="login-footer">
          <p>默认账号: admin / admin123</p>
        </div>
      </Card>
    </div>
  )
}
