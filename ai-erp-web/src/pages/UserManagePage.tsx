import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons'
import { userApi } from '../services/api'
import type { ColumnsType } from 'antd/es/table'

interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  role: string
  status: string
  organizationName: string
  createdAt: string
}

export default function UserManagePage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<User[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await userApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize])

  const handleAdd = () => {
    setEditingUser(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: User) => {
    setEditingUser(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await userApi.delete(id)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleResetPassword = async (id: number) => {
    Modal.confirm({
      title: '重置密码',
      content: '确定要将该用户的密码重置为 "123456" 吗？',
      onOk: async () => {
        try {
          await userApi.resetPassword(id, '123456')
          message.success('密码已重置为 123456')
        } catch (error) {
          message.error('重置密码失败')
        }
      },
    })
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingUser) {
        await userApi.update(editingUser.id, values)
        message.success('更新成功')
      } else {
        await userApi.create(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const columns: ColumnsType<User> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '用户名', dataIndex: 'username', width: 120 },
    { title: '姓名', dataIndex: 'realName', width: 120 },
    { title: '邮箱', dataIndex: 'email', width: 180 },
    { title: '手机', dataIndex: 'phone', width: 130 },
    {
      title: '角色',
      dataIndex: 'role',
      width: 100,
      render: (role) => {
        const colorMap: Record<string, string> = {
          ADMIN: 'red',
          BUYER: 'blue',
          APPROVER: 'green',
          VIEWER: 'default',
        }
        const textMap: Record<string, string> = {
          ADMIN: '管理员',
          BUYER: '采购员',
          APPROVER: '审批人',
          VIEWER: '查看者',
        }
        return <Tag color={colorMap[role]}>{textMap[role] || role}</Tag>
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (status) => (
        <Tag color={status === 'ACTIVE' ? 'success' : 'default'}>
          {status === 'ACTIVE' ? '启用' : '停用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button type="link" size="small" icon={<KeyOutlined />} onClick={() => handleResetPassword(record.id)}>
            重置密码
          </Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Card
      title="用户管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增用户
        </Button>
      }
    >
      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (page, size) => {
            setPageNum(page)
            setPageSize(size)
          },
        }}
      />

      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input disabled={!!editingUser} />
          </Form.Item>
          <Form.Item name="realName" label="姓名" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {!editingUser && (
            <Form.Item name="password" label="密码" rules={[{ required: !editingUser }]}>
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机">
            <Input />
          </Form.Item>
          <Form.Item name="role" label="角色" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="ADMIN">管理员</Select.Option>
              <Select.Option value="BUYER">采购员</Select.Option>
              <Select.Option value="APPROVER">审批人</Select.Option>
              <Select.Option value="VIEWER">查看者</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select>
              <Select.Option value="ACTIVE">启用</Select.Option>
              <Select.Option value="INACTIVE">停用</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
