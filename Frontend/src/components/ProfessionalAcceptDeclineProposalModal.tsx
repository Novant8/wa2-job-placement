import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { MessageCreate } from "../types/message.ts";
import { useEffect } from "react";

export default function ProfessionalAcceptDeclineProposalModal(props: any) {
  const handleAcceptDecline = (
    professionalConfirm: boolean,
    msg: MessageCreate,
  ) => {
    if (!props.proposalId) return;

    API.professionalConfirmDeclineJobProposal(
      props.proposalId,
      props.professionalId,
      professionalConfirm,
    )
      .then(() => {
        if (!professionalConfirm) {
          API.updateJobOfferStatus(props.jobOfferId, {
            status: "SELECTION_PHASE",
          })
            .then(() => {
              API.removeCandidate(props.jobOfferId, props.professionalId)
                .then(() => {
                  API.addRefusedCandidate(
                    props.jobOfferId,
                    props.professionalId,
                  )
                    .then(() => {
                      props.setProfessionalJobOfferDirty(true); //setCustomerJobOfferDirty(true);
                    })
                    .catch((err) => {
                      console.log(err);
                    });
                })
                .catch((err) => console.log(err));
            })
            .catch((err) => console.log(err));
        } else {
          API.updateJobOfferStatus(props.jobOfferId, {
            status: "CONSOLIDATED",
          }).catch((err) => console.log(err));
        }
        props.setProfessionalDirty();
        props.onHide();
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => {
        API.createMessage(msg)
          .then(() => {})
          .catch((err) => {
            console.log(err);
          });
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
            ? "Are you sure that you to accept the proposed Job Offer? Remember to upload the signed contract "
            : "Are you sure that you to decline the proposed job offer?"}
        </p>
      </Modal.Body>
      <Modal.Footer>
        {props.action === "accept" ? (
          <Button
            variant="success"
            onClick={() => {
              handleAcceptDecline(true, {
                sender: {
                  email: props.professionalInfo.contactInfo.addresses?.find(
                    (address: { email?: string }) => address.email,
                  )?.email,
                },
                channel: "EMAIL",
                subject:
                  "Proposal id: " +
                  props.proposalId +
                  " accepted by the professional",
                body:
                  "Proposal id: " +
                  props.proposalId +
                  " accepted by the professional id: " +
                  props.professionalId +
                  " name: " +
                  props.professionalInfo.contactInfo.name +
                  " for the job offer " +
                  props.jobOffer.description +
                  " now the job offer is CONSOLIDATED",
              });
            }}
          >
            Accept
          </Button>
        ) : (
          <Button
            variant="danger"
            onClick={() => {
              handleAcceptDecline(false, {
                sender: {
                  email: props.professionalInfo.contactInfo.addresses?.find(
                    (address: { email?: string }) => address.email,
                  )?.email,
                },
                channel: "EMAIL",
                subject:
                  "Proposal id: " +
                  props.proposalId +
                  " declined by the professional",
                body:
                  "Proposal id: " +
                  props.proposalId +
                  " accepted by the professional id: " +
                  props.professionalId +
                  " name: " +
                  props.professionalInfo.contactInfo.name +
                  " for the job offer " +
                  props.jobOffer.description,
              });
            }}
          >
            Decline
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}
