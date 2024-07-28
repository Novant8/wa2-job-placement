import React from "react";
import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import {Outlet} from "react-router-dom";
import TopNavbar from "./components/TopNavbar.tsx";
import {AuthContextProvider} from "./contexts/auth.tsx";

function App() {
    return (
        <React.StrictMode>
            <AuthContextProvider>
                <TopNavbar />
                <Outlet />
            </AuthContextProvider>
        </React.StrictMode>
    )
}

export default App
