import CreateJobOffer from "./CreateJobOffer.tsx";
import CandidateManagement from "./CandidateManagement.tsx";
import CustomerRelationshipManagement from "./CustomerRelationshipManagement.tsx";
import ViewCustomerJobOffer from "./ViewCustomerJobOffer.tsx";
import ViewRecruiterJobOffer from "./ViewRecruiterJobOffer.tsx";
import {useAuth} from "../contexts/auth.tsx";
enum SelectedItem {
    ViewJobOffers = 'ViewJobOffers',
    CreateJobOffer = 'CreateJobOffer',
    CandidateManagement = 'CandidateManagement',
    CustomerRelationshipManagement= 'Customer Relationship Management'
}
interface AsideContentProps {
    selectedItem: SelectedItem | null;
}

export default function AsideContent({selectedItem}: AsideContentProps){
    const {me} = useAuth();

    return (
        <div>
            {selectedItem === SelectedItem.CreateJobOffer && <CreateJobOffer />}
            {selectedItem === SelectedItem.CandidateManagement && <CandidateManagement />}
            {selectedItem === SelectedItem.CustomerRelationshipManagement && <CustomerRelationshipManagement/>}
            {
                me?.roles.includes("customer")?
                selectedItem === SelectedItem.ViewJobOffers && <ViewCustomerJobOffer />
                    : me?.roles.includes("operator")? selectedItem === SelectedItem.ViewJobOffers && <ViewRecruiterJobOffer />
                    :""
            }
            {!selectedItem && <p>Please select an option from the aside.</p>}
        </div>
    )
}