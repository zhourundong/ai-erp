import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { warehouseApi } from '../services/api'
import type { ColumnsType } from 'antd/es/table'

interface Warehouse {
  id: number
  code: string
  name: string
  type: string
  manager: string
  phone: string
  address: string
  status: string
}

export default function WarehousePage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<Warehouse[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await warehouseApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取仓库列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [pageNum, pageSize])

  const handleAdd = () => {
    setEditingWarehouse(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: Warehouse) => {
    setEditingWarehouse(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await warehouseApi.delete(id)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingWarehouse) {
        await warehouseApi.update(editingWarehouse.id, values)
        message.success('更新成功')
      } else {
        await warehouseApi.create(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const typeMap: Record<string, string> = {
    MAIN: '主仓库',
    NORMAL: '普通仓库',
    TEMP: '临时仓库',
  }

  const columns: ColumnsType<Warehouse> = [
    { title: '编码', dataIndex: 'code', width: 100 },
    { title: '仓库名称', dataIndex: 'name', width: 150 },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
      render: (type) => typeMap[type] || type,
    },
    { title: '负责人', dataIndex: 'manager', width: 100 },
    { title: '联系电话', dataIndex: 'phone', width: 120 },
    { title: '地址', dataIndex: 'address', ellipsis: true },
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
      title="仓库管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增仓库
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
        title={editingWarehouse ? '编辑仓库' : '新增仓库'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="仓库名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="仓库类型">
            <Select>
              <Select.Option value="MAIN">主仓库</Select.Option>
              <Select.Option value="NORMAL">普通仓库</Select.Option>
              <Select.Option value="TEMP">临时仓库</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="manager" label="负责人">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input />
          </Form.Item>
          <Form.Item name="address" label="地址">
            <Input.TextArea rows={2} />
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
