import CreateJobOffer from "./CreateJobOffer.tsx";
import ProfessionalsView from "./ProfessionalsView.tsx";
import CustomerRelationshipManagement from "./CustomersView.tsx";
import ViewCustomerJobOffer from "./ViewCustomerJobOffer.tsx";
import ViewRecruiterJobOffer from "./ViewRecruiterJobOffer.tsx";
import { useAuth } from "../contexts/auth.tsx";
import ViewProfessionalJobOffer from "./ViewProfessionalJobOffer.tsx";
import MessagesView from "./MessagesView.tsx";
enum SelectedItem {
  ViewJobOffers = "ViewJobOffers",
  CreateJobOffer = "CreateJobOffer",
  Professionals = "Professionals",
  Customers = "Customers",
  Messages = "Messages",
}
interface AsideContentProps {
  selectedItem: SelectedItem | null;
}

export default function AsideContent({ selectedItem }: AsideContentProps) {
  const { me } = useAuth();

  return (
    <div>
      {selectedItem === SelectedItem.CreateJobOffer && <CreateJobOffer />}
      {selectedItem === SelectedItem.Professionals && <ProfessionalsView />}
      {selectedItem === SelectedItem.Messages && <MessagesView />}
      {selectedItem === SelectedItem.Customers && (
        <CustomerRelationshipManagement />
      )}
      {me?.roles.includes("customer")
        ? selectedItem === SelectedItem.ViewJobOffers && (
            <ViewCustomerJobOffer />
          )
        : me?.roles.includes("operator")
          ? selectedItem === SelectedItem.ViewJobOffers && (
              <ViewRecruiterJobOffer />
            )
          : me?.roles.includes("professional")
            ? selectedItem === SelectedItem.ViewJobOffers && (
                <ViewProfessionalJobOffer />
              )
            : ""}
      {!selectedItem && <p>Please select an option from the aside.</p>}
    </div>
  );
}
