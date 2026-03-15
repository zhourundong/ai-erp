import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Descriptions, InputNumber, Divider } from 'antd'
import { PlusOutlined, EyeOutlined, CheckOutlined, ExportOutlined, MinusOutlined, EditOutlined } from '@ant-design/icons'
import { salesOrderApi, customerApi, warehouseApi, productApi } from '../services/api'
import { useAuthStore } from '../stores/authStore'
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

interface SalesOrderItem {
  id: number
  productId: number
  productSku: string
  productName: string
  specification: string
  quantity: number
  unit: string
  unitPrice: number
  shippedQty: number
  status: string
  shipQty?: number // 本次发货数量
}

interface Customer {
  id: number
  name: string
}

interface Warehouse {
  id: number
  name: string
}

interface Product {
  id: number
  sku: string
  name: string
  specification: string
  unit: string
  salePrice: number
}

interface OrderItemInput {
  key: string
  productId: number
  productSku: string
  productName: string
  specification: string
  quantity: number
  unit: string
  unitPrice: number
}

export default function SalesOrderPage() {
  const { user } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<SalesOrder[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [shipVisible, setShipVisible] = useState(false)
  const [currentOrder, setCurrentOrder] = useState<SalesOrder | null>(null)
  const [orderItems, setOrderItems] = useState<SalesOrderItem[]>([])
  const [shipForm] = Form.useForm()
  const [form] = Form.useForm()
  const [orderItemInputs, setOrderItemInputs] = useState<OrderItemInput[]>([])
  const [itemKeyCounter, setItemKeyCounter] = useState(0)

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

  const fetchWarehouses = async () => {
    try {
      const response: any = await warehouseApi.list({ pageNum: 1, pageSize: 100 })
      setWarehouses(response.data?.records || [])
    } catch (error) {
      console.error('获取仓库失败', error)
    }
  }

  const fetchProducts = async () => {
    try {
      const response: any = await productApi.list({ pageNum: 1, pageSize: 200 })
      setProducts(response.data?.records || [])
    } catch (error) {
      console.error('获取商品失败', error)
    }
  }

  useEffect(() => {
    fetchData()
    fetchCustomers()
    fetchWarehouses()
    fetchProducts()
  }, [pageNum, pageSize])

  const handleAdd = () => {
    form.resetFields()
    setOrderItemInputs([])
    setItemKeyCounter(0)
    setEditMode(false)
    setCurrentOrder(null)
    setModalVisible(true)
  }

  // 编辑订单
  const handleEdit = async (record: SalesOrder) => {
    setCurrentOrder(record)
    try {
      const response: any = await salesOrderApi.getItems(record.id)
      const items = (response.data || []).map((item: SalesOrderItem, index: number) => ({
        key: `edit_${index}`,
        productId: item.productId,
        productSku: item.productSku,
        productName: item.productName,
        specification: item.specification,
        quantity: item.quantity,
        unit: item.unit,
        unitPrice: item.unitPrice,
      }))
      setOrderItemInputs(items)
      setItemKeyCounter(items.length)
      form.setFieldsValue({
        customerId: record.customerId,
        remark: record.remark,
      })
      setEditMode(true)
      setModalVisible(true)
    } catch (error) {
      message.error('获取订单明细失败')
    }
  }

  const handleViewDetail = async (record: SalesOrder) => {
    setCurrentOrder(record)
    try {
      const response: any = await salesOrderApi.getItems(record.id)
      setOrderItems(response.data || [])
    } catch (error) {
      setOrderItems([])
    }
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

  // 添加商品明细
  const handleAddItem = () => {
    const newItem: OrderItemInput = {
      key: `new_${itemKeyCounter}`,
      productId: 0,
      productSku: '',
      productName: '',
      specification: '',
      quantity: 1,
      unit: '',
      unitPrice: 0,
    }
    setOrderItemInputs([...orderItemInputs, newItem])
    setItemKeyCounter(itemKeyCounter + 1)
  }

  // 删除商品明细
  const handleRemoveItem = (key: string) => {
    setOrderItemInputs(orderItemInputs.filter(item => item.key !== key))
  }

  // 选择商品时自动填充信息
  const handleProductSelect = (key: string, productId: number) => {
    const product = products.find(p => p.id === productId)
    if (product) {
      const newItems = orderItemInputs.map(item => {
        if (item.key === key) {
          return {
            ...item,
            productId: product.id,
            productSku: product.sku,
            productName: product.name,
            specification: product.specification || '',
            unit: product.unit || '件',
            unitPrice: product.salePrice || 0,
          }
        }
        return item
      })
      setOrderItemInputs(newItems)
    }
  }

  // 更新明细数量或单价
  const handleItemChange = (key: string, field: string, value: number) => {
    const newItems = orderItemInputs.map(item => {
      if (item.key === key) {
        return { ...item, [field]: value }
      }
      return item
    })
    setOrderItemInputs(newItems)
  }

  const handleSubmit = async (values: any) => {
    if (orderItemInputs.length === 0) {
      message.warning('请添加订单明细')
      return
    }

    // 校验明细
    const invalidItems = orderItemInputs.filter(item => !item.productId || item.quantity <= 0)
    if (invalidItems.length > 0) {
      message.warning('请完善订单明细（选择商品并填写数量）')
      return
    }

    const customer = customers.find(c => c.id === values.customerId)

    try {
      if (editMode && currentOrder) {
        // 编辑模式
        await salesOrderApi.update(currentOrder.id, {
          customerId: values.customerId,
          customerName: customer?.name || '',
          remark: values.remark,
          items: orderItemInputs.map(item => ({
            productId: item.productId,
            productSku: item.productSku,
            productName: item.productName,
            specification: item.specification,
            quantity: item.quantity,
            unit: item.unit,
            unitPrice: item.unitPrice,
          })),
        })
        message.success('修改成功')
      } else {
        // 新增模式
        await salesOrderApi.create({
          customerId: values.customerId,
          customerName: customer?.name || '',
          remark: values.remark,
          items: orderItemInputs.map(item => ({
            productId: item.productId,
            productSku: item.productSku,
            productName: item.productName,
            specification: item.specification,
            quantity: item.quantity,
            unit: item.unit,
            unitPrice: item.unitPrice,
          })),
        })
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  // 确认订单
  const handleConfirm = async (record: SalesOrder) => {
    const defaultWarehouseId = warehouses.find(w => w.name === '主仓库')?.id || warehouses[0]?.id
    if (!defaultWarehouseId) {
      message.error('请先创建仓库')
      return
    }
    Modal.confirm({
      title: '确认订单',
      content: '确认订单将检查库存是否充足，是否继续？',
      onOk: async () => {
        try {
          await salesOrderApi.confirm(record.id, defaultWarehouseId)
          message.success('订单已确认')
          fetchData()
        } catch (error: any) {
          message.error(error.message || '确认失败，可能是库存不足')
        }
      },
    })
  }

  // 取消订单
  const handleCancel = async (id: number) => {
    Modal.confirm({
      title: '取消订单',
      content: '确定要取消此销售订单吗？',
      onOk: async () => {
        try {
          await salesOrderApi.cancel(id)
          message.success('订单已取消')
          fetchData()
        } catch (error: any) {
          message.error(error.message || '取消失败')
        }
      },
    })
  }

  // 反确认
  const handleUnconfirm = async (id: number) => {
    Modal.confirm({
      title: '反确认',
      content: '反确认后订单将变为草稿状态，确定继续？',
      onOk: async () => {
        try {
          await salesOrderApi.unconfirm(id)
          message.success('反确认成功')
          fetchData()
        } catch (error: any) {
          message.error(error.message || '操作失败')
        }
      },
    })
  }

  // 打开发货出库弹窗
  const handleOpenShip = async (record: SalesOrder) => {
    setCurrentOrder(record)
    try {
      const response: any = await salesOrderApi.getItems(record.id)
      const items = (response.data || []).map((item: SalesOrderItem) => ({
        ...item,
        shipQty: item.quantity - (item.shippedQty || 0), // 默认填入未发货数量
      }))
      setOrderItems(items)
      shipForm.setFieldsValue({
        warehouseId: warehouses.find(w => w.name === '主仓库')?.id || warehouses[0]?.id,
      })
    } catch (error) {
      setOrderItems([])
    }
    setShipVisible(true)
  }

  // 执行发货出库
  const handleShip = async (values: any) => {
    const items = orderItems
      .filter(item => (item.shipQty || 0) > 0)
      .map(item => ({
        itemId: item.id,
        quantity: item.shipQty || 0,
      }))

    if (items.length === 0) {
      message.warning('请输入发货数量')
      return
    }

    try {
      await salesOrderApi.ship(currentOrder!.id, {
        warehouseId: values.warehouseId,
        items,
        operator: user?.realName || user?.username || '系统',
      })
      message.success('发货出库成功')
      setShipVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '发货出库失败')
    }
  }

  const statusMap: Record<string, { color: string; text: string }> = {
    DRAFT: { color: 'default', text: '草稿' },
    CONFIRMED: { color: 'success', text: '已确认' },
    SHIPPING: { color: 'processing', text: '发货中' },
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
      width: 280,
      render: (_, record) => (
        <Space size="small" wrap>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          {record.status === 'DRAFT' && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
                编辑
              </Button>
              <Button type="link" size="small" icon={<CheckOutlined />} onClick={() => handleConfirm(record)}>
                确认
              </Button>
              <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
                <Button type="link" size="small" danger>
                  删除
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'CONFIRMED' && (
            <>
              <Button type="link" size="small" icon={<ExportOutlined />} onClick={() => handleOpenShip(record)}>
                发货
              </Button>
              <Button type="link" size="small" danger onClick={() => handleUnconfirm(record.id)}>
                反确认
              </Button>
              <Button type="link" size="small" danger onClick={() => handleCancel(record.id)}>
                取消
              </Button>
            </>
          )}
          {record.status === 'SHIPPING' && (
            <Button type="link" size="small" icon={<ExportOutlined />} onClick={() => handleOpenShip(record)}>
              继续发货
            </Button>
          )}
        </Space>
      ),
    },
  ]

  // 计算总金额
  const totalAmount = orderItemInputs.reduce((sum, item) => sum + (item.quantity * item.unitPrice || 0), 0)

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
        title={editMode ? '编辑销售订单' : '新增销售订单'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={900}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="customerId" label="客户" rules={[{ required: true }]}>
            <Select placeholder="请选择客户">
              {customers.map(c => (
                <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>

        <Divider>订单明细</Divider>

        <Button type="dashed" onClick={handleAddItem} icon={<PlusOutlined />} style={{ marginBottom: 16 }}>
          添加商品
        </Button>

        <Table
          dataSource={orderItemInputs}
          rowKey="key"
          size="small"
          pagination={false}
          columns={[
            {
              title: '商品',
              width: 200,
              render: (_, record) => (
                <Select
                  placeholder="选择商品"
                  showSearch
                  optionFilterProp="children"
                  style={{ width: '100%' }}
                  value={record.productId || undefined}
                  onChange={(value) => handleProductSelect(record.key, value)}
                >
                  {products.map(p => (
                    <Select.Option key={p.id} value={p.id}>{p.name} ({p.sku})</Select.Option>
                  ))}
                </Select>
              ),
            },
            { title: '规格', dataIndex: 'specification', width: 100 },
            {
              title: '数量',
              width: 100,
              render: (_, record) => (
                <InputNumber
                  min={1}
                  value={record.quantity}
                  onChange={(v) => handleItemChange(record.key, 'quantity', v || 1)}
                />
              ),
            },
            { title: '单位', dataIndex: 'unit', width: 60 },
            {
              title: '单价',
              width: 100,
              render: (_, record) => (
                <InputNumber
                  min={0}
                  precision={2}
                  value={record.unitPrice}
                  onChange={(v) => handleItemChange(record.key, 'unitPrice', v || 0)}
                />
              ),
            },
            {
              title: '金额',
              width: 100,
              render: (_, record) => `¥${(record.quantity * record.unitPrice || 0).toFixed(2)}`,
            },
            {
              title: '',
              width: 50,
              render: (_, record) => (
                <Button type="text" danger icon={<MinusOutlined />} onClick={() => handleRemoveItem(record.key)} />
              ),
            },
          ]}
          summary={() => (
            <Table.Summary.Row>
              <Table.Summary.Cell index={0} colSpan={5}>合计</Table.Summary.Cell>
              <Table.Summary.Cell index={1}>¥{totalAmount.toFixed(2)}</Table.Summary.Cell>
              <Table.Summary.Cell index={2} />
            </Table.Summary.Row>
          )}
        />
      </Modal>

      <Modal
        title="销售订单详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={800}
      >
        {currentOrder && (
          <>
            <Descriptions bordered column={2} style={{ marginBottom: 16 }}>
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
            <h4>订单明细</h4>
            <Table
              dataSource={orderItems}
              rowKey="id"
              size="small"
              pagination={false}
              columns={[
                { title: '商品', dataIndex: 'productName' },
                { title: '规格', dataIndex: 'specification' },
                { title: '数量', dataIndex: 'quantity' },
                { title: '单位', dataIndex: 'unit' },
                { title: '单价', dataIndex: 'unitPrice', render: (v) => v ? `¥${v}` : '-' },
                { title: '已发货', dataIndex: 'shippedQty', render: (v) => v || 0 },
                {
                  title: '状态',
                  dataIndex: 'status',
                  render: (v) => {
                    const map: Record<string, string> = { PENDING: '待发货', PARTIAL: '部分发货', SHIPPED: '已发货' }
                    return map[v] || v
                  }
                },
              ]}
            />
          </>
        )}
      </Modal>

      <Modal
        title="发货出库"
        open={shipVisible}
        onCancel={() => setShipVisible(false)}
        onOk={() => shipForm.submit()}
        width={800}
      >
        <Form form={shipForm} layout="vertical" onFinish={handleShip}>
          <Form.Item name="warehouseId" label="出库仓库" rules={[{ required: true }]}>
            <Select>
              {warehouses.map(w => (
                <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
        <h4>发货明细</h4>
        <Table
          dataSource={orderItems}
          rowKey="id"
          size="small"
          pagination={false}
          columns={[
            { title: '商品', dataIndex: 'productName' },
            { title: '订单数量', dataIndex: 'quantity' },
            { title: '已发货', dataIndex: 'shippedQty', render: (v) => v || 0 },
            { title: '待发货', render: (_, record) => record.quantity - (record.shippedQty || 0) },
            {
              title: '本次发货',
              render: (_, record) => (
                <InputNumber
                  min={0}
                  max={record.quantity - (record.shippedQty || 0)}
                  value={record.shipQty}
                  onChange={(v) => {
                    const items = [...orderItems]
                    const idx = items.findIndex(i => i.id === record.id)
                    if (idx >= 0) {
                      items[idx].shipQty = v || 0
                      setOrderItems(items)
                    }
                  }}
                />
              )
            },
          ]}
        />
      </Modal>
    </Card>
  )
}
