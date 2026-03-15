import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Select, InputNumber, message } from 'antd'
import { PlusOutlined, MinusOutlined } from '@ant-design/icons'
import { inventoryApi, warehouseApi, productApi } from '../services/api'
import type { ColumnsType } from 'antd/es/table'

interface Inventory {
  id: number
  warehouseId: number
  productId: number
  productSku: string
  productName: string
  quantity: number
  availableQty: number
  lockedQty: number
  costPrice: number
}

interface Warehouse {
  id: number
  name: string
}

interface Product {
  id: number
  sku: string
  name: string
}

export default function InventoryPage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<Inventory[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [stockInVisible, setStockInVisible] = useState(false)
  const [stockOutVisible, setStockOutVisible] = useState(false)
  const [stockInForm] = Form.useForm()
  const [stockOutForm] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await inventoryApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取库存列表失败')
    } finally {
      setLoading(false)
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
      const response: any = await productApi.list({ pageNum: 1, pageSize: 100 })
      setProducts(response.data?.records || [])
    } catch (error) {
      console.error('获取商品失败', error)
    }
  }

  useEffect(() => {
    fetchData()
    fetchWarehouses()
    fetchProducts()
  }, [pageNum, pageSize])

  const handleStockIn = () => {
    stockInForm.resetFields()
    setStockInVisible(true)
  }

  const handleStockOut = () => {
    stockOutForm.resetFields()
    setStockOutVisible(true)
  }

  const handleStockInSubmit = async (values: any) => {
    try {
      const product = products.find(p => p.id === values.productId)
      await inventoryApi.stockIn({
        ...values,
        productSku: product?.sku,
        productName: product?.name,
        operator: 'admin',
      })
      message.success('入库成功')
      setStockInVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '入库失败')
    }
  }

  const handleStockOutSubmit = async (values: any) => {
    try {
      await inventoryApi.stockOut({
        ...values,
        operator: 'admin',
      })
      message.success('出库成功')
      setStockOutVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '出库失败')
    }
  }

  const columns: ColumnsType<Inventory> = [
    { title: 'SKU', dataIndex: 'productSku', width: 120 },
    { title: '商品名称', dataIndex: 'productName', width: 200 },
    {
      title: '库存数量',
      dataIndex: 'quantity',
      width: 100,
      render: (v) => <Tag color="blue">{v || 0}</Tag>,
    },
    {
      title: '可用数量',
      dataIndex: 'availableQty',
      width: 100,
      render: (v) => <Tag color="green">{v || 0}</Tag>,
    },
    {
      title: '锁定数量',
      dataIndex: 'lockedQty',
      width: 100,
      render: (v) => <Tag color="orange">{v || 0}</Tag>,
    },
    {
      title: '成本价',
      dataIndex: 'costPrice',
      width: 100,
      render: (v) => v ? `¥${v}` : '-',
    },
    {
      title: '库存状态',
      width: 100,
      render: (_, record) => {
        const qty = record.quantity || 0
        if (qty <= 0) return <Tag color="error">缺货</Tag>
        if (qty <= 10) return <Tag color="warning">低库存</Tag>
        return <Tag color="success">正常</Tag>
      },
    },
  ]

  return (
    <Card
      title="库存管理"
      extra={
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleStockIn}>
            入库
          </Button>
          <Button icon={<MinusOutlined />} onClick={handleStockOut}>
            出库
          </Button>
        </Space>
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
        title="商品入库"
        open={stockInVisible}
        onCancel={() => setStockInVisible(false)}
        onOk={() => stockInForm.submit()}
        width={500}
      >
        <Form form={stockInForm} layout="vertical" onFinish={handleStockInSubmit}>
          <Form.Item name="warehouseId" label="仓库" rules={[{ required: true }]}>
            <Select>
              {warehouses.map(w => (
                <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="productId" label="商品" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="children">
              {products.map(p => (
                <Select.Option key={p.id} value={p.id}>{p.sku} - {p.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="quantity" label="入库数量" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="costPrice" label="成本价">
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="商品出库"
        open={stockOutVisible}
        onCancel={() => setStockOutVisible(false)}
        onOk={() => stockOutForm.submit()}
        width={500}
      >
        <Form form={stockOutForm} layout="vertical" onFinish={handleStockOutSubmit}>
          <Form.Item name="warehouseId" label="仓库" rules={[{ required: true }]}>
            <Select>
              {warehouses.map(w => (
                <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="productId" label="商品" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="children">
              {products.map(p => (
                <Select.Option key={p.id} value={p.id}>{p.sku} - {p.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="quantity" label="出库数量" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
