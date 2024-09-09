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

/*const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { me } = useAuth(); // Ottieni lo stato di autenticazione
  const navigate = useNavigate();

   useEffect(() => {
    if (me?.principal) {
      navigate("/ui");
    }
  }, [me, navigate]);
  console.log(!me?.principal);
  return !me?.principal ? children : null;
};*/

const router = createBrowserRouter(
  [
    {
      element: <App />,
      children: [
        {
          path: "/",
          element: (
            // <ProtectedRoute>
            <Homepage />
            // </ProtectedRoute>
          ),
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
