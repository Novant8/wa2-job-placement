import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { useEffect, useState } from "react";
import { JobProposal } from "../types/JobProposal.ts";
import { useAuth } from "../contexts/auth.tsx";

import CustomerAcceptDeclineProposalModal from "./CustomerAcceptDeclineProposalModal.tsx";
import { Customer } from "../types/customer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";
import ProfessionalAcceptDeclineProposalModal from "./ProfessionalAcceptDeclineProposalModal.tsx";
import { Professional } from "../types/professional.ts";
import { UploadDocumentField } from "./UploadDocumentField.tsx";
import { ApiError } from "../../API.tsx";

export default function JobProposalModalDetail(props: any) {
  const [jobProposal, setJobProposal] = useState<JobProposal>();
  const [modalAction, setModalAction] = useState("");
  const [
    customerProposalConfirmationModalShow,
    setCustomerProposalConfirmationModalShow,
  ] = useState<boolean>(false);
  const [
    professionalProposalConfirmationModalShow,
    setProfessionalProposalConfirmationModalShow,
  ] = useState<boolean>(false);

  const [loadingDocument, setLoadingDocument] = useState(false);
  const [errorDocument, setErrorDocument] = useState("");
  const { me } = useAuth();
  const [dirty, setDirty] = useState<boolean>(false);
  const [customerInfo, setCustomerInfo] = useState<Customer>({
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

  const [professionalInfo, setProfessionalInfo] = useState<Professional>({
    id: 0,
    contactInfo: {
      id: 0,
      name: "",
      surname: "",
      ssn: "",
      category: "UNKNOWN",
      addresses: [],
    },
    location: "",
    skills: [],
    dailyRate: 0,
    employmentState: "NOT_AVAILABLE",
  });

  function updateInfoField<K extends keyof Contact>(
    field: K,
    value: Contact[K],
    role: string,
  ) {
    if (role == "customer") {
      setCustomerInfo({
        ...customerInfo,
        contactInfo: {
          ...customerInfo.contactInfo,
          [field]: value,
        },
      });
    } else {
      setProfessionalInfo({
        ...professionalInfo,
        contactInfo: {
          ...professionalInfo.contactInfo,
          [field]: value,
        },
      });
    }
  }

  if (me?.roles.includes("customer")) {
    useEffect(() => {
      if (!me || customerInfo.id > 0) return;

      const registeredRole = me.roles.find((role) =>
        ["customer", "professional"].includes(role),
      );

      if (registeredRole)
        updateInfoField(
          "category",
          registeredRole.toUpperCase() as ContactCategory,
          registeredRole,
        );

      API.getCustomerFromCurrentUser()
        .then((customer) => {
          setCustomerInfo(customer);
        })
        .catch((err) => console.log(err));
    }, [me, customerInfo.id]);
  }

  if (me?.roles.includes("professional")) {
    useEffect(() => {
      if (!me || customerInfo.id > 0) return;

      const registeredRole = me.roles.find((role) =>
        ["customer", "professional"].includes(role),
      );

      if (registeredRole)
        updateInfoField(
          "category",
          registeredRole.toUpperCase() as ContactCategory,
          registeredRole,
        );

      API.getProfessionalFromCurrentUser()
        .then((professional) => {
          setProfessionalInfo(professional);
        })
        .catch((err) => console.log(err));
    }, [me, professionalInfo.id]);
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

  function uploadOrUpdateContract(document: File) {
    if (!jobProposal) return;

    let promise;
    if (jobProposal.documentId)
      promise = API.updateDocument(jobProposal.documentId, document);
    else promise = API.uploadDocument(document);

    const prevDocument = jobProposal.documentId;
    setJobProposal({ ...jobProposal, documentId: null });
    setLoadingDocument(true);
    promise
      .then((document) => {
        API.loadJobProposalDocument(jobProposal.id, document.historyId).then(
          (proposal) => setJobProposal(proposal),
        );
      })
      .catch((err) => {
        setJobProposal({ ...jobProposal, documentId: prevDocument });
        setErrorDocument(err);
      })
      .finally(() => {
        setLoadingDocument(false);
      });
  }

  function deleteContract(documentId: number) {
    if (!jobProposal) return;
    setLoadingDocument(true);
    API.loadJobProposalDocument(jobProposal.id, null)
      .then((proposal) => {
        setJobProposal(proposal);
        API.deleteDocumentHistory(documentId);
      })
      .catch((err) => {
        console.log(err);
        setErrorDocument(err);
      })
      .finally(setLoadingDocument(false));
  }

  return (
    <Modal
      {...props}
      size="lg"
      aria-labelledby="contained-modal-title-vcenter"
      centered
    >
      {me?.roles.includes("customer") && (
        <CustomerAcceptDeclineProposalModal
          show={customerProposalConfirmationModalShow}
          action={modalAction}
          onHide={() => setCustomerProposalConfirmationModalShow(false)}
          customerId={customerInfo.id}
          customerInfo={customerInfo}
          proposalId={jobProposal?.id}
          jobOffer={jobProposal?.jobOffer}
          jobOfferId={jobProposal?.jobOffer.id}
          candidateId={jobProposal?.professional.id}
          setDirty={() => setDirty(true)}
          setCustomerJobOfferDirty={props.setCustomerJobOfferDirty}
          setProposalOnHide={props.onHide}
        />
      )}
      {me?.roles.includes("professional") && (
        <ProfessionalAcceptDeclineProposalModal
          show={professionalProposalConfirmationModalShow}
          action={modalAction}
          onHide={() => setProfessionalProposalConfirmationModalShow(false)}
          proposalId={jobProposal?.id}
          jobOffer={jobProposal?.jobOffer}
          jobOfferId={jobProposal?.jobOffer.id}
          professionalId={jobProposal?.professional.id}
          candidateId={jobProposal?.professional.id}
          professionalInfo={jobProposal?.professional}
          setDirty={() => setDirty(true)}
          setProfessionalJobOfferDirty={() => {
            props.setProfessionalJobOfferDirty;
          }}
          setProposalOnHide={props.onHide}
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
                      setCustomerProposalConfirmationModalShow(true);
                    }}
                  >
                    Accept
                  </Button>
                  <Button
                    variant="danger"
                    onClick={() => {
                      setModalAction("decline");
                      setCustomerProposalConfirmationModalShow(true);
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
        {me?.roles.includes("professional") && (
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
        {me?.roles.includes("operator") && (
          <>
            <p>
              {" "}
              Accepted by the professional:{" "}
              {jobProposal?.status === "ACCEPTED"
                ? "Yes"
                : jobProposal?.status === "CREATED"
                  ? "Not yet accepted by the professional"
                  : "No"}
            </p>
          </>
        )}
        {me?.roles.includes("customer") && (
          <>
            <p>
              {" "}
              Accepted by the professional:{" "}
              {jobProposal?.status === "ACCEPTED"
                ? "Yes"
                : jobProposal?.status === "CREATED"
                  ? "Not yet accepted by the professional"
                  : "No"}
            </p>
          </>
        )}
        {me?.roles.includes("manager") && (
          <p>
            {" "}
            Accepted by the professional:{" "}
            {jobProposal?.status === "ACCEPTED"
              ? "Yes"
              : jobProposal?.status === "CREATED"
                ? "Not yet accepted by the professional"
                : "No"}
          </p>
        )}
        {me?.roles.includes("professional") && (
          <p>
            {" "}
            Accept the job proposal:{" "}
            {jobProposal?.status === "ACCEPTED" ? (
              "You have already accepted this job proposal"
            ) : jobProposal?.status === "CREATED" &&
              !jobProposal.customerConfirmation ? (
              "Not yet accepted by the customer you can't accept it yet"
            ) : jobProposal?.status === "CREATED" &&
              !jobProposal.customerConfirmation ? (
              "You can't accept it because has been declined by the customer"
            ) : jobProposal?.status === "CREATED" &&
              jobProposal.customerConfirmation ? (
              <>
                <Button
                  variant="success"
                  style={{ marginRight: 10 }}
                  onClick={() => {
                    setModalAction("accept");
                    setProfessionalProposalConfirmationModalShow(true);
                  }}
                >
                  Accept
                </Button>
                <Button
                  variant="danger"
                  onClick={() => {
                    setModalAction("decline");
                    setProfessionalProposalConfirmationModalShow(true);
                  }}
                >
                  Decline
                </Button>
              </>
            ) : (
              ""
            )}
          </p>
        )}

        <p>
          Contract:{" "}
          {me?.roles.includes("customer") && (
            <UploadDocumentField
              documentId={jobProposal?.documentId}
              loading={loadingDocument}
              error={errorDocument}
              onUpload={uploadOrUpdateContract}
              onDelete={deleteContract}
            />
          )}
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
