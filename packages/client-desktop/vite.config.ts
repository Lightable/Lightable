import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import { ViteEjsPlugin } from "vite-plugin-ejs";
import analyze from 'rollup-plugin-analyzer';

export default defineConfig({
  plugins: [ViteEjsPlugin(), vue()],
  server: {
    port: 8080
  },
  resolve: {
    alias: [
      {
        find: '@',
        replacement: path.resolve(__dirname, 'src')
      }
    ]
  },
  build: {
    chunkSizeWarningLimit: 600,
    cssCodeSplit: true,
    reportCompressedSize: true,
    rollupOptions: {
      treeshake: true,
      
      plugins: [analyze({
        summaryOnly: true
      })]
    }
  },

});