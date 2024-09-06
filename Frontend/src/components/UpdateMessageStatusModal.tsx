import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { MessageEventInterface } from "../types/message.ts";

export default function updateMessageStatusModal(props: any) {
  const handleAcceptDecline = (msgEvent: MessageEventInterface) => {
    if (!props.message) return;

    API.updateMessagestatus(props.message.id, msgEvent)
      .then(() => {
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
          {props.action === "done"
            ? "Are you sure that you want to make done this message"
            : props.action === "discard"
              ? "Are you sure that you to discard this message"
              : props.action === "processing"
                ? "Are you sure that you want to put this message in processing"
                : ""}
        </p>
      </Modal.Body>
      <Modal.Footer>
        {props.action === "done" ? (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline({ status: "DONE" });
            }}
          >
            Done
          </Button>
        ) : props.action === "discard" ? (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline({ status: "DISCARDED" });
            }}
          >
            Discard
          </Button>
        ) : props.action === "processing" ? (
          <Button
            variant="warning"
            onClick={() => {
              handleAcceptDecline({ status: "PROCESSING" });
            }}
          >
            Processing
          </Button>
        ) : (
          ""
        )}
      </Modal.Footer>
    </Modal>
  );
}
