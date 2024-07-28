import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import Homepage from "./routes/Homepage.tsx";

const router = createBrowserRouter([
    {
        element: <App />,
        children: [
            {
                path: "/",
                element: <Homepage />
            }
        ]
    }
], { basename: "/ui" });

ReactDOM.createRoot(document.getElementById('root')!).render(
    <RouterProvider router={router} />
)
