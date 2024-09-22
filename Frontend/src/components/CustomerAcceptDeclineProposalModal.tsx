import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { JobOffer } from "../types/JobOffer.ts";
import { Customer } from "../types/customer.ts";
interface CustomerAcceptDeclineProposalModalProps {
  show: boolean; // Controls visibility of the modal
  action: string; // Action type (likely a string that represents the action, e.g., 'accept' or 'decline')
  onHide: () => void; // Callback to hide the modal
  customerId: number; // ID of the customer
  customerInfo: Customer; // The customer object containing detailed information
  proposalId?: number; // ID of the proposal, possibly undefined
  jobOffer?: JobOffer; // Job offer details, possibly undefined
  jobOfferId?: number; // ID of the job offer, possibly undefined
  candidateId?: number; // ID of the candidate, possibly undefined
  setDirty: () => void; // Callback to toggle the dirty state
  setProposalOnHide: () => void; // Callback to handle hiding the proposal modal
}
export default function CustomerAcceptDeclineProposalModal(
  props: CustomerAcceptDeclineProposalModalProps,
) {
  const handleAcceptDecline = (customerConfirm: boolean) => {
    if (!props.proposalId) return;

    API.customerConfirmDeclineJobProposal(
      props.proposalId,
      props.customerId,
      customerConfirm,
    )
      .then(() => {
        if (!customerConfirm) {
          API.updateJobOfferStatus(props.jobOfferId?.toString(), {
            status: "SELECTION_PHASE",
          })
            .then(() => {
              API.removeCandidate(props.jobOfferId, props.candidateId)
                .then(() => {
                  API.addRefusedCandidate(props.jobOfferId, props.candidateId)
                    .then(() => {})
                    .catch((err) => {
                      console.log(err);
                    });
                })
                .catch((err) => console.log(err));
            })
            .catch((err) => console.log(err));
        }
        props.onHide();
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => {
        props.setDirty();
        props.setProposalOnHide();
      });
  };

  return (
    <Modal
      show={props.show}
      onHide={props.onHide}
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
            ? "Are you sure that you to accept this candidate for this job offer?"
            : "Are you sure that you to decline this candidate for this job offer?"}
        </p>
      </Modal.Body>
      <Modal.Footer>
        {props.action === "accept" ? (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline(true);
            }}
          >
            Accept
          </Button>
        ) : (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline(false);
            }}
          >
            Decline
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}
