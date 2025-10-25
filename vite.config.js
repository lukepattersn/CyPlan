import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  root: '.',
  publicDir: 'public',
  build: {
    outDir: 'src/main/resources/static',
    emptyOutDir: false, // Don't delete existing files in static
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html')
      },
      output: {
        entryFileNames: 'js/[name]-[hash].js',
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          if (assetInfo.name.endsWith('.css')) {
            return 'css/[name]-[hash][extname]';
          }
          return 'assets/[name]-[hash][extname]';
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/addCourse': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/removeCourse': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/changeAcademicPeriod': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/generateSchedules': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/nextSchedule': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/previousSchedule': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
