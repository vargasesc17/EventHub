import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: { proxy: {
    '/api/eventos': { target: 'http://localhost:8081', changeOrigin: true, rewrite: path => path.replace(/^\/api/, '') },
    '/api/boleteria': { target: 'http://localhost:8082', changeOrigin: true, rewrite: path => path.replace(/^\/api\/boleteria/, '') },
  } },
});
