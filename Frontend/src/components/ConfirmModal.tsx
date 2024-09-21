import { ReactElement } from "react";
import { ModalProps } from "react-bootstrap";
import Modal from "react-bootstrap/Modal";
import Button from "react-bootstrap/Button";

export interface ConfirmModalProps extends ModalProps {
  title: string | ReactElement;
  onConfirm?: () => void;
  onCancel?: () => void;
}

export default function ConfirmModal({
  title,
  children,
  onConfirm,
  onCancel,
  ...props
}: ConfirmModalProps) {
  return (
    <Modal {...props} size="lg" onHide={() => onCancel?.()} centered>
      <Modal.Header closeButton>
        <Modal.Title>{title}</Modal.Title>
      </Modal.Header>
      <Modal.Body>{children}</Modal.Body>
      <Modal.Footer>
        <Button variant="danger" onClick={() => onCancel?.()}>
          Cancel
        </Button>
        <Button variant="success" onClick={() => onConfirm?.()}>
          Confirm
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
