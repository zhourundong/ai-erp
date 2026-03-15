import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Descriptions, InputNumber, Divider } from 'antd'
import { PlusOutlined, EyeOutlined, CheckOutlined, CloseOutlined, ImportOutlined, MinusOutlined, EditOutlined } from '@ant-design/icons'
import { purchaseOrderApi, supplierApi, warehouseApi, productApi } from '../services/api'
import { useAuthStore } from '../stores/authStore'
import { useTabStore } from '../stores/tabStore'
import type { ColumnsType } from 'antd/es/table'

interface PurchaseOrder {
  id: number
  orderNo: string
  orderDate: string
  supplierId: number
  supplierName: string
  totalAmount: number
  status: string
  paymentStatus: string
  buyerName: string
  remark: string
}

interface PurchaseOrderItem {
  id: number
  productId: number
  productSku: string
  productName: string
  specification: string
  quantity: number
  unit: string
  unitPrice: number
  receivedQty: number
  status: string
  receiveQty?: number // 本次收货数量
}

interface Supplier {
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
  costPrice: number
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

export default function PurchaseOrderPage() {
  const { user } = useAuthStore()
  const { consumePendingDetail, detailVersion } = useTabStore()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<PurchaseOrder[]>([])
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [receiveVisible, setReceiveVisible] = useState(false)
  const [currentOrder, setCurrentOrder] = useState<PurchaseOrder | null>(null)
  const [orderItems, setOrderItems] = useState<PurchaseOrderItem[]>([])
  const [receiveForm] = Form.useForm()
  const [form] = Form.useForm()
  const [orderItemInputs, setOrderItemInputs] = useState<OrderItemInput[]>([])
  const [itemKeyCounter, setItemKeyCounter] = useState(0)

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await purchaseOrderApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取采购订单列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchSuppliers = async () => {
    try {
      const response: any = await supplierApi.list({ pageNum: 1, pageSize: 100 })
      setSuppliers(response.data?.records || [])
    } catch (error) {
      console.error('获取供应商失败', error)
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
    fetchSuppliers()
    fetchWarehouses()
    fetchProducts()
  }, [pageNum, pageSize])

  // 处理待打开的详情或创建表单（从 AI 助手导航过来）
  useEffect(() => {
    const pendingDetail = consumePendingDetail()
    if (pendingDetail && pendingDetail.basePath === '/purchase-orders') {
      if (pendingDetail.type === 'create') {
        // 打开创建表单
        handleAdd()
      } else if (pendingDetail.type === 'view' && pendingDetail.id) {
        // 查看详情
        const orderId = parseInt(pendingDetail.id)
        if (!isNaN(orderId)) {
          // 先尝试从列表中查找
          const order = data.find(o => o.id === orderId)
          if (order) {
            handleViewDetail(order)
          } else {
            // 列表中没有，从 API 获取
            purchaseOrderApi.get(orderId).then((response: any) => {
              if (response.data) {
                handleViewDetail(response.data)
              } else {
                message.warning('未找到该订单')
              }
            }).catch(() => {
              message.error('获取订单详情失败')
            })
          }
        }
      }
    }
  }, [detailVersion, data])

  const handleAdd = () => {
    form.resetFields()
    setOrderItemInputs([])
    setItemKeyCounter(0)
    setEditMode(false)
    setCurrentOrder(null)
    setModalVisible(true)
  }

  // 编辑订单
  const handleEdit = async (record: PurchaseOrder) => {
    setCurrentOrder(record)
    try {
      const response: any = await purchaseOrderApi.getItems(record.id)
      const items = (response.data || []).map((item: PurchaseOrderItem, index: number) => ({
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
        supplierId: record.supplierId,
        remark: record.remark,
      })
      setEditMode(true)
      setModalVisible(true)
    } catch (error) {
      message.error('获取订单明细失败')
    }
  }

  const handleViewDetail = async (record: PurchaseOrder) => {
    setCurrentOrder(record)
    try {
      const response: any = await purchaseOrderApi.getItems(record.id)
      setOrderItems(response.data || [])
    } catch (error) {
      setOrderItems([])
    }
    setDetailVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await purchaseOrderApi.delete(id)
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
            unitPrice: product.costPrice || 0,
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

    const supplier = suppliers.find(s => s.id === values.supplierId)

    try {
      if (editMode && currentOrder) {
        // 编辑模式
        await purchaseOrderApi.update(currentOrder.id, {
          supplierId: values.supplierId,
          supplierName: supplier?.name || '',
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
        await purchaseOrderApi.create({
          supplierId: values.supplierId,
          supplierName: supplier?.name || '',
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

  // 提交审批
  const handleSubmitApproval = async (id: number) => {
    try {
      await purchaseOrderApi.submit(id)
      message.success('提交成功')
      fetchData()
    } catch (error: any) {
      message.error(error.message || '提交失败')
    }
  }

  // 审批通过
  const handleApprove = async (id: number) => {
    try {
      await purchaseOrderApi.approve(id)
      message.success('审批通过')
      fetchData()
    } catch (error: any) {
      message.error(error.message || '审批失败')
    }
  }

  // 审批拒绝
  const handleReject = async (id: number) => {
    Modal.confirm({
      title: '审批拒绝',
      content: '确定要拒绝此采购订单吗？',
      onOk: async () => {
        try {
          await purchaseOrderApi.reject(id)
          message.success('已拒绝')
          fetchData()
        } catch (error: any) {
          message.error(error.message || '操作失败')
        }
      },
    })
  }

  // 反审核
  const handleUnapprove = async (id: number) => {
    Modal.confirm({
      title: '反审核',
      content: '反审核后订单将变为待审批状态，确定继续？',
      onOk: async () => {
        try {
          await purchaseOrderApi.unapprove(id)
          message.success('反审核成功')
          fetchData()
        } catch (error: any) {
          message.error(error.message || '操作失败')
        }
      },
    })
  }

  // 打开收货入库弹窗
  const handleOpenReceive = async (record: PurchaseOrder) => {
    setCurrentOrder(record)
    try {
      const response: any = await purchaseOrderApi.getItems(record.id)
      const items = (response.data || []).map((item: PurchaseOrderItem) => ({
        ...item,
        receiveQty: item.quantity - (item.receivedQty || 0), // 默认填入未收货数量
      }))
      setOrderItems(items)
      receiveForm.setFieldsValue({
        warehouseId: warehouses.find(w => w.name === '主仓库')?.id || warehouses[0]?.id,
      })
    } catch (error) {
      setOrderItems([])
    }
    setReceiveVisible(true)
  }

  // 执行收货入库
  const handleReceive = async (values: any) => {
    const items = orderItems
      .filter(item => (item.receiveQty || 0) > 0)
      .map(item => ({
        itemId: item.id,
        quantity: item.receiveQty || 0,
      }))

    if (items.length === 0) {
      message.warning('请输入收货数量')
      return
    }

    try {
      await purchaseOrderApi.receive(currentOrder!.id, {
        warehouseId: values.warehouseId,
        items,
        operator: user?.realName || user?.username || '系统',
      })
      message.success('收货入库成功')
      setReceiveVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '收货入库失败')
    }
  }

  const statusMap: Record<string, { color: string; text: string }> = {
    DRAFT: { color: 'default', text: '草稿' },
    PENDING: { color: 'processing', text: '待审批' },
    APPROVED: { color: 'success', text: '已审批' },
    RECEIVING: { color: 'processing', text: '收货中' },
    COMPLETED: { color: 'success', text: '已完成' },
    REJECTED: { color: 'error', text: '已拒绝' },
    CANCELLED: { color: 'error', text: '已取消' },
  }

  const paymentStatusMap: Record<string, { color: string; text: string }> = {
    UNPAID: { color: 'error', text: '未付款' },
    PARTIAL: { color: 'warning', text: '部分付款' },
    PAID: { color: 'success', text: '已付款' },
  }

  const columns: ColumnsType<PurchaseOrder> = [
    { title: '订单号', dataIndex: 'orderNo', width: 150 },
    { title: '订单日期', dataIndex: 'orderDate', width: 120 },
    { title: '供应商', dataIndex: 'supplierName', width: 150 },
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
      title: '付款状态',
      dataIndex: 'paymentStatus',
      width: 100,
      render: (status) => {
        const { color, text } = paymentStatusMap[status] || { color: 'default', text: status }
        return <Tag color={color}>{text}</Tag>
      },
    },
    { title: '采购员', dataIndex: 'buyerName', width: 100 },
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
              <Button type="link" size="small" onClick={() => handleSubmitApproval(record.id)}>
                提交审批
              </Button>
              <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
                <Button type="link" size="small" danger>
                  删除
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'PENDING' && (
            <>
              <Button type="link" size="small" icon={<CheckOutlined />} onClick={() => handleApprove(record.id)}>
                通过
              </Button>
              <Button type="link" size="small" danger icon={<CloseOutlined />} onClick={() => handleReject(record.id)}>
                拒绝
              </Button>
            </>
          )}
          {record.status === 'APPROVED' && (
            <Button type="link" size="small" danger onClick={() => handleUnapprove(record.id)}>
              反审核
            </Button>
          )}
          {(record.status === 'APPROVED' || record.status === 'RECEIVING') && (
            <Button type="link" size="small" icon={<ImportOutlined />} onClick={() => handleOpenReceive(record)}>
              收货入库
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
      title="采购订单"
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
        title={editMode ? '编辑采购订单' : '新增采购订单'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={900}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="supplierId" label="供应商" rules={[{ required: true }]}>
            <Select placeholder="请选择供应商">
              {suppliers.map(s => (
                <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>
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
                    <Select.Option key={p.id} value={p.id}>
                      {p.name} ({p.sku}) {p.costPrice ? `¥${p.costPrice}` : '(无成本价)'}
                    </Select.Option>
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
        title="采购订单详情"
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
              <Descriptions.Item label="供应商">{currentOrder.supplierName}</Descriptions.Item>
              <Descriptions.Item label="采购员">{currentOrder.buyerName}</Descriptions.Item>
              <Descriptions.Item label="总金额">
                {currentOrder.totalAmount ? `¥${currentOrder.totalAmount}` : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="订单状态">
                <Tag color={statusMap[currentOrder.status]?.color}>
                  {statusMap[currentOrder.status]?.text}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="付款状态">
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
                { title: '已收货', dataIndex: 'receivedQty', render: (v) => v || 0 },
                {
                  title: '状态',
                  dataIndex: 'status',
                  render: (v) => {
                    const map: Record<string, string> = { PENDING: '待收货', PARTIAL: '部分收货', RECEIVED: '已收货' }
                    return map[v] || v
                  }
                },
              ]}
            />
          </>
        )}
      </Modal>

      <Modal
        title="收货入库"
        open={receiveVisible}
        onCancel={() => setReceiveVisible(false)}
        onOk={() => receiveForm.submit()}
        width={800}
      >
        <Form form={receiveForm} layout="vertical" onFinish={handleReceive}>
          <Form.Item name="warehouseId" label="入库仓库" rules={[{ required: true }]}>
            <Select>
              {warehouses.map(w => (
                <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
        <h4>收货明细</h4>
        <Table
          dataSource={orderItems}
          rowKey="id"
          size="small"
          pagination={false}
          columns={[
            { title: '商品', dataIndex: 'productName' },
            { title: '订单数量', dataIndex: 'quantity' },
            { title: '已收货', dataIndex: 'receivedQty', render: (v) => v || 0 },
            { title: '待收货', render: (_, record) => record.quantity - (record.receivedQty || 0) },
            {
              title: '本次收货',
              render: (_, record) => (
                <InputNumber
                  min={0}
                  max={record.quantity - (record.receivedQty || 0)}
                  value={record.receiveQty}
                  onChange={(v) => {
                    const items = [...orderItems]
                    const idx = items.findIndex(i => i.id === record.id)
                    if (idx >= 0) {
                      items[idx].receiveQty = v || 0
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
