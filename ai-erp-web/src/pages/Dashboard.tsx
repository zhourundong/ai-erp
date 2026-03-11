import { Card, Row, Col, Statistic } from 'antd'
import { ShoppingCartOutlined, DatabaseOutlined, DollarOutlined, TeamOutlined } from '@ant-design/icons'

export default function Dashboard() {
  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>仪表盘</h2>
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic
              title="采购订单"
              value={156}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="库存数量"
              value={12345}
              prefix={<DatabaseOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="本月销售额"
              value={156789}
              prefix={<DollarOutlined />}
              precision={2}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="员工数量"
              value={89}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>
      
      <Card style={{ marginTop: 24 }} title="AI 助手提示">
        <p>欢迎使用 AI Native ERP！系统已为您准备好以下功能：</p>
        <ul>
          <li>智能采购建议 - 系统自动分析库存，推荐采购时机</li>
          <li>智能库存预警 - 动态预测库存风险</li>
          <li>自然语言查询 - 直接用对话方式查询业务数据</li>
          <li>智能报表解读 - 自动分析数据趋势</li>
        </ul>
      </Card>
    </div>
  )
}
