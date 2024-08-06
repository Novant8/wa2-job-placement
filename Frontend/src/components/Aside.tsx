import {ListGroup} from "react-bootstrap";

enum SelectedItem {
    ViewJobOffers = 'ViewJobOffers',
    CreateJobOffer = 'CreateJobOffer',
    CandidateManagement = 'CandidateManagement'
}
interface AsideProps {
    onSelect: (item: SelectedItem) => void;
}

export default function  Aside ({ onSelect }:AsideProps)  {
    return (
        <div>
            <h2>CRM</h2>
            <ListGroup>

                <ListGroup.Item className="my-3" action onClick={() => onSelect(SelectedItem.ViewJobOffers)}>
                    View Job Offers
                </ListGroup.Item>

                <ListGroup.Item action onClick={() => onSelect(SelectedItem.CreateJobOffer)}>
                    Create Job Offer
                </ListGroup.Item>

                <ListGroup.Item action onClick={() => onSelect(SelectedItem.CandidateManagement)}>
                    Candidate Management
                </ListGroup.Item>

            </ListGroup>
        </div>
    );
};
