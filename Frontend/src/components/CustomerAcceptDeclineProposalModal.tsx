import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";

export default function CustomerAcceptDeclineProposalModal(props: any) {
  const handleAcceptDecline = (customerConfirm: boolean) => {
    if (!props.proposalId) return;

    API.customerConfirmDeclineJobProposal(
      props.proposalId,
      props.customerId,
      customerConfirm,
    )
      .then(() => props.onHide)
      .catch((err) => {
        console.log(err);
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
