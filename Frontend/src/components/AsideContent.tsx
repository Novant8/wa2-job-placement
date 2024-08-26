import CreateJobOffer from "./CreateJobOffer.tsx";
import CandidateManagement from "./CandidateManagement.tsx";
import CustomerRelationshipManagement from "./CustomerRelationshipManagement.tsx";
import ViewCustomerJobOffer from "./ViewCustomerJobOffer.tsx";
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

    return (
        <div>
            {selectedItem === SelectedItem.CreateJobOffer && <CreateJobOffer />}
            {selectedItem === SelectedItem.CandidateManagement && <CandidateManagement />}
            {selectedItem === SelectedItem.CustomerRelationshipManagement && <CustomerRelationshipManagement/>}
            {selectedItem === SelectedItem.ViewJobOffers && <ViewCustomerJobOffer />}
            {!selectedItem && <p>Please select an option from the aside.</p>}
        </div>
    )
}