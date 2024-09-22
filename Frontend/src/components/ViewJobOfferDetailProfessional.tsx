import { useEffect, useState } from "react";
import {
  Button,
  Card,
  Col,
  Container,
  Form,
  Row,
  Spinner,
} from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { JobOffer } from "../types/JobOffer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";

import JobProposalModalDetail from "./JobProposalDetailModal.tsx";
import { Professional } from "../types/professional.ts";
import { FaCircleArrowLeft } from "react-icons/fa6";
import JobOfferBadge from "./Badges/JobOfferBadge.tsx";
import { CiZoomIn } from "react-icons/ci";

type Candidate = {
  id: number;
  name: string;
  surname: string;
};
export default function ViewJobOfferDetailProfessional() {
  const [jobOffer, setJobOffer] = useState<JobOffer>();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [dirty, setDirty] = useState<boolean>(false);
  const [userInfo, setUserInfo] = useState<Professional>({
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
  const { jobOfferId } = useParams();
  const { me } = useAuth();
  const navigate = useNavigate();
  const [jobProposalDetailModalShow, setJobProposalDetailModalShow] =
    useState<boolean>(false);
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate>({
    id: 0,
    name: "",
    surname: "",
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
    API.getProfessionalFromCurrentUser()
      .then((professional) => {
        setUserInfo(professional);
        API.getJobOfferDetails(jobOfferId)
          .then((data) => {
            setJobOffer(data);
          })
          .catch(() => {
            setError("Failed to fetch job offer details");
          });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [me, dirty]);

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
        setProfessionalDirty={() => setDirty(true)}
        setProfessionalJobOfferDirty={() => setDirty(true)}
      />
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
                <div className="text-center">{jobOffer?.description}</div>
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
                <>Duration: {jobOffer?.duration} Months</>
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

            {["CANDIDATE_PROPOSAL", "CONSOLIDATED"].some(
              (state) => jobOffer?.offerStatus == state,
            ) && (
              <Container className="mt-5">
                <Button
                  variant="warning"
                  onClick={() => {
                    //setJobProposalDetailModalShow(true);
                    if (jobOffer != undefined) {
                      let selected: Candidate = {
                        id: jobOffer?.professional.id,
                        name: jobOffer?.professional.contactInfo.name,
                        surname: jobOffer?.professional.contactInfo.surname,
                      };
                      setSelectedCandidate(selected);
                      setJobProposalDetailModalShow(true);
                    }
                  }}
                  className="me-2"
                >
                  Show Job Proposal
                </Button>
                {/*
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
                      //onClick={() => handleCandidateAction("download", candidate.id)}
                    >
                      Download CV
                    </Button>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
        */}
              </Container>
            )}
          </Form>
        </Card.Body>
      </Card>
    </>
  );
}
