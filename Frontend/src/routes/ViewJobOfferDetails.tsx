import { useAuth } from "../contexts/auth.tsx";
import ViewJobOfferDetailsCustomers from "../components/ViewJobOfferDetailsCustomers.tsx";
import ViewJobOfferDetailsRecruiter from "../components/ViewJobOfferDetailRecruiter.tsx";
import ViewJobOfferDetailProfessional from "../components/ViewJobOfferDetailProfessional.tsx";
import PageLayout from "../components/PageLayout.tsx";

export default function ViewJobOfferDetails() {
  const { me } = useAuth();
  return (
    <PageLayout>
      {me?.roles.includes("customer") ? (
        <ViewJobOfferDetailsCustomers />
      ) : me?.roles.includes("operator") || me?.roles.includes("manager") ? (
        <ViewJobOfferDetailsRecruiter />
      ) : (
        <ViewJobOfferDetailProfessional />
      )}
    </PageLayout>
  );
}
