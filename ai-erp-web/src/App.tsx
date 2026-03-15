import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './stores/authStore'
import LoginPage from './pages/LoginPage'
import MainLayout from './layouts/MainLayout'

function App() {
  const { token } = useAuthStore()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={
          token ? <Navigate to="/" replace /> : <LoginPage />
        } />
        <Route path="/*" element={
          token ? <MainLayout /> : <Navigate to="/login" replace />
        } />
      </Routes>
    </BrowserRouter>
  )
}

export default App
