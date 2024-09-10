import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { MessageEventInterface } from "../types/message.ts";
import { InputGroup } from "react-bootstrap";
import { useState } from "react";

export default function updateMessageStatusModal(props: any) {
  const [comment, setComment] = useState("");

  const handleAcceptDecline = (msgEvent: MessageEventInterface) => {
    if (!props.message) return;

    API.updateMessagestatus(props.message.id, msgEvent)
      .then(() => {
        props.onHide();
      })
      .catch((error) => {
        console.log(error);
      })
      .finally(() => props.setDirty());
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
                : props.action === "read"
                  ? "Are you sure that you want to put this message in read"
                  : ""}
        </p>

        <InputGroup>
          <InputGroup.Text>Insert comment</InputGroup.Text>
          <Form.Control
            as="textarea"
            aria-label="Insert comment"
            onChange={(e) => setComment(e.target.value)}
          />
        </InputGroup>
      </Modal.Body>

      <Modal.Footer>
        {props.action === "done" ? (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline({ status: "DONE", comments: comment });
            }}
          >
            Done
          </Button>
        ) : props.action === "discard" ? (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline({ status: "DISCARDED", comments: comment });
            }}
          >
            Discard
          </Button>
        ) : props.action === "processing" ? (
          <Button
            variant="warning"
            onClick={() => {
              handleAcceptDecline({ status: "PROCESSING", comments: comment });
            }}
          >
            Processing
          </Button>
        ) : props.action === "read" ? (
          <Button
            variant="info"
            onClick={() => {
              handleAcceptDecline({ status: "READ", comments: comment });
            }}
          >
            Read
          </Button>
        ) : (
          ""
        )}
      </Modal.Footer>
    </Modal>
  );
}
