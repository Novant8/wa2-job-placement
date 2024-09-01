import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { JobOfferUpdateStatus } from "../types/JobOffer.ts";

export default function JobProposalModal(props: any) {
  const handleAcceptDecline = (status: any, professional: number) => {
    if (!props.jobOffer) return;

    const jobOfferUpdateStatus: JobOfferUpdateStatus = {
      status: status,
      professionalId: professional,
    };

    API.createJobProposal(
      props.jobOffer.customer.id,
      props.jobOffer.id,
      props.candidate.id,
    )
      .then(() => {
        API.updateJobOfferStatus(props.jobOffer.id, jobOfferUpdateStatus)
          .then(() => {
            //navigate(`/crm/RecruiterJobOffer/${props.jobOffer.id}`);
          })
          .catch((error) => {
            console.log(error);
          });
      })
      .catch((error) => {
        console.log(error);
      })
      .finally(() => {
        props.setDirty(true);
        props.onHide();
      });
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
        <p>Are you sure that you to propose this candidate to the job offer?</p>
      </Modal.Body>
      <Modal.Footer>
        <Button
          variant="success"
          onClick={() => {
            handleAcceptDecline("CANDIDATE_PROPOSAL", props.candidate.id);
          }}
        >
          Propose
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
