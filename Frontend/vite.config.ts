import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: "/ui",
  server: {
    hmr: {
      port: 5174,
      host: "localhost",
      protocol: "ws",
    },
  },
});
