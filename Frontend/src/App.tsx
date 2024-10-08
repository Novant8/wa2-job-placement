import React from "react";
import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import { Outlet } from "react-router-dom";
import { AuthContextProvider } from "./contexts/auth.tsx";

function App() {
  return (
    <React.StrictMode>
      <AuthContextProvider>
        <Outlet />
      </AuthContextProvider>
    </React.StrictMode>
  );
}

export default App;
