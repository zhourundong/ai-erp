import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 对于SSE流式响应，需要禁用代理缓冲
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            // 对于流式请求，设置必要的头
            proxyReq.setHeader('Connection', 'keep-alive')
            proxyReq.setHeader('Cache-Control', 'no-cache')
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            // 对于SSE流式响应，禁用缓冲
            if (req.url?.includes('/stream')) {
              proxyRes.headers['cache-control'] = 'no-cache'
              proxyRes.headers['connection'] = 'keep-alive'
              proxyRes.headers['x-accel-buffering'] = 'no'
            }
          })
        },
      },
    },
  },
})
