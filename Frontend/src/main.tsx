import ReactDOM from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import {
  createBrowserRouter,
  RouterProvider,
  useNavigate,
} from "react-router-dom";
import Homepage from "./routes/Homepage.tsx";
import Crm from "./routes/Crm.tsx";
import EditAccount from "./routes/EditAccount.tsx";
import ViewJobOfferDetails from "./routes/ViewJobOfferDetails.tsx";
import ViewJobOfferDetailsRecruiter from "./routes/ViewJobOfferDetailRecruiter.tsx";
import CustomerInfo from "./routes/CustomerInfo.tsx";
import ProfessionaInfo from "./routes/ProfessionaInfo.tsx";
import ViewJobOfferDetailProfessional from "./routes/ViewJobOfferDetailProfessional.tsx";
import { useAuth } from "./contexts/auth.tsx";
import { useEffect } from "react";
import SendEmail from "./routes/SendEmail.tsx";

const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { me } = useAuth(); // Ottieni lo stato di autenticazione
  const navigate = useNavigate();

  useEffect(() => {
    if (me?.principal) {
      navigate("/crm");
    }
  }, [me, navigate]);

  return !me?.principal ? children : null;
};

const router = createBrowserRouter(
  [
    {
      element: <App />,
      children: [
        {
          path: "/",
          element: (
            <ProtectedRoute>
              <Homepage />
            </ProtectedRoute>
          ),
        },
        {
          path: "/crm",
          element: <Crm />,
        },
        {
          path: "/edit-account",
          element: <EditAccount />,
        },
        {
          path: "/crm/jobOffer/:jobOfferId",
          element: <ViewJobOfferDetails />,
        },
        {
          path: "/crm/customers/:customerId",
          element: <CustomerInfo />,
        },
        {
          path: "/crm/professionals/:professionalId",
          element: <ProfessionaInfo />,
        },
        {
          path: "/crm/RecruiterJobOffer/:jobOfferId",
          element: <ViewJobOfferDetailsRecruiter />,
        },
        {
          path: "/crm/ProfessionalJobOffer/:jobOfferId",
          element: <ViewJobOfferDetailProfessional />,
        },
        {
          path: "/communication-manager",
          element: <SendEmail />,
        },
      ],
    },
  ],
  { basename: "/ui" },
);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <RouterProvider router={router} />,
);
