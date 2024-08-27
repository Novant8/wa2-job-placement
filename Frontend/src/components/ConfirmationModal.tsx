import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal';

export default function ConfirmationModal(props:any) {
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
                    Are you sure that you to accept to handle the job offer, after that you will be unable to modify it
                </p>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="success" onClick={props.onHide}>Accept</Button>
            </Modal.Footer>
        </Modal>
    );
}