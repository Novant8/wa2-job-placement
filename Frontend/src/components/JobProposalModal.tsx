import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { JobOfferUpdateStatus } from "../types/JobOffer.ts";
import { sendEmailStruct } from "../types/sendEmail.ts";
import { useEffect, useState } from "react";
import { EmailAddress, isEmailAddress } from "../types/address.ts";

export default function JobProposalModal(props: any) {
  const [customerMail, setCustomerMail] = useState("");
  const [professionalMail, setProfessionalMail] = useState("");

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
    API.getProfessionalById(props.candidate.id)
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
    status: any,
    professional: number,
    msg: sendEmailStruct[],
  ) => {
    if (!props.jobOffer) return;

    const jobOfferUpdateStatus: JobOfferUpdateStatus = {
      status: status,
      professionalId: professional,
    };

    API.createJobProposal(
      props.jobOffer.customer.id,
      props.jobOffer.id,
      props.candidate.id,
    )
      .then(() => {
        API.updateJobOfferStatus(props.jobOffer.id, jobOfferUpdateStatus)
          .then(() => {
            //navigate(`/crm/RecruiterJobOffer/${props.jobOffer.id}`);
          })
          .catch((error) => {
            console.log(error);
          });
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
        props.onHide();
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
        <p>Are you sure that you to propose this candidate to the job offer?</p>
      </Modal.Body>
      <Modal.Footer>
        <Button
          variant="success"
          onClick={() => {
            handleAcceptDecline("CANDIDATE_PROPOSAL", props.candidate.id, [
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
                  "has a valid candidate and now it is in CANDIDATE_PROPOSAL PHASE.  Best Regards",
              },
              {
                to: professionalMail,
                subject:
                  "New Job proposal" + "[" + props.jobOffer?.description + "]",
                body:
                  "Dear professional.  We want to inform you have been selected for the  Job Offer " +
                  "[" +
                  props.jobOffer?.description +
                  "]" +
                  "please enter in the platform for see the progress of the job proposal.  Best Regards",
              },
            ]);
          }}
        >
          Propose
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
