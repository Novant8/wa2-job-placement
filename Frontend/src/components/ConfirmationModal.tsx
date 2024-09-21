import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { JobOffer, JobOfferUpdateStatus } from "../types/JobOffer.ts";

import { useEffect, useState } from "react";
import { EmailAddress, isEmailAddress } from "../types/address.ts";
import { sendEmailStruct } from "../types/sendEmail.ts";

export interface ConfirmationModalProps {
  action: string;
  onHide: () => void;
  jobOffer: JobOffer | undefined;
  setDirty: () => void;
  show: boolean;
}

export default function ConfirmationModal(props: ConfirmationModalProps) {
  /* const handleAcceptDecline = (status: JobOfferUpdateStatus) => {
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
  };*/
  const [customerMail, setCustomerMail] = useState("");
  const [_professionalMail, setProfessionalMail] = useState("");

  useEffect(() => {
    API.getCustomerById(props.jobOffer?.customer.id)
      .then((c) => {
        let mail = c.contactInfo?.addresses
          .filter((a) => isEmailAddress(a))
          .map((a) => a as EmailAddress)[0].email;
        setCustomerMail(mail);
      })
      .catch((error) => {
        console.error("Error fetching customer by ID:", error);
      });
  }, []);

  useEffect(() => {
    if (props.jobOffer?.professional == undefined) return;
    API.getProfessionalById(props.jobOffer?.professional.id)
      .then((c) => {
        let mail = c.contactInfo?.addresses
          .filter((a) => isEmailAddress(a))
          .map((a) => a as EmailAddress)[0].email;
        setProfessionalMail(mail);
      })
      .catch((error) => {
        console.error("Error fetching professional by ID:", error);
      });
  }, []);

  const handleAcceptDecline = (
    status: JobOfferUpdateStatus,
    msg: sendEmailStruct[],
  ) => {
    console.log(msg);
    API.updateJobOfferStatus(props.jobOffer?.id.toString(), status)
      .then(() => {
        props.onHide();
      })
      .catch((error) => {
        console.log(error);
      })
      .finally(() => {
        for (let m of msg) {
          API.sendEmail(m)
            .then(() => {})
            .catch((err) => {
              console.log(err);
            });
        }

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
              handleAcceptDecline({ status: "SELECTION_PHASE" }, [
                {
                  to: customerMail,
                  subject:
                    "Update on Job Offer" +
                    "[" +
                    props.jobOffer?.description +
                    "]",
                  body:
                    "Dear customer. We want to inform you that the Job Offer " +
                    "[" +
                    props.jobOffer?.description +
                    "]" +
                    "has been accepted and now it is in SELECTION PHASE. Best Regards",
                },
              ]);
            }}
          >
            Accept
          </Button>
        ) : props.action === "decline" ? (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline({ status: "ABORTED" }, [
                {
                  to: customerMail,
                  subject:
                    "Updated on Job Offer: " + props.jobOffer?.description,
                  body:
                    "Dear Customer " +
                    props.jobOffer?.customer.contactInfo.name +
                    " " +
                    props.jobOffer?.customer.contactInfo.surname +
                    "," +
                    "We want to inform you that the Job [ " +
                    props.jobOffer?.description +
                    "]is been marked ABORTED",
                },
              ]);
            }}
          >
            Decline
          </Button>
        ) : props.action === "abort" ? (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline({ status: "ABORTED" }, [
                {
                  to: customerMail,
                  subject:
                    "Updated on Job Offer: " + props.jobOffer?.description,
                  body:
                    "Dear Customer " +
                    props.jobOffer?.customer.contactInfo.name +
                    " " +
                    props.jobOffer?.customer.contactInfo.surname +
                    "," +
                    "We want to inform you that the Job [ " +
                    props.jobOffer?.description +
                    "]is been marked ABORTED",
                },
              ]);
            }}
          >
            Abort
          </Button>
        ) : (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline({ status: "DONE" }, [
                {
                  to: customerMail,
                  subject:
                    "Updated on Job Offer " + props.jobOffer?.description,
                  body:
                    "Dear professional " +
                    props.jobOffer?.professional.contactInfo.name +
                    " " +
                    props.jobOffer?.professional.contactInfo.surname +
                    "." +
                    "We want to inform you that the Job [ " +
                    props.jobOffer?.description +
                    " ]" +
                    " proposed by customer [" +
                    props.jobOffer?.customer.contactInfo.name +
                    " " +
                    props.jobOffer?.customer.contactInfo.name +
                    "]is been marked COMPLETED",
                },
              ]);
            }}
          >
            DONE
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}
