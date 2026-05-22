import React from "react";
import ReactDOM from "react-dom/client";
import { MantineProvider } from "@mantine/core";
import { DatesProvider } from "@mantine/dates";
import App from "./App.jsx";
import { theme } from "./theme.js";
import "@mantine/core/styles.css";
import "@mantine/dates/styles.css";
import "./styles.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="light">
      <DatesProvider settings={{ firstDayOfWeek: 1, consistentWeeks: true }}>
        <App />
      </DatesProvider>
    </MantineProvider>
  </React.StrictMode>
);
