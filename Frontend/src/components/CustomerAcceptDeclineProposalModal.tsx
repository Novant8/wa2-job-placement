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
      .then(() => {
        if (!customerConfirm) {
          console.log("HERE CUSTOMER CONFIRM " + customerConfirm);
          API.updateJobOfferStatus(props.jobOfferId, {
            status: "SELECTION_PHASE",
          })
            .then(() => {
              API.removeCandidate(props.jobOfferId, props.candidateId)
                .then(() => {
                  API.addRefusedCandidate(props.jobOfferId, props.candidateId)
                    .then(() => {
                      props.setCustomerJobOfferDirty(true);
                    })
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
