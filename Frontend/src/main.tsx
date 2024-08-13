import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import Homepage from "./routes/Homepage.tsx";
import Crm from "./routes/Crm.tsx";
import EditAccount from "./routes/EditAccount.tsx";
import ViewJobOfferDetails from "./routes/ViewJobOfferDetails.tsx";

const router = createBrowserRouter([
    {
        element: <App />,
        children: [
            {
                path: "/",
                element: <Homepage />
            },
            {
                path: "/crm",
                element: <Crm />
            },
            {
                path: "/edit-account",
                element: <EditAccount />
            },
            {
                path: "/crm/jobOffer/:jobOfferId",
                element: <ViewJobOfferDetails />
            }
        ]
    }
], { basename: "/ui" });

ReactDOM.createRoot(document.getElementById('root')!).render(
    <RouterProvider router={router} />
)
