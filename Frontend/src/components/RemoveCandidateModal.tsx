import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";

export default function RemoveCandidateModal(props: any) {
  const handleRemoveCandidate = () => {
    if (!props.jobOffer || !props.candidate) return;

    API.removeCandidate(props.jobOffer.id, props.candidate.id)
      .then(() => {
        props.setDirty(true);
        props.onHide();
      })
      .catch((error) => {
        console.log(error);
      });
    //.finally(() => window.location.reload());
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
          Remove Candidate
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p>
          Are you sure that you to remove{" "}
          {props.candidate.name + " " + props.candidate.surname} as a candidate
          for job offer?
        </p>
      </Modal.Body>
      <Modal.Footer>
        <Button
          variant="danger"
          onClick={() => {
            handleRemoveCandidate();
          }}
        >
          Remove
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
