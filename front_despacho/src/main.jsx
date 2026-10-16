import React from "react";
import ReactDOM from "react-dom/client";
import { MsalProvider } from "@azure/msal-react";
import App from "./App.jsx";
import { msalInstance } from "./msal.js";
import "./api/axiosAuth.js";
import "./index.css";

export { msalInstance };

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <MsalProvider instance={msalInstance}>
      <App />
    </MsalProvider>
  </React.StrictMode>
);
