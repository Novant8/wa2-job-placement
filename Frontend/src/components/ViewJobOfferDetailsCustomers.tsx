import React, { useEffect, useState } from "react";
import {
  Button,
  Card,
  Col,
  Container,
  Form,
  InputGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { JobOffer, JobOfferCreate } from "../types/JobOffer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { Customer } from "../types/customer.ts";
import JobProposalModalDetail from "./JobProposalDetailModal.tsx";
import ConfirmationModal from "./ConfirmationModal.tsx";
import { FaCircleArrowLeft } from "react-icons/fa6";
import * as Icon from "react-bootstrap-icons";
import JobOfferBadge from "./Badges/JobOfferBadge.tsx";
import { CiZoomIn } from "react-icons/ci";
import { MdDelete } from "react-icons/md";

type Candidate = {
  id: number;
  name: string;
  surname: string;
  cvDocument?: number;
};
export default function ViewJobOfferDetailsCustomers() {
  const [isEditable, setIsEditable] = useState(false);
  const [jobOffer, setJobOffer] = useState<JobOffer>();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [newSkill, setNewSkill] = useState<string>("");
  const [dirty, setDirty] = useState(false);
  const [modalShow, setModalShow] = useState<boolean>(false);
  const [modalAction, setModalAction] = useState<string>("");
  const navigate = useNavigate();
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
  const { jobOfferId } = useParams();
  const { me } = useAuth();
  const [jobProposalDetailModalShow, setJobProposalDetailModalShow] =
    useState<boolean>(false);
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate>({
    id: 0,
    name: "",
    surname: "",
    cvDocument: undefined,
  });

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
    if (!me) return;

    const registeredRole = me.roles.find((role) =>
      ["customer", "professional"].includes(role),
    );
    if (registeredRole)
      updateInfoField(
        "category",
        registeredRole.toUpperCase() as ContactCategory,
      );

    setLoading(true);
    API.getCustomerFromCurrentUser()
      .then((customer) => {
        setUserInfo(customer);
        API.getJobOfferDetails(jobOfferId)
          .then((data) => {
            setJobOffer(data);
            setDirty(false);
          })
          .catch(() => {
            setError("Failed to fetch job offer details");
          });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [me, dirty]);

  const handleEditClick = () => {
    setIsEditable(!isEditable);
  };

  const handleSubmit = () => {
    if (!jobOffer) return;

    const updatedJobOffer: JobOfferCreate = {
      description: jobOffer.description,
      duration: jobOffer.duration,
      notes: jobOffer.notes,
      requiredSkills: jobOffer.requiredSkills,
    };

    API.updateJobOffer(jobOfferId, updatedJobOffer)
      .then(() => {
        setIsEditable(false);
      })
      .catch(() => {
        setError("Failed to update job offer");
      });
  };

  const handleInputChange = (field: keyof JobOffer, value: any) => {
    setJobOffer({
      ...jobOffer!,
      [field]: value,
    });
  };

  const handleSkillChange = (index: number, value: string) => {
    const updatedSkills = [...jobOffer!.requiredSkills];
    updatedSkills[index] = value;
    setJobOffer({
      ...jobOffer!,
      requiredSkills: updatedSkills,
    });
  };
  const handleAddSkill = () => {
    if (newSkill.trim()) {
      setJobOffer({
        ...jobOffer!,
        requiredSkills: [...jobOffer!.requiredSkills, newSkill.trim()],
      });
      setNewSkill("");
    }
  };

  const handleRemoveSkill = (index: number) => {
    const updatedSkills = jobOffer!.requiredSkills.filter(
      (_, i) => i !== index,
    );
    setJobOffer({
      ...jobOffer!,
      requiredSkills: updatedSkills,
    });
  };

  function handleViewDocument(documentId: number) {
    API.getDocumentHistory(documentId)
      .then((history) => {
        let document = history.versions[0];
        const url = `/document-store/API/documents/${document.historyId}/version/${document.versionId}/data`;
        window.open(url, "_blank");
      })
      .catch(() => "Error");
  }

  if (loading) {
    return (
      <Container className="text-center mt-5">
        <Spinner animation="border" />
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="text-center mt-5">
        <p>{error}</p>
      </Container>
    );
  }

  return (
    <>
      <Card>
        <Card.Header>
          <Card.Title>
            <Row className="justify-content-begin">
              <Col xs={4}>
                <Button
                  className="d-flex align-items-center text-sm-start"
                  onClick={() => navigate(-1)}
                >
                  <FaCircleArrowLeft /> &nbsp; Back
                </Button>
              </Col>
              <Col xs={4}>
                {isEditable ? (
                  <InputGroup controlId="formDescription">
                    <InputGroup.Text id="formDuration">
                      Description
                    </InputGroup.Text>
                    <Form.Control
                      as="input"
                      value={jobOffer?.description || ""}
                      disabled={!isEditable}
                      onChange={(e) =>
                        handleInputChange("description", e.target.value)
                      }
                    />
                  </InputGroup>
                ) : (
                  <div className="text-center">{jobOffer?.description}</div>
                )}
              </Col>
            </Row>
          </Card.Title>
        </Card.Header>
        <Card.Body>
          <JobProposalModalDetail
            show={jobProposalDetailModalShow}
            onHide={() => setJobProposalDetailModalShow(false)}
            jobOfferId={jobOffer?.id}
            professionalId={selectedCandidate.id}
            setCustomerJobOfferDirty={() => setDirty(true)}
          />
          <ConfirmationModal
            show={modalShow}
            action={modalAction}
            onHide={() => setModalShow(false)}
            jobOffer={jobOffer}
            setDirty={() => setDirty(true)}
          />
          <Form>
            <Row className="mb-3">
              <Col>Job ID {jobOffer?.id}</Col>
              <Col>
                Status <JobOfferBadge status={jobOffer?.offerStatus} />
              </Col>
              <Col>
                {isEditable ? (
                  <InputGroup controlId="formDuration">
                    <InputGroup.Text id="formDuration">
                      <Icon.Calendar className="mx-1" /> Duration
                    </InputGroup.Text>
                    <Form.Control
                      type="text"
                      value={jobOffer?.duration || ""}
                      disabled={!isEditable}
                      onChange={(e) =>
                        handleInputChange("duration", e.target.value)
                      }
                    />
                  </InputGroup>
                ) : (
                  <>Duration: {jobOffer?.duration} Months</>
                )}
              </Col>
              <Col>Value: {jobOffer?.value || "N/A"} €</Col>
            </Row>
            <hr />
            <Row>
              <Col>
                Customer: {jobOffer?.customer.contactInfo.name}{" "}
                {jobOffer?.customer.contactInfo.surname}{" "}
                <CiZoomIn
                  size={20}
                  color="black"
                  style={{
                    backgroundColor: "white",
                    borderRadius: "20%", // Makes the background fully rounded
                    // padding: "10px", // Adds space around the icon inside the circle
                    transition: "color 0.3s ease, background-color 0.3s ease",
                  }}
                  onClick={() =>
                    navigate(`/crm/customers/${jobOffer?.customer.id}`)
                  }
                  onMouseOver={(e) => {
                    e.currentTarget.style.backgroundColor = "#e9ecef";
                    e.currentTarget.style.cursor = "pointer"; // Optional: change cursor on hover
                  }}
                  onMouseOut={(e) => {
                    e.currentTarget.style.backgroundColor = "white";
                  }}
                />
              </Col>
              <Col>
                Professional :{" "}
                {jobOffer?.professional?.contactInfo.name || "N/A"}{" "}
                {jobOffer?.professional?.contactInfo.surname}{" "}
                {!jobOffer?.professional ? (
                  <></>
                ) : (
                  <CiZoomIn
                    size={20}
                    color="black"
                    style={{
                      backgroundColor: "white",
                      borderRadius: "20%", // Makes the background fully rounded
                      // padding: "10px", // Adds space around the icon inside the circle
                      transition: "color 0.3s ease, background-color 0.3s ease",
                    }}
                    onClick={() =>
                      navigate(
                        `/crm/professionals/${jobOffer?.professional.id}`,
                      )
                    }
                    onMouseOver={(e) => {
                      e.currentTarget.style.backgroundColor = "#e9ecef";
                      e.currentTarget.style.cursor = "pointer"; // Optional: change cursor on hover
                    }}
                    onMouseOut={(e) => {
                      e.currentTarget.style.backgroundColor = "white";
                    }}
                  />
                )}
              </Col>
            </Row>
            <hr />
            <Row>
              <Col>
                {isEditable ? (
                  <Form.Group controlId="requiredSkills" className="mb-3 mt-2">
                    {jobOffer?.requiredSkills.map((skill, index) => (
                      <Row key={index} className="mb-">
                        <Col>
                          <InputGroup className="mb-3">
                            <InputGroup.Text id="duration">
                              <Icon.Award className="mx-1" /> Skill
                            </InputGroup.Text>
                            <Form.Control
                              type="text"
                              placeholder="Enter a required skill"
                              value={skill}
                              disabled={!isEditable}
                              onChange={(e) =>
                                handleSkillChange(index, e.target.value)
                              }
                              required
                            />
                            {jobOffer?.requiredSkills.length > 1 &&
                            isEditable ? (
                              <MdDelete
                                size={25}
                                role="button"
                                onClick={() => handleRemoveSkill(index)}
                              />
                            ) : (
                              <></>
                            )}
                          </InputGroup>
                        </Col>
                      </Row>
                    ))}
                    {isEditable ? (
                      <InputGroup className="mb-3">
                        <InputGroup.Text id="duration">
                          <Icon.Award className="mx-1" /> Skill
                        </InputGroup.Text>
                        <Form.Control
                          type="text"
                          placeholder="Add new skill"
                          value={newSkill}
                          onChange={(e) => setNewSkill(e.target.value)}
                        />
                        <Button variant="primary" onClick={handleAddSkill}>
                          Add
                        </Button>
                      </InputGroup>
                    ) : (
                      <></>
                    )}
                  </Form.Group>
                ) : (
                  <>
                    Required Skills :{" "}
                    {jobOffer?.requiredSkills.join(", ").toString()}{" "}
                  </>
                )}
              </Col>

              <Col>
                {!isEditable ? (
                  <>Notes:{jobOffer?.notes || "N/A"}</>
                ) : (
                  <InputGroup className="mb-3">
                    <InputGroup.Text id="duration">
                      <Icon.Pencil className="mx-1" /> Notes
                    </InputGroup.Text>

                    <Form.Control
                      as="textarea"
                      rows={3}
                      value={jobOffer?.notes || ""}
                      disabled={!isEditable}
                      onChange={(e) =>
                        handleInputChange("notes", e.target.value)
                      }
                    />
                  </InputGroup>
                )}
              </Col>
            </Row>
            {jobOffer?.offerStatus === "CREATED" && (
              <>
                <Button variant="primary" onClick={handleEditClick}>
                  {isEditable ? "Cancel" : "Edit"}
                </Button>
                {isEditable && (
                  <Button
                    variant="success"
                    className="mx-2"
                    onClick={handleSubmit}
                  >
                    Submit
                  </Button>
                )}
              </>
            )}

            {jobOffer?.offerStatus === "CANDIDATE_PROPOSAL" && (
              <>
                <hr />
                <Container>
                  <h4>Proposed Professional</h4>
                  <Row>
                    <Col
                      md={12}
                      key={jobOffer.professional.id}
                      className="mb-4"
                    >
                      <Card>
                        <Card.Body>
                          <Card.Title>{`${jobOffer.professional.contactInfo.name} ${jobOffer.professional.contactInfo.surname}`}</Card.Title>
                          <Card.Subtitle className="mb-2 text-muted">
                            Location: {jobOffer.professional.location}
                          </Card.Subtitle>
                          <Card.Text>
                            Employment State:{" "}
                            {jobOffer.professional.employmentState}
                            <br />
                            Skills: {jobOffer.professional.skills.join(", ")}
                          </Card.Text>

                          <Button
                            variant="warning"
                            onClick={() => {
                              //setJobProposalDetailModalShow(true);

                              let selected: Candidate = {
                                id: jobOffer.professional.id,
                                name: jobOffer.professional.contactInfo.name,
                                surname:
                                  jobOffer.professional.contactInfo.surname,
                                cvDocument: jobOffer?.professional.cvDocument,
                              };
                              setSelectedCandidate(selected);
                              setJobProposalDetailModalShow(true);
                            }}
                            className="me-2"
                          >
                            Show Job Proposal
                          </Button>

                          <Button
                            variant="primary"
                            disabled={!jobOffer?.professional.cvDocument}
                            onClick={() =>
                              jobOffer?.professional.cvDocument != undefined
                                ? handleViewDocument(
                                    jobOffer?.professional.cvDocument,
                                  )
                                : null
                            }
                          >
                            Download CV
                          </Button>
                        </Card.Body>
                      </Card>
                    </Col>
                  </Row>
                </Container>
              </>
            )}
          </Form>

          {jobOffer?.offerStatus === "CONSOLIDATED" && (
            <>
              <Button
                variant="warning"
                onClick={() => {
                  //setJobProposalDetailModalShow(true);

                  let selected: Candidate = {
                    id: jobOffer.professional.id,
                    name: jobOffer.professional.contactInfo.name,
                    surname: jobOffer.professional.contactInfo.surname,
                    cvDocument: jobOffer?.professional.cvDocument,
                  };
                  setSelectedCandidate(selected);
                  setJobProposalDetailModalShow(true);
                }}
                className="me-2"
              >
                Show Job Proposal
              </Button>
              <Button
                style={{ marginRight: 10 }}
                variant="success"
                onClick={() => {
                  setModalAction("done");
                  setModalShow(true);
                }}
              >
                Make Job Offer Done
              </Button>

              <Button
                variant="danger"
                onClick={() => {
                  setModalAction("abort");
                  setModalShow(true);
                }}
              >
                Abort Job Offer
              </Button>
            </>
          )}
        </Card.Body>
      </Card>
    </>
  );
}
