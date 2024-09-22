import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";

import { Professional } from "../types/professional.ts";
import { JobOffer } from "../types/JobOffer.ts";
interface ProfessionalAcceptDeclineProposalModalProps {
  show: boolean; // Controls visibility of the modal
  action: string; // The action (like 'accept' or 'decline')
  onHide: () => void; // Callback to hide the modal
  proposalId?: number; // Proposal ID, possibly undefined
  jobOffer?: JobOffer; // Job offer object, possibly undefined
  jobOfferId?: number; // ID of the job offer, possibly undefined
  professionalId?: number; // ID of the professional (candidate), possibly undefined
  candidateId?: number; // Candidate ID, possibly undefined (same as professionalId)
  professionalInfo?: Professional; // Professional info, possibly undefined
  setDirty: () => void; // Callback to toggle the dirty state
  setProposalOnHide: () => void; // Callback for hiding the proposal modal
}
export default function ProfessionalAcceptDeclineProposalModal(
  props: ProfessionalAcceptDeclineProposalModalProps,
) {
  const handleAcceptDecline = (professionalConfirm: boolean) => {
    if (!props.proposalId) return;

    API.professionalConfirmDeclineJobProposal(
      props.proposalId,
      props.professionalId,
      professionalConfirm,
    )
      .then(() => {
        if (!professionalConfirm) {
          API.updateJobOfferStatus(props.jobOfferId?.toString(), {
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
                      //setCustomerJobOfferDirty(true);
                    })
                    .catch((err) => {
                      console.log(err);
                    });
                })
                .catch((err) => console.log(err));
            })
            .catch((err) => console.log(err));
        } else {
          API.updateJobOfferStatus(props.jobOfferId?.toString(), {
            status: "CONSOLIDATED",
          }).catch((err) => console.log(err));
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
      onHide={props.onHide}
      show={props.show}
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
