import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { useEffect, useState } from "react";
import { JobProposal } from "../types/JobProposal.ts";
import { useAuth } from "../contexts/auth.tsx";

import CustomerAcceptDeclineProposalModal from "./CustomerAcceptDeclineProposalModal.tsx";
import { Customer } from "../types/customer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";

export default function JobProposalModalDetail(props: any) {
  const [jobProposal, setJobProposal] = useState<JobProposal>();
  const [modalAction, setModalAction] = useState("");
  const [proposalConfirmationModalShow, setProposalConfirmationModalShow] =
    useState<boolean>(false);
  const { me } = useAuth();
  const [dirty, setDirty] = useState<boolean>(false);
  const [userInfo, setUserInfo] = useState<Customer>({
    id: 0,
    contactInfo: {
      id: 0,
      name: "",
      surname: "",
      ssn: "",
      category: "UNKNOWN",
      addresses: [],
    },
  });

  if (me?.roles.includes("customer")) {
    function updateInfoField<K extends keyof Contact>(
      field: K,
      value: Contact[K],
    ) {
      setUserInfo({
        ...userInfo,
        contactInfo: {
          ...userInfo.contactInfo,
          [field]: value,
        },
      });
    }

    useEffect(() => {
      if (!me || userInfo.id > 0) return;

      const registeredRole = me.roles.find((role) =>
        ["customer", "professional"].includes(role),
      );
      if (registeredRole)
        updateInfoField(
          "category",
          registeredRole.toUpperCase() as ContactCategory,
        );

      API.getCustomerFromCurrentUser()
        .then((customer) => {
          setUserInfo(customer);
        })
        .catch((err) => console.log(err));
    }, [me, userInfo.id]);
  }

  useEffect(() => {
    if (props.professionalId === 0) return;

    API.getJobProposalbyOfferAndProfessional(
      props.jobOfferId,
      props.professionalId,
    )
      .then((data) => {
        console.log(data);
        setJobProposal(data);
      })
      .catch((err) => console.log(err));
  }, [props.professionalId, dirty]);
  /*
    const handleAcceptDecline = (status: any, professional: number) => {
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
        props.setDirty(true);
        props.onHide;
      });
  };
*/
  return (
    <Modal
      {...props}
      size="lg"
      aria-labelledby="contained-modal-title-vcenter"
      centered
    >
      {me?.roles.includes("customer") && (
        <CustomerAcceptDeclineProposalModal
          show={proposalConfirmationModalShow}
          action={modalAction}
          onHide={() => setProposalConfirmationModalShow(false)}
          customerId={userInfo.id}
          proposalId={jobProposal?.id}
          setDirty={() => setDirty(true)}
        />
      )}
      <Modal.Header closeButton>
        <Modal.Title id="contained-modal-title-vcenter">
          Job Proposal Detail
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <h4>Job Proposal: {jobProposal?.id}</h4>
        <p>
          {" "}
          Customer:{" "}
          {jobProposal?.customer.contactInfo.name +
            " " +
            jobProposal?.customer.contactInfo.surname}
        </p>
        <p>
          {" "}
          Professional:{" "}
          {jobProposal?.professional.contactInfo.name +
            " " +
            jobProposal?.professional.contactInfo.surname}
        </p>
        {me?.roles.includes("operator") && (
          <>
            <p>
              {" "}
              Confirmation by customer:{" "}
              {jobProposal?.customerConfirmation === false &&
              jobProposal?.status === "CREATED"
                ? "The customer has not decided yet"
                : jobProposal?.customerConfirmation === true
                  ? "Accepted by the customer"
                  : jobProposal?.customerConfirmation === false &&
                      jobProposal?.status === "DECLINED"
                    ? "Declined by the customer"
                    : ""}
            </p>
          </>
        )}
        {me?.roles.includes("manager") && (
          <>
            <p>
              {" "}
              Confirmation by customer:{" "}
              {!jobProposal?.customerConfirmation
                ? "The customer has not decided yet"
                : jobProposal?.customerConfirmation === true
                  ? "Accepted by the customer"
                  : "Declined by yhe customer"}
            </p>
          </>
        )}
        {me?.roles.includes("customer") && (
          <>
            <p>
              {" "}
              Confirmation by customer:{" "}
              {!jobProposal?.customerConfirmation &&
              jobProposal?.status === "CREATED" ? (
                <>
                  <Button
                    variant="success"
                    style={{ marginRight: 10 }}
                    onClick={() => {
                      setModalAction("accept");
                      setProposalConfirmationModalShow(true);
                    }}
                  >
                    Accept
                  </Button>
                  <Button
                    variant="danger"
                    onClick={() => {
                      setModalAction("decline");
                      setProposalConfirmationModalShow(true);
                    }}
                  >
                    Decline
                  </Button>
                </>
              ) : jobProposal?.customerConfirmation ? (
                "You have accepted this professional for the job offer"
              ) : !jobProposal?.customerConfirmation &&
                jobProposal?.status === "DECLINED" ? (
                "You have declined this professional for the job offer"
              ) : (
                ""
              )}
            </p>
          </>
        )}
        <p>
          {" "}
          Accepted by the professional:{" "}
          {jobProposal?.status === "ACCEPTED"
            ? "Yes"
            : jobProposal?.status === "CREATED"
              ? "Not yet accepted by the customer"
              : "No"}
        </p>
        <p>
          Contract:{" "}
          {jobProposal?.documentId ? (
            <Button variant="info">Download Job Contract</Button>
          ) : (
            "No contract yet submitted "
          )}
        </p>
      </Modal.Body>
      <Modal.Footer>
        <Button
          variant="warning"
          onClick={() => {
            props.onHide();
            //handleAcceptDecline("CANDIDATE_PROPOSAL", props.candidate.id);
          }}
        >
          Close
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
