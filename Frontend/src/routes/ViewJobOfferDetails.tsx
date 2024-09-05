import { useEffect, useState } from "react";
import {
  Form,
  Button,
  Col,
  Row,
  Container,
  Spinner,
  InputGroup,
  Card,
} from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { JobOffer, JobOfferCreate } from "../types/JobOffer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { Customer } from "../types/customer.ts";
import JobProposalModalDetail from "../components/JobProposalDetailModal.tsx";
import ConfirmationModal from "../components/ConfirmationModal.tsx";
import { set } from "js-cookie";
import { ApiError } from "../../API.tsx";
type Candidate = {
  id: number;
  name: string;
  surname: string;
  cvDocument?: number;
};
export default function ViewJobOfferDetails() {
  const [isEditable, setIsEditable] = useState(false);
  const [jobOffer, setJobOffer] = useState<JobOffer>();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [newSkill, setNewSkill] = useState<string>("");
  const [dirty, setDirty] = useState(false);
  const [modalShow, setModalShow] = useState<boolean>(false);
  const [modalAction, setModalAction] = useState<string>("");
  const [documentError, setDocumentError] = useState("");
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
  const navigate = useNavigate();
  const [jobProposalDetailModalShow, setJobProposalDetailModalShow] =
    useState<boolean>(false);
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate>({
    id: 0,
    name: "",
    surname: "",
    cvDocument: null,
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
    if (!me || userInfo.id > 0) return;

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
        navigate(`/crm/jobOffer/${jobOfferId}`);
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
    setDocumentError("");
    API.getDocumentHistory(documentId)
      .then((history) => {
        let document = history.versions[0];
        const url = `/document-store/API/documents/${document.historyId}/version/${document.versionId}/data`;
        window.open(url, "_blank");
      })
      .catch((err: ApiError) => setDocumentError(err.message));
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
          <Col>
            <Form.Group controlId="formJobId">
              <Form.Label>Job ID</Form.Label>
              <Form.Control type="text" value={jobOffer?.id} disabled />
            </Form.Group>
          </Col>
          <Col>
            <Form.Group controlId="formJobStatus">
              <Form.Label>Status</Form.Label>
              <Form.Control
                type="text"
                value={jobOffer?.offerStatus}
                disabled
              />
            </Form.Group>
          </Col>
        </Row>

        <Form.Group controlId="formDescription" className="mb-3">
          <Form.Label>Description</Form.Label>
          <Form.Control
            as="textarea"
            rows={3}
            value={jobOffer?.description || ""}
            disabled={!isEditable}
            onChange={(e) => handleInputChange("description", e.target.value)}
          />
        </Form.Group>

        <Form.Group controlId="formCustomerInfo" className="mb-3">
          <Form.Label>Customer Contact Info</Form.Label>
          <Form.Control
            type="text"
            value={`Name: ${jobOffer?.customer.contactInfo.name}`}
            disabled
          />
          <Form.Control
            type="text"
            value={`Surname: ${jobOffer?.customer.contactInfo.surname}`}
            disabled
          />
        </Form.Group>

        <Form.Group controlId="formProfessionalInfo" className="mb-3">
          <Form.Label>Professional Contact Info</Form.Label>
          <Form.Control
            type="text"
            value={`Name: ${jobOffer?.professional?.contactInfo.name || "N/A"}`}
            disabled
          />
          <Form.Control
            type="text"
            value={`Surname: ${jobOffer?.professional?.contactInfo.surname || "N/A"}`}
            disabled
          />
          <Form.Control
            type="text"
            value={`Location: ${jobOffer?.professional?.location || "N/A"}`}
            disabled
          />
          <Form.Control
            type="text"
            value={`Employment State: ${jobOffer?.professional?.employmentState || "N/A"}`}
            disabled
          />
        </Form.Group>

        <Form.Group controlId="formRequiredSkills" className="mb-3">
          <Form.Label>Required Skills</Form.Label>
          {jobOffer?.requiredSkills.map((skill, index) => (
            <InputGroup key={index} className="mb-2">
              <Form.Control
                type="text"
                value={skill}
                disabled={!isEditable}
                onChange={(e) => handleSkillChange(index, e.target.value)}
              />
              {isEditable && (
                <Button
                  variant="danger"
                  onClick={() => handleRemoveSkill(index)}
                >
                  Remove
                </Button>
              )}
            </InputGroup>
          ))}
          {isEditable && (
            <InputGroup className="mb-3">
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
          )}
        </Form.Group>

        <Row className="mb-3">
          <Col>
            <Form.Group controlId="formDuration">
              <Form.Label>Duration</Form.Label>
              <Form.Control
                type="text"
                value={jobOffer?.duration || ""}
                disabled={!isEditable}
                onChange={(e) => handleInputChange("duration", e.target.value)}
              />
            </Form.Group>
          </Col>
          <Col>
            <Form.Group controlId="formValue">
              <Form.Label>Value</Form.Label>
              <Form.Control
                type="text"
                value={jobOffer?.value || "N/A"}
                disabled
              />
            </Form.Group>
          </Col>
        </Row>

        <Form.Group controlId="formNotes" className="mb-3">
          <Form.Label>Notes</Form.Label>
          <Form.Control
            as="textarea"
            rows={3}
            value={jobOffer?.notes || ""}
            disabled={!isEditable}
            onChange={(e) => handleInputChange("notes", e.target.value)}
          />
        </Form.Group>

        {jobOffer?.offerStatus === "CREATED" && (
          <>
            <Button variant="primary" onClick={handleEditClick}>
              {isEditable ? "Cancel" : "Edit"}
            </Button>
            {isEditable && (
              <Button variant="warning" onClick={handleSubmit}>
                Submit
              </Button>
            )}
          </>
        )}

        {jobOffer?.offerStatus === "CANDIDATE_PROPOSAL" && (
          <Container className="mt-5">
            <h2>Proposed Professional</h2>
            <Row>
              <Col md={12} key={jobOffer.professional.id} className="mb-4">
                <Card>
                  <Card.Body>
                    <Card.Title>{`${jobOffer.professional.contactInfo.name} ${jobOffer.professional.contactInfo.surname}`}</Card.Title>
                    <Card.Subtitle className="mb-2 text-muted">
                      Location: {jobOffer.professional.location}
                    </Card.Subtitle>
                    <Card.Text>
                      Employment State: {jobOffer.professional.employmentState}
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
                      variant="primary"
                      disabled={!jobOffer?.professional.cvDocument}
                      onClick={() =>
                        handleViewDocument(jobOffer?.professional.cvDocument)
                      }
                    >
                      Download CV
                    </Button>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </Container>
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
    </>
  );
}
