import ReactDOM from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import { createBrowserRouter, Link, RouterProvider } from "react-router-dom";
import Homepage from "./routes/Homepage.tsx";

import EditAccount from "./routes/EditAccount.tsx";

import ViewJobOfferDetailsRecruiter from "./components/ViewJobOfferDetailRecruiter.tsx";
import CustomerInfo from "./routes/CustomerInfo.tsx";
import ProfessionaInfo from "./routes/ProfessionaInfo.tsx";
import ViewJobOfferDetailProfessional from "./components/ViewJobOfferDetailProfessional.tsx";

import SendEmail from "./routes/SendEmail.tsx";

import CustomersView from "./routes/CustomersView.tsx";

import JobOffer from "./routes/JobOffer.tsx";
import ViewJobOfferDetails from "./routes/ViewJobOfferDetails.tsx";
import ProfessionalsView from "./routes/ProfessionalsView.tsx";
import { useAuth } from "./contexts/auth.tsx";
import MessagesView from "./routes/MessagesView.tsx";

const NotLoggedView = () => {
  return (
    <>
      <div>Generic Error</div>
      <div>
        You cannot access this resources. Check your account rights or contact
        the administrator
      </div>
      <div>
        <Link to="/">Back to the homepage</Link>
      </div>
    </>
  );
};

const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { me } = useAuth(); // Ottieni lo stato di autenticazione
  const canRenderSendEmail =
    me?.principal &&
    children.type == SendEmail &&
    (me?.roles.includes("operator") || me?.roles.includes("manager"));

  const canRenderMessage =
    me?.principal &&
    children.type == MessagesView &&
    (me?.roles.includes("operator") || me?.roles.includes("manager"));

  return (
    <>
      {canRenderSendEmail ? (
        children
      ) : canRenderMessage ? (
        children
      ) : (
        <NotLoggedView />
      )}
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
          path: "/edit-account",
          element: <EditAccount />,
        },
        {
          path: "/crm/customers/",
          element: <CustomersView />,
        },
        {
          path: "/crm/customers/:customerId",
          element: <CustomerInfo />,
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
          element: (
            <ProtectedRoute>
              <SendEmail />
            </ProtectedRoute>
          ),
        },
        {
          path: "/messages",
          element: (
            <ProtectedRoute>
              <MessagesView />
            </ProtectedRoute>
          ),
        },
      ],
    },
  ],
  { basename: "/ui" },
);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <RouterProvider router={router} />,
);
