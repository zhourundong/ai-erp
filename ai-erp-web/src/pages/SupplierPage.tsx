import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, message, Popconfirm, Rate, Select } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { supplierApi } from '../services/api'
import type { ColumnsType } from 'antd/es/table'

interface Supplier {
  id: number
  code: string
  name: string
  contactPerson: string
  phone: string
  email: string
  address: string
  categories: string
  aiScore: number
  transactionCount: number
  status: string
  createdAt: string
}

export default function SupplierPage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<Supplier[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await supplierApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取供应商列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize])

  const handleAdd = () => {
    setEditingSupplier(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: Supplier) => {
    setEditingSupplier(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await supplierApi.delete(id)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingSupplier) {
        await supplierApi.update(editingSupplier.id, values)
        message.success('更新成功')
      } else {
        await supplierApi.create(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const columns: ColumnsType<Supplier> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '编码', dataIndex: 'code', width: 120 },
    { title: '名称', dataIndex: 'name', width: 180 },
    { title: '联系人', dataIndex: 'contactPerson', width: 100 },
    { title: '电话', dataIndex: 'phone', width: 130 },
    {
      title: 'AI评分',
      dataIndex: 'aiScore',
      width: 150,
      render: (score) => (
        <Rate disabled allowHalf value={(score || 0) / 20} />
      ),
    },
    {
      title: '交易次数',
      dataIndex: 'transactionCount',
      width: 100,
      render: (count) => <Tag color="blue">{count || 0}次</Tag>,
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
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Card
      title="供应商管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增供应商
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
        title={editingSupplier ? '编辑供应商' : '新增供应商'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="供应商名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="address" label="地址">
            <Input />
          </Form.Item>
          <Form.Item name="categories" label="供货品类">
            <Input placeholder="多个品类用逗号分隔" />
          </Form.Item>
          <Form.Item name="bankAccount" label="银行账户">
            <Input />
          </Form.Item>
          <Form.Item name="bankName" label="开户行">
            <Input />
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
