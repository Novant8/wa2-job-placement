import ReactDOM from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import {
  createBrowserRouter,
  Link,
  RouterProvider,
  useNavigate,
} from "react-router-dom";
import Homepage from "./routes/Homepage.tsx";
import Crm from "./routes/Crm.tsx";
import EditAccount from "./routes/EditAccount.tsx";

import ViewJobOfferDetailsRecruiter from "./components/ViewJobOfferDetailRecruiter.tsx";
import CustomerInfo from "./routes/CustomerInfo.tsx";
import ProfessionaInfo from "./routes/ProfessionaInfo.tsx";
import ViewJobOfferDetailProfessional from "./components/ViewJobOfferDetailProfessional.tsx";

import SendEmail from "./routes/SendEmail.tsx";
import TopNavbar from "./components/TopNavbar.tsx";
import CustomersView from "./routes/CustomersView.tsx";

import JobOffer from "./routes/JobOffer.tsx";
import ViewJobOfferDetails from "./routes/ViewJobOfferDetails.tsx";
import ProfessionalsView from "./routes/ProfessionalsView.tsx";
import { useAuth } from "./contexts/auth.tsx";
import { useEffect } from "react";
import { Button } from "react-bootstrap";

const NotLoggedView = () => {
  const navigate = useNavigate();
  return (
    <>
      <div>Generic Error</div>
      <div>
        You cannot access this resources. Check your account rights or contact
        the administrator
      </div>
      <div>
        <Link onClick={() => navigate("/")}>Back to the homepage</Link>
      </div>
    </>
  );
};

const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { me } = useAuth(); // Ottieni lo stato di autenticazione
  return me?.principal ? (
    children
  ) : (
    <>
      <NotLoggedView />
    </>
  );
};

const router = createBrowserRouter(
  [
    {
      element: <App />,
      children: [
        {
          path: "/",
          element: <Homepage />,
        },
        {
          path: "/crm",
          element: (
            <>
              <TopNavbar />
              <Crm />
            </>
          ),
        },
        {
          path: "/edit-account",
          element: <EditAccount />,
        },
        {
          path: "/crm/customers/",
          element: (
            <ProtectedRoute>
              <CustomersView />
            </ProtectedRoute>
          ),
        },
        {
          path: "/crm/customers/:customerId",
          element: (
            <ProtectedRoute>
              <CustomerInfo />
            </ProtectedRoute>
          ),
        },
        {
          path: "/crm/job-offers/",
          element: <JobOffer />,
        },
        {
          path: "/crm/job-offers/:jobOfferId",
          element: <ViewJobOfferDetails />,
        },
        {
          path: "/crm/professionals/",
          element: <ProfessionalsView />,
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
