import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            // Rollup'a @/src yolunu gösterir
            '@': path.resolve(__dirname, './src'),
        },
    },
    // 💡 MDI ve diğer external kütüphane hatalar?n? önler
    build: {
        rollupOptions: {
            external: [
                '@mdi/js'
            ]
        }
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            }
        }
    }
});