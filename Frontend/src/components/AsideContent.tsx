import CreateJobOffer from "./CreateJobOffer.tsx";
//import ViewCustomerJobOffer from "./ViewCustomerJobOffer.tsx";
enum SelectedItem {
    ViewJobOffers = 'ViewJobOffers',
    CreateJobOffer = 'CreateJobOffer'
}
interface AsideContentProps {
    selectedItem: SelectedItem | null;
}

export default function AsideContent({selectedItem}: AsideContentProps){

    return (
        <div>
            {selectedItem === SelectedItem.CreateJobOffer && <CreateJobOffer />}
            {!selectedItem && <p>Please select an option from the aside.</p>}
        </div>
    )
}