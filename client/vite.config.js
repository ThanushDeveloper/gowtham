import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react-swc'


// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [react()],
    server: {
      proxy: mode === "development" ? {
        "/api": {
          // Use provided backend URL, else default to local Spring Boot
          target: env.VITE_PRODUCTION_BACKEND_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          // rewrite: (path) => path.replace(/^\/api/, '')
        }
      } : {}
    }
  }
})
