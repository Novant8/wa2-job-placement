import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import * as API from "../../API.tsx";
import { useEffect, useState } from "react";
import { JobProposal } from "../types/JobProposal.ts";
import { useAuth, UserRole } from "../contexts/auth.tsx";

import CustomerAcceptDeclineProposalModal from "./CustomerAcceptDeclineProposalModal.tsx";
import { Customer } from "../types/customer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";
import ProfessionalAcceptDeclineProposalModal from "./ProfessionalAcceptDeclineProposalModal.tsx";
import { Professional } from "../types/professional.ts";
import { UploadDocumentField } from "./UploadDocumentField.tsx";
import { Accordion, ButtonGroup, Spinner } from "react-bootstrap";
import { DocumentHistory } from "../types/documents.ts";
import { TiTick, TiTimes } from "react-icons/ti";
import { BiInfoCircle, BiSolidHourglass } from "react-icons/bi";
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
  const [loadingSignedDocument, setLoadingSignedDocument] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorDocument, setErrorDocument] = useState("");
  const [errorSignedDocument, setErrorSignedDocument] = useState("");
  const [documentHistory, setDocumentHistory] = useState<
    DocumentHistory | undefined
  >();
  const [signedDocumentHistory, setSignedDocumentHistory] = useState<
    DocumentHistory | undefined
  >();
  const [customerConfirm, setCustomerConfirm] = useState(
    jobProposal?.customerConfirmation,
  );
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
    setLoading(true);
    API.getJobProposalbyOfferAndProfessional(
      props.jobOfferId,
      props.professionalId,
    )
      .then((data) => {
        setJobProposal(data);
        setCustomerConfirm(data.customerConfirmation);
        if (data.documentId) {
          API.getJobProposalDocumentHistory(data.id).then((history) => {
            setDocumentHistory(history);
          });
        }

        if (data.professionalSignedContract) {
          API.getJobProposalSignedContractHistory(data.id).then((history) => {
            setSignedDocumentHistory(history);
          });
        }
      })
      .catch((err) => console.log(err))
      .finally(() => {
        setDirty(false);
        setLoading(false);
      });
  }, [props.professionalId, dirty]);

  function uploadOrUpdateContract(document: File) {
    if (!jobProposal) return;

    let promise;
    if (jobProposal.documentId)
      promise = API.updateDocument(jobProposal.documentId, document, me!);
    else promise = API.uploadDocument(document, me!);

    const prevDocument = jobProposal.documentId;
    setJobProposal({ ...jobProposal, documentId: null });
    setLoadingDocument(true);
    promise
      .then((document) => {
        API.loadJobProposalDocument(jobProposal.id, document.historyId).then(
          (proposal) => {
            setJobProposal(proposal);
            setDirty(true);
          },
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

  function uploadOrUpdateSignedContract(document: File) {
    if (!jobProposal) return;

    let promise;
    if (jobProposal.professionalSignedContract)
      promise = API.updateDocument(
        jobProposal.professionalSignedContract,
        document,
        me!,
      );
    else promise = API.uploadDocument(document, me!);

    const prevDocument = jobProposal.professionalSignedContract;
    setJobProposal({ ...jobProposal, professionalSignedContract: null });
    setLoadingSignedDocument(true);
    promise
      .then((document) => {
        API.loadJobProposalSignedDocument(
          jobProposal.id,
          document.historyId,
        ).then((proposal) => {
          setJobProposal(proposal);
          setDirty(true);
        });
      })
      .catch((err) => {
        setJobProposal({
          ...jobProposal,
          professionalSignedContract: prevDocument,
        });
        setErrorSignedDocument(err);
      })
      .finally(() => {
        setLoadingSignedDocument(false);
      });
  }

  function deleteContract(documentId: number) {
    if (!jobProposal) return;
    setLoadingDocument(true);
    API.loadJobProposalDocument(jobProposal.id, null)
      .then((proposal) => {
        setJobProposal(proposal);
        API.deleteDocumentHistory(documentId);
        setDirty(true);
      })
      .catch((err) => {
        console.log(err);
        setErrorDocument(err);
      })
      .finally(() => setLoadingDocument(false));
  }

  function deleteSignedContract(documentId: number) {
    if (!jobProposal) return;
    setLoadingSignedDocument(true);
    API.loadJobProposalSignedDocument(jobProposal.id, null)
      .then((proposal) => {
        setJobProposal(proposal);
        API.deleteDocumentHistory(documentId);
        setDirty(true);
      })
      .catch((err) => {
        console.log(err);
        setErrorSignedDocument(err);
      })
      .finally(() => setLoadingSignedDocument(false));
  }

  if (loading) {
    return <Spinner />;
  }
  return (
    <Modal
      {...props}
      size="lg"
      aria-labelledby="contained-modal-title-vcenter"
      centered
    >
      {me?.roles.includes("customer") &&
        customerProposalConfirmationModalShow && (
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
            setProposalOnHide={props.onHide}
          />
        )}
      {me?.roles.includes("professional") &&
        professionalProposalConfirmationModalShow && (
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
            setProfessionalDirty={props.setProfessionalDirty}
          />
        )}
      <Modal.Header closeButton>
        <Modal.Title id="contained-modal-title-vcenter">
          {jobProposal?.jobOffer.description}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {" "}
        <strong> Customer:</strong>{" "}
        {jobProposal?.customer.contactInfo.name +
          " " +
          jobProposal?.customer.contactInfo.surname}
        <br />
        <strong> Professional:</strong>{" "}
        {jobProposal?.professional.contactInfo.name +
          " " +
          jobProposal?.professional.contactInfo.surname}
        <hr />
        Accepted by the customer:{" "}
        {jobProposal?.customerConfirmation === false &&
        jobProposal?.status === "CREATED" ? (
          <BiSolidHourglass size={20} />
        ) : jobProposal?.customerConfirmation === true ? (
          <TiTick size={20} />
        ) : jobProposal?.customerConfirmation === false &&
          jobProposal?.status === "DECLINED" ? (
          <TiTimes size={20} />
        ) : (
          ""
        )}
        <br />
        Accepted by the professional:{" "}
        {jobProposal?.status === "ACCEPTED" ? (
          <TiTick size={20} />
        ) : jobProposal?.status === "CREATED" ? (
          <BiSolidHourglass size={20} />
        ) : (
          <TiTimes size={20} />
        )}{" "}
        <hr />{" "}
        {me?.roles.includes("customer") && (
          <>
            {" "}
            Contract for the professional:
            <UploadDocumentField
              documentId={jobProposal?.documentId}
              getDocumentHistory={() =>
                API.getJobProposalDocumentHistory(jobProposal!.id)
              }
              getDocumentHistoryDataUrl={() =>
                `/crm/API/jobProposals/${jobProposal?.id}/document/data`
              }
              getDocumentVersionDataUrl={(_, versionId) =>
                `/crm/API/jobProposals/${jobProposal?.id}/document/version/${versionId}/data`
              }
              loading={loadingDocument}
              error={errorDocument}
              onUpload={uploadOrUpdateContract}
              onDelete={deleteContract}
              customerView={true}
              customerConfirm={customerConfirm ? customerConfirm : false}
              professionalView={false}
              professionalConfirm={false}
            />
          </>
        )}
        {me?.roles.includes("customer") && (
          <>
            <p>
              {" "}
              {!jobProposal?.customerConfirmation &&
              jobProposal?.status === "CREATED" ? (
                <>
                  For accept the candidate, please upload a contract, otherwise
                  if you are not interessed in the candidate decline the offer :{" "}
                  <br />
                  <Button
                    variant="success"
                    style={{ marginRight: 10 }}
                    disabled={!jobProposal.documentId}
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
              ) : (
                <></>
              )}
            </p>
          </>
        )}
        {me?.roles.includes("professional") && (
          <>
            {" "}
            Contract for the professional:{" "}
            {!jobProposal?.customerConfirmation ? (
              <>Not yet available, Wait for a customer's decisions</>
            ) : (
              <></>
            )}
            <UploadDocumentField
              documentId={jobProposal?.documentId}
              getDocumentHistory={() =>
                API.getJobProposalDocumentHistory(jobProposal!.id)
              }
              getDocumentHistoryDataUrl={() =>
                `/crm/API/jobProposals/${jobProposal?.id}/document/data`
              }
              getDocumentVersionDataUrl={(_, versionId) =>
                `/crm/API/jobProposals/${jobProposal?.id}/document/version/${versionId}/data`
              }
              loading={loadingDocument}
              error={errorDocument}
              onUpload={uploadOrUpdateContract}
              onDelete={deleteContract}
              customerView={false}
              customerConfirm={customerConfirm ? customerConfirm : false}
              professionalView={false}
              professionalConfirm={false}
            />
            {professionalInfo.employmentState == "EMPLOYED" &&
            jobProposal?.jobOffer.offerStatus != "CONSOLIDATED" &&
            jobProposal?.customerConfirmation ? (
              <>
                <div style={{ color: "red", textDecoration: "underline" }}>
                  <BiInfoCircle color={"red"} size={30} />
                  You are EMPLOYED and you cannot be enrolled in two jobs
                  simultaneously.
                  <BiInfoCircle color={"red"} size={30} />
                </div>
              </>
            ) : (
              <></>
            )}
            {me?.roles.includes("professional") &&
              (professionalInfo.employmentState == "UNEMPLOYED" ||
                jobProposal?.jobOffer.offerStatus == "CONSOLIDATED") &&
              jobProposal?.customerConfirmation && (
                <>
                  <hr />
                  <p>
                    Signed contract by the professional:{" "}
                    <UploadDocumentField
                      documentId={jobProposal?.professionalSignedContract}
                      getDocumentHistory={() =>
                        API.getJobProposalSignedContractHistory(jobProposal!.id)
                      }
                      getDocumentHistoryDataUrl={() =>
                        `/crm/API/jobProposals/${jobProposal?.id}/document/signed/data`
                      }
                      getDocumentVersionDataUrl={(_, versionId) =>
                        `/crm/API/jobProposals/${jobProposal?.id}/document/signed/version/${versionId}/data`
                      }
                      loading={loadingSignedDocument}
                      error={errorSignedDocument}
                      onUpload={uploadOrUpdateSignedContract}
                      onDelete={deleteSignedContract}
                      customerView={false}
                      customerConfirm={false}
                      professionalView={true}
                      professionalConfirm={jobProposal?.status == "ACCEPTED"}
                    />
                  </p>
                </>
              )}
            {me?.roles.includes("professional") &&
              professionalInfo.employmentState == "UNEMPLOYED" && (
                <p>
                  {" "}
                  {jobProposal?.status === "CREATED" &&
                  jobProposal.customerConfirmation ? (
                    <>
                      No contract yet submitted, upload it if you want to accept
                      the Job Proposal:{" "}
                      <Button
                        variant="success"
                        style={{ marginRight: 10 }}
                        disabled={!jobProposal.professionalSignedContract}
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
          </>
        )}
        {["operator", "manager"].some((role) =>
          me?.roles.includes(role as UserRole),
        ) &&
          (jobProposal?.documentId ? (
            <>
              <div className="my-2">
                Contract for the professional:
                <span className="mx-3 my-auto">
                  <strong>{documentHistory?.versions[0].name}</strong>
                </span>
                <ButtonGroup>
                  <Button
                    variant="primary"
                    as="a"
                    href={`/crm/API/jobProposals/${jobProposal?.id}/document/data`}
                    target="_blank"
                  >
                    View
                  </Button>
                </ButtonGroup>
              </div>
              {documentHistory && documentHistory.versions.length > 1 && (
                <Accordion>
                  <Accordion.Item eventKey="0">
                    <Accordion.Header>Show all versions</Accordion.Header>
                    <Accordion.Body>
                      {documentHistory.versions.map((document) => {
                        return (
                          <div className="my-2">
                            <span className="mx-3 my-auto">
                              <strong>{document.name}</strong>
                            </span>
                            <ButtonGroup>
                              <Button
                                variant="primary"
                                as="a"
                                href={`/crm/API/jobProposals/${jobProposal?.id}/document/version/${document.versionId}/data`}
                                target="_blank"
                              >
                                View
                              </Button>
                            </ButtonGroup>
                          </div>
                        );
                      })}
                    </Accordion.Body>
                  </Accordion.Item>
                </Accordion>
              )}
            </>
          ) : (
            "No contract yet submitted "
          ))}
        {["operator", "manager", "customer"].some((role) =>
          me?.roles.includes(role as UserRole),
        ) &&
          jobProposal?.customerConfirmation && (
            <div>
              <hr />

              {jobProposal?.professionalSignedContract ? (
                <div className="my-2">
                  Signed contract by the professional:
                  <span className="mx-3 my-auto">
                    <strong>{signedDocumentHistory?.versions[0].name}</strong>
                  </span>
                  <ButtonGroup>
                    <Button
                      variant="primary"
                      as="a"
                      href={`/crm/API/jobProposals/${jobProposal?.id}/document/signed/data`}
                      target="_blank"
                    >
                      View
                    </Button>
                  </ButtonGroup>
                </div>
              ) : (
                "Signed contract by the professional: No contract yet submitted"
              )}

              {signedDocumentHistory &&
                signedDocumentHistory.versions.length > 1 && (
                  <Accordion>
                    <Accordion.Item eventKey="0">
                      <Accordion.Header>Show all versions</Accordion.Header>
                      <Accordion.Body>
                        {signedDocumentHistory.versions.map((document) => (
                          <div className="my-2" key={document.versionId}>
                            <span className="mx-3 my-auto">
                              <strong>{document.name}</strong>
                            </span>
                            <ButtonGroup>
                              <Button
                                variant="primary"
                                as="a"
                                href={`/crm/API/jobProposals/${jobProposal?.id}/document/signed/version/${document.versionId}/data`}
                                target="_blank"
                              >
                                View
                              </Button>
                            </ButtonGroup>
                          </div>
                        ))}
                      </Accordion.Body>
                    </Accordion.Item>
                  </Accordion>
                )}
            </div>
          )}
      </Modal.Body>
      <Modal.Footer />
    </Modal>
  );
}
