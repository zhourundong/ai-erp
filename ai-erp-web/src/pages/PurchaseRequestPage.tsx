import { useState, useEffect } from 'react'
import { Table, Card, Button, Space, Tag, Modal, Form, Input, Select, message, Descriptions, TreeSelect } from 'antd'
import { PlusOutlined, EyeOutlined, CheckOutlined, CloseOutlined, FileAddOutlined } from '@ant-design/icons'
import { purchaseRequestApi, supplierApi, organizationApi } from '../services/api'
import { useAuthStore } from '../stores/authStore'
import type { ColumnsType } from 'antd/es/table'

interface PurchaseRequest {
  id: number
  requestNo: string
  requestDate: string
  applicantName: string
  departmentId: number
  departmentName: string
  requirementDescription: string
  status: string
  aiEstimatedAmount: number
  approverName: string
  approvalComment: string
  purchaseOrderId: number
  createdAt: string
}

interface Supplier {
  id: number
  name: string
}

interface OrgNode {
  id: number
  name: string
  parentId: number
  type: string
  children?: OrgNode[]
}

export default function PurchaseRequestPage() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<PurchaseRequest[]>([])
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [orgTree, setOrgTree] = useState<OrgNode[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [modalVisible, setModalVisible] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [createOrderVisible, setCreateOrderVisible] = useState(false)
  const [currentRequest, setCurrentRequest] = useState<PurchaseRequest | null>(null)
  const [form] = Form.useForm()
  const [createOrderForm] = Form.useForm()
  const { user } = useAuthStore()

  const fetchData = async () => {
    setLoading(true)
    try {
      const response: any = await purchaseRequestApi.list({ pageNum, pageSize })
      setData(response.data?.records || [])
      setTotal(response.data?.total || 0)
    } catch (error) {
      message.error('获取采购申请列表失败')
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

  const fetchOrganizations = async () => {
    try {
      const response: any = await organizationApi.tree()
      setOrgTree(response.data || [])
    } catch (error) {
      console.error('获取组织失败', error)
    }
  }

  useEffect(() => {
    fetchData()
    fetchSuppliers()
    fetchOrganizations()
  }, [pageNum, pageSize])

  const handleAdd = () => {
    form.resetFields()
    setModalVisible(true)
  }

  const handleViewDetail = (record: PurchaseRequest) => {
    setCurrentRequest(record)
    setDetailVisible(true)
  }

  const handleSubmit = async (values: any) => {
    // 从组织树中查找部门名称
    const findOrgName = (nodes: OrgNode[], id: number): string => {
      for (const node of nodes) {
        if (node.id === id) return node.name
        if (node.children) {
          const found = findOrgName(node.children, id)
          if (found) return found
        }
      }
      return ''
    }

    const departmentName = findOrgName(orgTree, values.departmentId)

    try {
      await purchaseRequestApi.create({
        ...values,
        departmentName,
        applicantId: user?.id,
        applicantName: user?.realName,
      })
      message.success('创建成功')
      setModalVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const handleSubmitForApproval = async (id: number) => {
    try {
      await purchaseRequestApi.submit(id)
      message.success('提交成功')
      fetchData()
    } catch (error: any) {
      message.error(error.message || '提交失败')
    }
  }

  const handleApprove = async (id: number) => {
    try {
      await purchaseRequestApi.approve(id, {
        approverId: user?.id || 0,
        approverName: user?.realName || '',
      })
      message.success('审批通过')
      fetchData()
    } catch (error: any) {
      message.error(error.message || '审批失败')
    }
  }

  const handleReject = async (id: number) => {
    Modal.confirm({
      title: '审批拒绝',
      content: (
        <Input.TextArea
          id="rejectReason"
          placeholder="请输入拒绝原因"
          rows={3}
        />
      ),
      onOk: async () => {
        const reason = (document.getElementById('rejectReason') as HTMLTextAreaElement)?.value
        try {
          await purchaseRequestApi.reject(id, {
            approverId: user?.id || 0,
            approverName: user?.realName || '',
            comment: reason,
          })
          message.success('已拒绝')
          fetchData()
        } catch (error: any) {
          message.error(error.message || '操作失败')
        }
      },
    })
  }

  // 打开生成订单弹窗
  const handleOpenCreateOrder = (record: PurchaseRequest) => {
    setCurrentRequest(record)
    createOrderForm.resetFields()
    setCreateOrderVisible(true)
  }

  // 从申请生成订单
  const handleCreateOrder = async (values: any) => {
    try {
      const response: any = await purchaseRequestApi.createOrder(currentRequest!.id, {
        supplierId: values.supplierId,
        supplierName: suppliers.find(s => s.id === values.supplierId)?.name || '',
        buyerId: user?.id,
        buyerName: user?.realName,
      })
      message.success(`采购订单创建成功: ${response.data?.orderNo || ''}`)
      setCreateOrderVisible(false)
      fetchData()
    } catch (error: any) {
      message.error(error.message || '创建订单失败')
    }
  }

  const statusMap: Record<string, { color: string; text: string }> = {
    DRAFT: { color: 'default', text: '草稿' },
    PENDING: { color: 'processing', text: '待审批' },
    APPROVED: { color: 'success', text: '已通过' },
    REJECTED: { color: 'error', text: '已拒绝' },
  }

  const columns: ColumnsType<PurchaseRequest> = [
    { title: '申请单号', dataIndex: 'requestNo', width: 150 },
    { title: '申请日期', dataIndex: 'requestDate', width: 120 },
    { title: '申请人', dataIndex: 'applicantName', width: 100 },
    { title: '部门', dataIndex: 'departmentName', width: 120 },
    {
      title: '需求描述',
      dataIndex: 'requirementDescription',
      ellipsis: true,
    },
    {
      title: '预估金额',
      dataIndex: 'aiEstimatedAmount',
      width: 120,
      render: (amount) => amount ? `¥${amount}` : '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => {
        const { color, text } = statusMap[status] || { color: 'default', text: status }
        return <Tag color={color}>{text}</Tag>
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 300,
      render: (_, record) => (
        <Space size="small" wrap>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          {record.status === 'DRAFT' && (
            <Button type="link" size="small" onClick={() => handleSubmitForApproval(record.id)}>
              提交审批
            </Button>
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
          {record.status === 'APPROVED' && !record.purchaseOrderId && (
            <Button type="link" size="small" icon={<FileAddOutlined />} onClick={() => handleOpenCreateOrder(record)}>
              生成订单
            </Button>
          )}
          {record.purchaseOrderId && (
            <Tag color="blue">已生成订单</Tag>
          )}
        </Space>
      ),
    },
  ]

  // 转换组织树为 TreeSelect 格式
  const convertToTreeData = (nodes: OrgNode[]): any[] => {
    return nodes.map(node => ({
      value: node.id,
      title: node.name,
      children: node.children ? convertToTreeData(node.children) : undefined,
    }))
  }

  return (
    <Card
      title="采购申请"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增申请
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
        title="新增采购申请"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="departmentId" label="部门" rules={[{ required: true }]}>
            <TreeSelect
              placeholder="请选择部门"
              treeData={convertToTreeData(orgTree)}
              treeDefaultExpandAll
            />
          </Form.Item>
          <Form.Item name="requirementDescription" label="需求描述" rules={[{ required: true }]}>
            <Input.TextArea rows={4} placeholder="请描述您的采购需求..." />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="采购申请详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentRequest && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="申请单号">{currentRequest.requestNo}</Descriptions.Item>
            <Descriptions.Item label="申请日期">{currentRequest.requestDate}</Descriptions.Item>
            <Descriptions.Item label="申请人">{currentRequest.applicantName}</Descriptions.Item>
            <Descriptions.Item label="部门">{currentRequest.departmentName}</Descriptions.Item>
            <Descriptions.Item label="需求描述" span={2}>
              {currentRequest.requirementDescription}
            </Descriptions.Item>
            <Descriptions.Item label="预估金额">
              {currentRequest.aiEstimatedAmount ? `¥${currentRequest.aiEstimatedAmount}` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusMap[currentRequest.status]?.color}>
                {statusMap[currentRequest.status]?.text}
              </Tag>
            </Descriptions.Item>
            {currentRequest.approverName && (
              <Descriptions.Item label="审批人">{currentRequest.approverName}</Descriptions.Item>
            )}
            {currentRequest.approvalComment && (
              <Descriptions.Item label="审批意见">{currentRequest.approvalComment}</Descriptions.Item>
            )}
            {currentRequest.purchaseOrderId && (
              <Descriptions.Item label="采购订单ID">{currentRequest.purchaseOrderId}</Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>

      <Modal
        title="生成采购订单"
        open={createOrderVisible}
        onCancel={() => setCreateOrderVisible(false)}
        onOk={() => createOrderForm.submit()}
        width={500}
      >
        <Form form={createOrderForm} layout="vertical" onFinish={handleCreateOrder}>
          <Form.Item name="supplierId" label="选择供应商" rules={[{ required: true }]}>
            <Select placeholder="请选择供应商">
              {suppliers.map(s => (
                <Select.Option key={s.id} value={s.id}>{s.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <p style={{ color: '#666', marginBottom: 0 }}>
            将根据采购申请明细自动生成采购订单
          </p>
        </Form>
      </Modal>
    </Card>
  )
}
