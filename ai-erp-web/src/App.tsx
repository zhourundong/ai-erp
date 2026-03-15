import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './stores/authStore'
import LoginPage from './pages/LoginPage'
import MainLayout from './layouts/MainLayout'
import ChatPage from './pages/ChatPage'
import UserManagePage from './pages/UserManagePage'
import OrganizationPage from './pages/OrganizationPage'
import SupplierPage from './pages/SupplierPage'
import CustomerPage from './pages/CustomerPage'
import ProductPage from './pages/ProductPage'
import WarehousePage from './pages/WarehousePage'
import InventoryPage from './pages/InventoryPage'
import InventoryTransactionPage from './pages/InventoryTransactionPage'
import PurchaseOrderPage from './pages/PurchaseOrderPage'
import SalesOrderPage from './pages/SalesOrderPage'

function App() {
  const { token } = useAuthStore()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={
          token ? <Navigate to="/" replace /> : <LoginPage />
        } />
        <Route path="/" element={
          token ? <MainLayout /> : <Navigate to="/login" replace />
        }>
          <Route index element={<Navigate to="/chat" replace />} />
          <Route path="chat" element={<ChatPage />} />
          <Route path="users" element={<UserManagePage />} />
          <Route path="organizations" element={<OrganizationPage />} />
          <Route path="products" element={<ProductPage />} />
          <Route path="suppliers" element={<SupplierPage />} />
          <Route path="customers" element={<CustomerPage />} />
          <Route path="warehouses" element={<WarehousePage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="inventory-transactions" element={<InventoryTransactionPage />} />
          <Route path="purchase-orders" element={<PurchaseOrderPage />} />
          <Route path="sales-orders" element={<SalesOrderPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
