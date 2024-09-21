import { useAuth } from "../contexts/auth.tsx";
import ViewCustomerJobOffer from "../components/ViewCustomerJobOffer.tsx";
import ViewProfessionalJobOffer from "../components/ViewProfessionalJobOffer.tsx";
import ViewRecruiterJobOffer from "../components/ViewRecruiterJobOffer.tsx";
import PageLayout from "../components/PageLayout.tsx";

export default function JobOffer() {
  const { me } = useAuth();
  return (
    <PageLayout>
      {me?.roles.includes("customer") ? (
        <ViewCustomerJobOffer />
      ) : me?.roles.includes("operator") || me?.roles.includes("manager") ? (
        <ViewRecruiterJobOffer />
      ) : (
        <ViewProfessionalJobOffer />
      )}
    </PageLayout>
  );
}
