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
import ConfirmationModal from "./ConfirmationModal.tsx";

import * as API from "../../API.tsx";
import * as Icon from "react-bootstrap-icons";
import SelectCandidateModal from "./SelectCandidateModal.tsx";
import RemoveCandidateModal from "./RemoveCandidateModal.tsx";
import JobProposalModal from "./JobProposalModal.tsx";
import JobProposalModalDetail from "./JobProposalDetailModal.tsx";
import { FaCircleArrowLeft } from "react-icons/fa6";
import JobOfferBadge from "./Badges/JobOfferBadge.tsx";
import { CiZoomIn } from "react-icons/ci";
import { MdDelete } from "react-icons/md";
import { useEffect, useState } from "react";

type Candidate = {
  id: number;
  name: string;
  surname: string;
  cvDocument?: number;
};
export default function ViewJobOfferDetailsRecruiter() {
  const [isEditable, setIsEditable] = useState(false);
  const [editableOffer, setEditableOffer] = useState(true);
  const [jobOffer, setJobOffer] = useState<JobOffer | undefined>();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [newSkill, setNewSkill] = useState<string>("");
  const [modalShow, setModalShow] = useState<boolean>(false);
  const [modalAction, setModalAction] = useState<string>("");
  const [dirty, setDirty] = useState(false);
  const [jobProposalModalShow, setJobProposalModalShow] =
    useState<boolean>(false);
  const [jobProposalDetailModalShow, setJobProposalDetailModalShow] =
    useState<boolean>(false);
  const [candidateModalShow, setCandidateModalShow] = useState<boolean>(false);
  const [removeCandidateModalShow, setRemoveCandidateModalShow] =
    useState<boolean>(false);
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate>({
    id: 0,
    name: "",
    surname: "",
    cvDocument: undefined,
  });

  const { jobOfferId } = useParams();
  //const { me } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    API.getJobOfferDetails(jobOfferId)
      .then((data) => {
        setJobOffer(data);
        setDirty(false);
        if (data.offerStatus !== "CREATED") {
          setEditableOffer(false);
        }
      })
      .catch(() => {
        setError("Failed to fetch job offer details");
      })

      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [dirty, jobOfferId]);

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
      {modalShow ? (
        <ConfirmationModal
          show={modalShow}
          action={modalAction}
          onHide={() => setModalShow(false)}
          jobOffer={jobOffer}
          setDirty={() => setDirty(true)}
        />
      ) : (
        <></>
      )}
      {removeCandidateModalShow ? (
        <RemoveCandidateModal
          show={removeCandidateModalShow}
          onHide={() => setRemoveCandidateModalShow(false)}
          jobOffer={jobOffer}
          candidate={selectedCandidate}
          setDirty={() => setDirty(true)}
        />
      ) : (
        <></>
      )}
      {jobProposalModalShow ? (
        <JobProposalModal
          show={jobProposalModalShow}
          onHide={() => setJobProposalModalShow(false)}
          jobOffer={jobOffer}
          candidate={selectedCandidate}
          setDirty={() => setDirty(true)}
        />
      ) : (
        <></>
      )}

      {jobProposalDetailModalShow ? (
        <JobProposalModalDetail
          show={jobProposalDetailModalShow}
          onHide={() => setJobProposalDetailModalShow(false)}
          jobOfferId={jobOffer?.id}
          professionalId={selectedCandidate.id}
        />
      ) : (
        <></>
      )}
      {candidateModalShow ? (
        <SelectCandidateModal
          show={candidateModalShow}
          // action={candidateModalAction}
          jobOffer={jobOffer}
          onHide={() => setCandidateModalShow(false)}
          setDirty={() => setDirty(true)}
        />
      ) : (
        <></>
      )}

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
                  <InputGroup>
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
          <Form>
            <Row className="mb-3">
              <Col>Job ID {jobOffer?.id}</Col>
              <Col>
                Status <JobOfferBadge status={jobOffer?.offerStatus} />
              </Col>
              <Col>
                {isEditable ? (
                  <InputGroup>
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
            <hr />
            {jobOffer?.offerStatus === "CREATED" && (
              <>
                <Button
                  variant={isEditable ? "danger" : "primary"}
                  onClick={handleEditClick}
                  disabled={!editableOffer}
                  style={{ marginRight: 10 }}
                >
                  {isEditable ? "Cancel" : "Edit"}
                </Button>

                {!isEditable && (
                  <>
                    <Button
                      variant="success"
                      onClick={() => {
                        setModalShow(true);
                        setModalAction("accept");
                      }}
                      disabled={!editableOffer}
                      style={{ marginRight: 10 }}
                    >
                      Accept
                    </Button>

                    <Button
                      variant="danger"
                      onClick={() => {
                        setModalShow(true);
                        setModalAction("decline");
                      }}
                      disabled={!editableOffer}
                    >
                      Decline
                    </Button>
                  </>
                )}
                {isEditable && (
                  <Button variant="warning" onClick={handleSubmit}>
                    Submit
                  </Button>
                )}
              </>
            )}

            {jobOffer?.candidates?.length != undefined &&
              jobOffer?.candidates?.length > 0 &&
              jobOffer?.offerStatus === "SELECTION_PHASE" && (
                <>
                  <Container className="mt-5">
                    <h2>Candidates</h2>
                    <Row>
                      {jobOffer?.candidates.map((candidate) => (
                        <Col md={12} key={candidate.id} className="mb-4">
                          <Card>
                            <Card.Body>
                              <Card.Title>{`${candidate.contactInfo.name} ${candidate.contactInfo.surname}`}</Card.Title>
                              <Card.Subtitle className="mb-2 text-muted">
                                Location: {candidate.location}
                              </Card.Subtitle>
                              <Card.Text>
                                Employment State: {candidate.employmentState}
                                <br />
                                Skills: {candidate.skills.join(", ")}
                              </Card.Text>
                              <Button
                                variant="success"
                                //onClick={() => handleCandidateAction("eligible", candidate.id)}

                                onClick={() => {
                                  let selected: Candidate = {
                                    id: candidate.id,
                                    name: candidate.contactInfo.name,
                                    surname: candidate.contactInfo.surname,
                                    cvDocument: candidate.cvDocument,
                                  };
                                  setSelectedCandidate(selected);
                                  setJobProposalModalShow(true);
                                }}
                                className="me-2"
                              >
                                Eligible Candidate
                              </Button>
                              <Button
                                variant="danger"
                                onClick={() => {
                                  let selected: Candidate = {
                                    id: candidate.id,
                                    name: candidate.contactInfo.name,
                                    surname: candidate.contactInfo.surname,
                                    cvDocument: candidate.cvDocument,
                                  };
                                  setSelectedCandidate(selected);
                                  setRemoveCandidateModalShow(true);
                                }}
                                className="me-2"
                              >
                                Remove Candidate
                              </Button>
                              <Button
                                variant="primary"
                                href={`/crm/API/professionals/${candidate.id}/cv/data`}
                                target="_blank"
                                disabled={!candidate.cvDocument}
                              >
                                Download CV
                              </Button>
                            </Card.Body>
                          </Card>
                        </Col>
                      ))}
                    </Row>
                  </Container>
                </>
              )}

            {jobOffer?.offerStatus === "CANDIDATE_PROPOSAL" && (
              <>
                <Container className="mt-5">
                  <h2>Proposed Professional</h2>
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
                            href={`/crm/API/professionals/${jobOffer?.professional.id}/cv/data`}
                            target="_blank"
                            disabled={!jobOffer?.professional.cvDocument}
                          >
                            Download CV
                          </Button>
                        </Card.Body>
                      </Card>
                    </Col>
                  </Row>
                </Container>
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
            {jobOffer?.offerStatus === "CONSOLIDATED" && (
              <Container>
                <Row>
                  <Col>
                    <Button
                      variant="warning"
                      onClick={() => {
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
                  </Col>
                </Row>
              </Container>
            )}

            {jobOffer?.offerStatus === "SELECTION_PHASE" && (
              <>
                <Button
                  variant="warning"
                  onClick={() => setCandidateModalShow(true)}
                  style={{ marginRight: 10 }}
                >
                  Propose Professional
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
          </Form>
        </Card.Body>
      </Card>
    </>
  );
}
