import CreateJobOffer from "./CreateJobOffer.tsx";
import CandidateManagement from "./CandidateManagement.tsx";
enum SelectedItem {
    ViewJobOffers = 'ViewJobOffers',
    CreateJobOffer = 'CreateJobOffer',
    CandidateManagement = 'CandidateManagement'
}
interface AsideContentProps {
    selectedItem: SelectedItem | null;
}

export default function AsideContent({selectedItem}: AsideContentProps){

    return (
        <div>
            {selectedItem === SelectedItem.CreateJobOffer && <CreateJobOffer />}
            {selectedItem === SelectedItem.CandidateManagement && <CandidateManagement />}
            {!selectedItem && <p>Please select an option from the aside.</p>}
        </div>
    )
}