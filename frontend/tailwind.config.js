/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        surface: {
          DEFAULT: "#f4f5f9",
          card: "#ffffff",
          sunken: "#eef0f6",
        },
        ink: {
          DEFAULT: "#1f2a44",
          muted: "#6b7490",
          subtle: "#9aa3bd",
        },
        accent: {
          DEFAULT: "#1b2340",
        },
      },
      boxShadow: {
        card: "0 1px 2px rgba(15, 23, 42, 0.04), 0 4px 12px rgba(15, 23, 42, 0.04)",
        cardHover: "0 2px 6px rgba(15, 23, 42, 0.08), 0 12px 24px rgba(15, 23, 42, 0.08)",
      },
      fontFamily: {
        sans: [
          "Inter",
          "ui-sans-serif",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "Roboto",
          "sans-serif",
        ],
      },
    },
  },
  plugins: [],
};
