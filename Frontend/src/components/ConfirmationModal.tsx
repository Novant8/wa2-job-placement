import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { JobOffer, JobOfferUpdateStatus } from "../types/JobOffer.ts";
import { MessageCreate } from "../types/message.ts";
import { useEffect } from "react";

export interface ConfirmationModalProps {
  action: string;
  onHide: () => void;
  jobOffer: JobOffer;
  setDirty: () => void;
}
export default function ConfirmationModal(props: ConfirmationModalProps) {
  const handleAcceptDecline = (status: JobOfferUpdateStatus) => {
    if (!props.jobOffer) return;

    API.updateJobOfferStatus(props.jobOffer.id.toString(), status)
      .then(() => {
        //navigate(`/crm/RecruiterJobOffer/${props.jobOffer.id}`);
        props.setDirty();
        props.onHide();
      })
      .catch((error) => {
        console.log(error);
      });
    //.finally(() => );
  };

  useEffect(() => {
    console.log(props);
  }, [props]);

  const handleAcceptDeclineCustomer = (
    status: JobOfferUpdateStatus,
    msg: MessageCreate,
  ) => {
    // if (!props.jobOffer) return;

    API.updateJobOfferStatus(props.jobOffer.id.toString(), status)
      .then(() => {
        props.onHide;
      })
      .catch((error) => {
        console.log(error);
      })
      .finally(() => {
        API.createMessage(msg)
          .then(() => {})
          .catch((err) => {
            console.log(err);
          });
        props.setDirty();
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
              handleAcceptDeclineCustomer(
                { status: "ABORTED" },
                {
                  sender: { email: "EMAIL_EXAMPLE_MODAL_CONFIRM@XYZ.COM" }, //TODO: here you have a reduced customer with no addresses, retrive a complete customer so you have the addresses
                  /* email: props.jobOffer.customer.contactInfo.addresses?.find(
                      (address: { email?: string }) => address.email,
                    )?.email,*/
                  channel: "EMAIL",
                  subject:
                    "Job Offer id: " +
                    props.jobOffer.id +
                    " aborted by the customer",
                  body:
                    "Job Offer id: " +
                    props.jobOffer.id +
                    " name: " +
                    props.jobOffer.description +
                    " aborted by the customer id: " +
                    props.jobOffer.customer.id +
                    " name: " +
                    props.jobOffer.customer.contactInfo.name,
                },
              );
            }}
          >
            Abort
          </Button>
        ) : (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDeclineCustomer(
                { status: "DONE" },
                {
                  sender: { email: "EMAIL_EXAMPLE_MODAL_CONFIRM@XYZ.COM" }, //TODO: here you have a reduced customer with no addresses, retrive a complete customer so you have the addresses
                  /* email: props.jobOffer.customer.contactInfo.addresses?.find(
                     (address: { email?: string }) => address.email,
                   )?.email,*/
                  channel: "EMAIL",
                  subject:
                    "Job Offer id: " +
                    props.jobOffer.id +
                    " done by the customer",
                  body:
                    "Job Offer id: " +
                    props.jobOffer.id +
                    " name: " +
                    props.jobOffer.description +
                    " put to done by the customer id: " +
                    props.jobOffer.customer.id +
                    " name: " +
                    props.jobOffer.customer.contactInfo.name,
                },
              );
            }}
          >
            DONE
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}
