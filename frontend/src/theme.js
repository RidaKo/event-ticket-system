import { createTheme } from "@mantine/core";

export const theme = createTheme({
  primaryColor: "brand",
  fontFamily:
    "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  colors: {
    brand: [
      "#eef1f8",
      "#dde2f0",
      "#bcc6de",
      "#98a7cb",
      "#7789b9",
      "#6479af",
      "#5a71ab",
      "#4b6095",
      "#415684",
      "#334972",
    ],
  },
  defaultRadius: "md",
});
