import { useState, useEffect } from 'react'
import { Table, Card, Form, Select, Space, Tag, Button } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import { inventoryApi, warehouseApi, productApi } from '../services/api'
import type { ColumnsType } from 'antd/es/table'

interface InventoryTransaction {
  id: number
  transactionNo: string
  transactionType: string
  warehouseId: number
  warehouseName: string
  productId: number
  productSku: string
  productName: string
  quantity: number
  beforeQty: number
  afterQty: number
  unitCost: number
  totalCost: number
  relatedOrderType: string
  relatedOrderId: number
  relatedOrderNo: string
  operator: string
  remark: string
  createdAt: string
}

interface Warehouse {
  id: number
  name: string
}

interface Product {
  id: number
  name: string
  sku: string
}

export default function InventoryTransactionPage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<InventoryTransaction[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const values = form.getFieldsValue()
      const response: any = await inventoryApi.transactions({
        pageNum,
        pageSize,
        warehouseId: values.warehouseId,
        productId: values.productId,
        transactionType: values.transactionType,
      })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      console.error('获取库存流水失败', error)
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
      const response: any = await productApi.list({ pageNum: 1, pageSize: 200 })
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

  const handleSearch = () => {
    setPageNum(1)
    fetchData()
  }

  const handleReset = () => {
    form.resetFields()
    setPageNum(1)
    fetchData()
  }

  const transactionTypeMap: Record<string, { color: string; text: string }> = {
    PURCHASE_IN: { color: 'success', text: '采购入库' },
    SALES_OUT: { color: 'warning', text: '销售出库' },
    OTHER_IN: { color: 'success', text: '其他入库' },
    OTHER_OUT: { color: 'warning', text: '其他出库' },
    TRANSFER: { color: 'processing', text: '库存调拨' },
    ADJUST: { color: 'default', text: '库存调整' },
  }

  const columns: ColumnsType<InventoryTransaction> = [
    { title: '流水号', dataIndex: 'transactionNo', width: 160 },
    {
      title: '交易类型',
      dataIndex: 'transactionType',
      width: 100,
      render: (type) => {
        const { color, text } = transactionTypeMap[type] || { color: 'default', text: type }
        return <Tag color={color}>{text}</Tag>
      },
    },
    { title: '仓库', dataIndex: 'warehouseName', width: 120 },
    { title: '商品编码', dataIndex: 'productSku', width: 100 },
    { title: '商品名称', dataIndex: 'productName', width: 150 },
    {
      title: '数量',
      dataIndex: 'quantity',
      width: 100,
      render: (v) => {
        const color = v > 0 ? '#52c41a' : '#ff4d4f'
        return <span style={{ color, fontWeight: 'bold' }}>{v > 0 ? `+${v}` : v}</span>
      },
    },
    { title: '变更前', dataIndex: 'beforeQty', width: 80 },
    { title: '变更后', dataIndex: 'afterQty', width: 80 },
    {
      title: '单价',
      dataIndex: 'unitCost',
      width: 80,
      render: (v) => v ? `¥${v}` : '-',
    },
    {
      title: '总金额',
      dataIndex: 'totalCost',
      width: 100,
      render: (v) => v ? `¥${v}` : '-',
    },
    {
      title: '关联单据',
      width: 150,
      render: (_, record) => {
        if (record.relatedOrderNo) {
          return (
            <span>
              {record.relatedOrderType === 'PURCHASE_ORDER' ? '采购单' :
               record.relatedOrderType === 'SALES_ORDER' ? '销售单' : record.relatedOrderType}
              : {record.relatedOrderNo}
            </span>
          )
        }
        return '-'
      },
    },
    { title: '操作人', dataIndex: 'operator', width: 80 },
    { title: '时间', dataIndex: 'createdAt', width: 160 },
  ]

  return (
    <Card
      title="库存流水"
      extra={
        <Button icon={<ReloadOutlined />} onClick={fetchData}>
          刷新
        </Button>
      }
    >
      <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
        <Form.Item name="warehouseId" label="仓库">
          <Select placeholder="全部仓库" allowClear style={{ width: 150 }}>
            {warehouses.map(w => (
              <Select.Option key={w.id} value={w.id}>{w.name}</Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="productId" label="商品">
          <Select placeholder="全部商品" allowClear showSearch optionFilterProp="children" style={{ width: 200 }}>
            {products.map(p => (
              <Select.Option key={p.id} value={p.id}>{p.name} ({p.sku})</Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="transactionType" label="类型">
          <Select placeholder="全部类型" allowClear style={{ width: 120 }}>
            <Select.Option value="PURCHASE_IN">采购入库</Select.Option>
            <Select.Option value="SALES_OUT">销售出库</Select.Option>
            <Select.Option value="OTHER_IN">其他入库</Select.Option>
            <Select.Option value="OTHER_OUT">其他出库</Select.Option>
            <Select.Option value="TRANSFER">库存调拨</Select.Option>
            <Select.Option value="ADJUST">库存调整</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" onClick={handleSearch}>查询</Button>
            <Button onClick={handleReset}>重置</Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1600 }}
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
    </Card>
  )
}
