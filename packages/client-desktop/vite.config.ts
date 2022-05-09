import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import { ViteEjsPlugin } from "vite-plugin-ejs";
import analyze from 'rollup-plugin-analyzer';
import {resolve} from 'path';
import viteCompression from 'vite-plugin-compression';

export default defineConfig({
  plugins: [ViteEjsPlugin(), vue(), viteCompression()],
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
      input: {
        main: resolve(__dirname, 'index.html'),
        splash: resolve(__dirname, 'splash.html')
      },
      plugins: [analyze({
        summaryOnly: true
      })]
    }
  },

});