import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Row, Col, Statistic, Table, Tag, Progress, Spin, Empty } from 'antd'
import {
  ShoppingCartOutlined,
  DollarOutlined,
  InboxOutlined,
  AlertOutlined,
  RiseOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import { dashboardApi } from '../services/api'
import './DashboardPage.css'

interface StatusCount {
  status: string
  label: string
  count: number
}

interface InventoryWarning {
  productId: number
  productSku: string
  productName: string
  safetyStock: number
  currentStock: number
  warehouseName: string
}

interface DailyTrend {
  date: string
  count: number
  amount: number
}

interface DashboardStats {
  purchaseOrderCount: number
  salesOrderCount: number
  productCount: number
  supplierCount: number
  customerCount: number
  warehouseCount: number
  totalPurchaseAmount: number
  totalSalesAmount: number
  purchaseOrderStatus: StatusCount[]
  salesOrderStatus: StatusCount[]
  inventoryWarningCount: number
  inventoryWarnings: InventoryWarning[]
  purchaseTrend: DailyTrend[]
  salesTrend: DailyTrend[]
  pendingApprovalCount: number
  pendingReceiveCount: number
  pendingShipCount: number
}

const statusColorMap: Record<string, string> = {
  DRAFT: 'default',
  PENDING: 'warning',
  APPROVED: 'processing',
  RECEIVING: 'processing',
  COMPLETED: 'success',
  REJECTED: 'error',
  CONFIRMED: 'processing',
  SHIPPING: 'processing',
  CANCELLED: 'error',
}

export default function DashboardPage() {
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    fetchStats()
  }, [])

  const fetchStats = async () => {
    try {
      setLoading(true)
      const data: any = await dashboardApi.getStats()
      setStats(data)
    } catch (error) {
      console.error('获取统计数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatAmount = (amount: number) => {
    if (!amount) return '¥0'
    if (amount >= 10000) {
      return `¥${(amount / 10000).toFixed(2)}万`
    }
    return `¥${amount.toFixed(2)}`
  }

  // 采购趋势图配置
  const purchaseTrendOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stats?.purchaseTrend.map(t => t.date) || [],
    },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '金额', position: 'right' }
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: stats?.purchaseTrend.map(t => t.count) || [],
        itemStyle: { color: '#667eea' }
      },
      {
        name: '金额',
        type: 'line',
        yAxisIndex: 1,
        data: stats?.purchaseTrend.map(t => t.amount) || [],
        itemStyle: { color: '#8b5cf6' }
      }
    ]
  }

  // 销售趋势图配置
  const salesTrendOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stats?.salesTrend.map(t => t.date) || [],
    },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '金额', position: 'right' }
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: stats?.salesTrend.map(t => t.count) || [],
        itemStyle: { color: '#52c41a' }
      },
      {
        name: '金额',
        type: 'line',
        yAxisIndex: 1,
        data: stats?.salesTrend.map(t => t.amount) || [],
        itemStyle: { color: '#faad14' }
      }
    ]
  }

  // 库存预警表格列
  const warningColumns = [
    {
      title: '商品',
      dataIndex: 'productName',
      key: 'productName',
      render: (text: string, record: InventoryWarning) => (
        <div>
          <div style={{ fontWeight: 500 }}>{text}</div>
          <div style={{ fontSize: 12, color: '#999' }}>{record.productSku}</div>
        </div>
      )
    },
    {
      title: '当前库存',
      dataIndex: 'currentStock',
      key: 'currentStock',
      render: (val: number, record: InventoryWarning) => (
        <span style={{ color: '#ff4d4f' }}>
          {val} / {record.safetyStock}
        </span>
      )
    },
    {
      title: '库存状态',
      key: 'status',
      render: (_: any, record: InventoryWarning) => {
        const percent = record.safetyStock > 0
          ? Math.round((record.currentStock / record.safetyStock) * 100)
          : 0
        return (
          <Progress
            percent={percent}
            size="small"
            status={percent < 50 ? 'exception' : 'normal'}
            format={() => `${percent}%`}
          />
        )
      }
    }
  ]

  // 待办事项
  const todoItems = [
    { key: 'approval', icon: <ClockCircleOutlined />, label: '待审批采购订单', count: stats?.pendingApprovalCount || 0, color: '#faad14', path: '/purchase-orders?status=PENDING' },
    { key: 'receive', icon: <InboxOutlined />, label: '待收货采购订单', count: stats?.pendingReceiveCount || 0, color: '#52c41a', path: '/purchase-orders?status=APPROVED' },
    { key: 'ship', icon: <RiseOutlined />, label: '待发货销售订单', count: stats?.pendingShipCount || 0, color: '#1890ff', path: '/sales-orders?status=CONFIRMED' },
  ]

  if (loading) {
    return (
      <div className="dashboard-loading">
        <Spin size="large" />
      </div>
    )
  }

  if (!stats) {
    return <Empty description="暂无数据" />
  }

  return (
    <div className="dashboard-page">
      {/* 核心指标 */}
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card" hoverable onClick={() => navigate('/purchase-orders')}>
            <Statistic
              title="采购订单"
              value={stats.purchaseOrderCount}
              prefix={<ShoppingCartOutlined style={{ color: '#667eea' }} />}
              valueStyle={{ color: '#667eea' }}
            />
            <div className="stat-footer">
              <span>总金额: {formatAmount(stats.totalPurchaseAmount)}</span>
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card" hoverable onClick={() => navigate('/sales-orders')}>
            <Statistic
              title="销售订单"
              value={stats.salesOrderCount}
              prefix={<DollarOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
            <div className="stat-footer">
              <span>总金额: {formatAmount(stats.totalSalesAmount)}</span>
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card" hoverable onClick={() => navigate('/products')}>
            <Statistic
              title="商品总数"
              value={stats.productCount}
              prefix={<InboxOutlined style={{ color: '#8b5cf6' }} />}
              valueStyle={{ color: '#8b5cf6' }}
            />
            <div className="stat-footer">
              <span>供应商: {stats.supplierCount} | 客户: {stats.customerCount}</span>
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={12} md={6}>
          <Card className="stat-card" hoverable onClick={() => navigate('/inventory')}>
            <Statistic
              title="库存预警"
              value={stats.inventoryWarningCount}
              prefix={<AlertOutlined style={{ color: stats.inventoryWarningCount > 0 ? '#ff4d4f' : '#52c41a' }} />}
              valueStyle={{ color: stats.inventoryWarningCount > 0 ? '#ff4d4f' : '#52c41a' }}
            />
            <div className="stat-footer">
              <span>仓库: {stats.warehouseCount}</span>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 待办事项 */}
      <Card title="待办事项" className="section-card">
        <Row gutter={16}>
          {todoItems.map(item => (
            <Col xs={24} sm={8} key={item.key}>
              <Card
                className="todo-card"
                hoverable
                onClick={() => navigate(item.path)}
              >
                <div className="todo-content">
                  <div className="todo-icon" style={{ backgroundColor: `${item.color}20`, color: item.color }}>
                    {item.icon}
                  </div>
                  <div className="todo-info">
                    <div className="todo-label">{item.label}</div>
                    <div className="todo-count" style={{ color: item.color }}>{item.count}</div>
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>

      {/* 趋势图表 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="采购趋势（近7天）" className="section-card">
            <ReactECharts option={purchaseTrendOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="销售趋势（近7天）" className="section-card">
            <ReactECharts option={salesTrendOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>

      {/* 订单状态 & 库存预警 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="采购订单状态" className="section-card">
            <div className="status-list">
              {stats.purchaseOrderStatus.length > 0 ? (
                stats.purchaseOrderStatus.map(item => (
                  <div key={item.status} className="status-item">
                    <Tag color={statusColorMap[item.status] || 'default'}>{item.label}</Tag>
                    <span className="status-count">{item.count}</span>
                  </div>
                ))
              ) : (
                <Empty description="暂无数据" />
              )}
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            title={`库存预警 (${stats.inventoryWarningCount})`}
            className="section-card"
            extra={stats.inventoryWarningCount > 0 && (
              <a onClick={() => navigate('/inventory')}>查看全部</a>
            )}
          >
            {stats.inventoryWarnings.length > 0 ? (
              <Table
                columns={warningColumns}
                dataSource={stats.inventoryWarnings}
                rowKey="productId"
                pagination={false}
                size="small"
              />
            ) : (
              <Empty description="库存充足" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>

      {/* 快捷入口 */}
      <Card title="快捷入口" className="section-card">
        <Row gutter={[16, 16]}>
          {[
            { label: '采购订单', icon: '📋', path: '/purchase-orders' },
            { label: '销售订单', icon: '📝', path: '/sales-orders' },
            { label: '库存查询', icon: '📦', path: '/inventory' },
            { label: '商品管理', icon: '🏷️', path: '/products' },
            { label: '供应商', icon: '🏭', path: '/suppliers' },
            { label: '客户管理', icon: '👥', path: '/customers' },
          ].map(item => (
            <Col xs={12} sm={8} md={4} key={item.path}>
              <Card
                className="quick-card"
                hoverable
                onClick={() => navigate(item.path)}
              >
                <div className="quick-icon">{item.icon}</div>
                <div className="quick-label">{item.label}</div>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </div>
  )
}
