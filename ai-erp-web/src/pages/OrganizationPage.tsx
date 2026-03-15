import { useEffect, useState } from 'react'
import { Card, Tree, Button, Space, Modal, Form, Input, Select, message, Spin } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { organizationApi } from '../services/api'

interface OrgNode {
  id: number
  key: number
  title: string
  name: string
  code: string
  type: string
  parentId: number | null
  status: string
  children?: OrgNode[]
}

export default function OrganizationPage() {
  const [treeData, setTreeData] = useState<OrgNode[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingOrg, setEditingOrg] = useState<OrgNode | null>(null)
  const [form] = Form.useForm()

  const fetchTree = async () => {
    setLoading(true)
    try {
      const response: any = await organizationApi.tree()
      const convertToTreeData = (nodes: any[]): OrgNode[] =>
        nodes.map((node) => ({
          ...node,
          key: node.id,
          title: node.name,
          name: node.name,
          children: node.children ? convertToTreeData(node.children) : undefined,
        }))
      setTreeData(convertToTreeData(response.data || []))
    } catch (error) {
      message.error('获取组织树失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchTree()
  }, [])

  const handleAdd = (parentNode?: OrgNode) => {
    setEditingOrg(null)
    form.resetFields()
    if (parentNode) {
      form.setFieldsValue({ parentId: parentNode.id })
    }
    setModalVisible(true)
  }

  const handleEdit = (node: OrgNode) => {
    setEditingOrg(node)
    form.setFieldsValue(node)
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await organizationApi.delete(id)
      message.success('删除成功')
      fetchTree()
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingOrg) {
        await organizationApi.update(editingOrg.id, values)
        message.success('更新成功')
      } else {
        await organizationApi.create(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchTree()
    } catch (error: any) {
      message.error(error.message || '操作失败')
    }
  }

  const renderTreeNodes = (nodes: OrgNode[]): any[] =>
    nodes.map((node) => ({
      key: node.id,
      title: (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
          <span>
            {node.name}
            <span style={{ color: '#999', marginLeft: 8, fontSize: 12 }}>
              ({node.code})
            </span>
          </span>
          <Space size="small" onClick={(e) => e.stopPropagation()}>
            <Button type="link" size="small" onClick={() => handleAdd(node)}>
              添加子组织
            </Button>
            <Button type="link" size="small" onClick={() => handleEdit(node)}>
              编辑
            </Button>
            <Button type="link" size="small" danger onClick={() => handleDelete(node.id)}>
              删除
            </Button>
          </Space>
        </div>
      ),
      children: node.children ? renderTreeNodes(node.children) : undefined,
    }))

  return (
    <Card
      title="组织管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
          新增组织
        </Button>
      }
    >
      <Spin spinning={loading}>
        <Tree
          showLine
          treeData={renderTreeNodes(treeData)}
          defaultExpandAll
        />
      </Spin>

      <Modal
        title={editingOrg ? '编辑组织' : '新增组织'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="code" label="组织编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="name" label="组织名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="组织类型" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="COMPANY">公司</Select.Option>
              <Select.Option value="BRANCH">分公司</Select.Option>
              <Select.Option value="DEPARTMENT">部门</Select.Option>
            </Select>
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
