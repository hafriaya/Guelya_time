import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        // Guelya Time Dark Theme - Matching Desktop App
        bg: {
          primary: "#0f0f0f",
          secondary: "#1a1a1a",
          tertiary: "#252525",
        },
        accent: {
          primary: "#7b5bf5",
          secondary: "#5e43d8",
          hover: "#8e6fff",
          light: "rgba(123, 91, 245, 0.15)",
        },
        border: {
          DEFAULT: "#3a3a3a",
          hover: "#4a4a4a",
          focus: "#7b5bf5",
        },
        text: {
          primary: "#ffffff",
          secondary: "#b3b3b3",
          muted: "#666666",
        },
        status: {
          success: "#2ecc71",
          warning: "#f39c12",
          error: "#e74c3c",
        },
      },
      fontFamily: {
        sans: ["Segoe UI", "SF Pro Display", "Helvetica Neue", "Arial", "sans-serif"],
      },
      animation: {
        "fade-in": "fadeIn 0.3s ease-in-out",
        "slide-up": "slideUp 0.3s ease-out",
        "slide-in-right": "slideInRight 0.3s ease-out",
      },
      keyframes: {
        fadeIn: {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        slideUp: {
          "0%": { opacity: "0", transform: "translateY(10px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        slideInRight: {
          "0%": { opacity: "0", transform: "translateX(100%)" },
          "100%": { opacity: "1", transform: "translateX(0)" },
        },
      },
    },
  },
  plugins: [],
};

export default config;
