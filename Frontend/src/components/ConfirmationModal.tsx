import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal';
import * as API from "../../API.tsx";
import {JobOfferUpdateStatus} from "../types/JobOffer.ts";
import {useNavigate} from "react-router-dom";


export default function ConfirmationModal(props:any) {
    const navigate = useNavigate();


    const handleAcceptDecline = (status: string) => {
        if (!props.jobOffer) return;

        const jobOfferUpdateStatus: JobOfferUpdateStatus = {
            status: status

        };

        API.updateJobOfferStatus(props.jobOffer.id, jobOfferUpdateStatus)
            .then(() => {
                //navigate(`/crm/RecruiterJobOffer/${props.jobOffer.id}`);
                props.onHide
            })
            .catch((error) => {
                console.log(error);
            }).finally(()=>window.location.reload());
    };


    return (
        <Modal
            {...props}
            size="lg"
            aria-labelledby="contained-modal-title-vcenter"
            centered
        >
            <Modal.Header closeButton>
                <Modal.Title id="contained-modal-title-vcenter">
                    Confirmation
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>

                <p>
                    { props.action==="accept" ?
                    "Are you sure that you to accept to handle the job offer, after that you will be unable to modify it"
                        : "Are you sure that you to decline to handle the job offer, after that the job offer will be aborted"
                    }
                </p>
            </Modal.Body>
            <Modal.Footer>
                { props.action==="accept" ?
                <Button variant="success" onClick={()=>{

                    handleAcceptDecline("SELECTION_PHASE")

                }}>Accept</Button>
                    :
                <Button variant="danger" onClick={()=>{

                    handleAcceptDecline("ABORTED")

                }}>Decline</Button>

                }
            </Modal.Footer>
        </Modal>
    );
}