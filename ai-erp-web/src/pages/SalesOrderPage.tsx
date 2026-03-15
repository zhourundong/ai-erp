import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Descriptions } from 'antd'
import { PlusOutlined, EyeOutlined } from '@ant-design/icons'
import { salesOrderApi, customerApi } from '../services/api'
import type { ColumnsType } from 'antd/es/table'

interface SalesOrder {
  id: number
  orderNo: string
  orderDate: string
  customerId: number
  customerName: string
  totalAmount: number
  status: string
  paymentStatus: string
  salesName: string
  remark: string
}

interface Customer {
  id: number
  name: string
}

export default function SalesOrderPage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<SalesOrder[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [currentOrder, setCurrentOrder] = useState<SalesOrder | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await salesOrderApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取销售订单列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchCustomers = async () => {
    try {
      const response: any = await customerApi.list({ pageNum: 1, pageSize: 100 })
      setCustomers(response.data?.records || [])
    } catch (error) {
      console.error('获取客户失败', error)
    }
  }

  useEffect(() => {
    fetchData()
    fetchCustomers()
  }, [pageNum, pageSize])

  const handleAdd = () => {
    form.resetFields()
    setModalVisible(true)
  }

  const handleViewDetail = (record: SalesOrder) => {
    setCurrentOrder(record)
    setDetailVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await salesOrderApi.delete(id)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleSubmit = async (values: any) => {
    try {
      await salesOrderApi.create(values)
      message.success('创建成功')
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const statusMap: Record<string, { color: string; text: string }> = {
    DRAFT: { color: 'default', text: '草稿' },
    PENDING: { color: 'processing', text: '待确认' },
    CONFIRMED: { color: 'success', text: '已确认' },
    SHIPPED: { color: 'processing', text: '已发货' },
    COMPLETED: { color: 'success', text: '已完成' },
    CANCELLED: { color: 'error', text: '已取消' },
  }

  const paymentStatusMap: Record<string, { color: string; text: string }> = {
    UNPAID: { color: 'error', text: '未收款' },
    PARTIAL: { color: 'warning', text: '部分收款' },
    PAID: { color: 'success', text: '已收款' },
  }

  const columns: ColumnsType<SalesOrder> = [
    { title: '订单号', dataIndex: 'orderNo', width: 150 },
    { title: '订单日期', dataIndex: 'orderDate', width: 120 },
    { title: '客户', dataIndex: 'customerName', width: 150 },
    {
      title: '总金额',
      dataIndex: 'totalAmount',
      width: 120,
      render: (v) => v ? `¥${v}` : '-',
    },
    {
      title: '订单状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => {
        const { color, text } = statusMap[status] || { color: 'default', text: status }
        return <Tag color={color}>{text}</Tag>
      },
    },
    {
      title: '收款状态',
      dataIndex: 'paymentStatus',
      width: 100,
      render: (status) => {
        const { color, text } = paymentStatusMap[status] || { color: 'default', text: status }
        return <Tag color={color}>{text}</Tag>
      },
    },
    { title: '销售员', dataIndex: 'salesName', width: 100 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          {record.status === 'DRAFT' && (
            <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <Card
      title="销售订单"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增订单
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
        title="新增销售订单"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="customerId" label="客户" rules={[{ required: true }]}>
            <Select>
              {customers.map(c => (
                <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="expectedDeliveryDate" label="预计发货日期">
            <Input type="date" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="销售订单详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentOrder && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="订单号">{currentOrder.orderNo}</Descriptions.Item>
            <Descriptions.Item label="订单日期">{currentOrder.orderDate}</Descriptions.Item>
            <Descriptions.Item label="客户">{currentOrder.customerName}</Descriptions.Item>
            <Descriptions.Item label="销售员">{currentOrder.salesName}</Descriptions.Item>
            <Descriptions.Item label="总金额">
              {currentOrder.totalAmount ? `¥${currentOrder.totalAmount}` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="订单状态">
              <Tag color={statusMap[currentOrder.status]?.color}>
                {statusMap[currentOrder.status]?.text}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="收款状态">
              <Tag color={paymentStatusMap[currentOrder.paymentStatus]?.color}>
                {paymentStatusMap[currentOrder.paymentStatus]?.text}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="备注">{currentOrder.remark || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </Card>
  )
}
