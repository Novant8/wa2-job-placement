import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { JobOfferUpdateStatus } from "../types/JobOffer.ts";

export default function ConfirmationModal(props: any) {
  const handleAcceptDecline = (status: JobOfferUpdateStatus) => {
    if (!props.jobOffer) return;

    /*const jobOfferUpdateStatus: JobOfferUpdateStatus = {
      status: status,
    };*/

    API.updateJobOfferStatus(props.jobOffer.id, status)
      .then(() => {
        //navigate(`/crm/RecruiterJobOffer/${props.jobOffer.id}`);
        props.onHide;
      })
      .catch((error) => {
        console.log(error);
      })
      .finally(() => props.setDirty);
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
          {props.action === "accept"
            ? "Are you sure that you to accept to handle the job offer, after that you will be unable to modify it"
            : props.action === "decline"
              ? "Are you sure that you to decline to handle the job offer, after that the job offer will be aborted"
              : props.action === "done"
                ? "Are you sure that you want to make the job offer done?"
                : "Are you sure that you want to make the job offer aborted?"}
        </p>
      </Modal.Body>
      <Modal.Footer>
        {props.action === "accept" ? (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline({ status: "SELECTION_PHASE" });
            }}
          >
            Accept
          </Button>
        ) : props.action === "decline" ? (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline({ status: "ABORTED" });
            }}
          >
            Decline
          </Button>
        ) : props.action === "abort" ? (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline({ status: "ABORTED" });
            }}
          >
            Abort
          </Button>
        ) : (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline({ status: "DONE" });
            }}
          >
            DONE
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}
