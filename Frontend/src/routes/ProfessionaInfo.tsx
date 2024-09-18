import * as API from "../../API.tsx";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { Customer } from "../types/customer.ts";
import { ReducedJobOffer } from "../types/JobOffer.ts";
import { useAuth } from "../contexts/auth.tsx";
import { Contact, ContactCategory } from "../types/contact.ts";
import {
  Alert,
  Button,
  Card,
  Col,
  Container,
  Form,
  InputGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import {
  isDwellingAddress,
  isEmailAddress,
  isPhoneAddress,
} from "../types/address.ts";
import EditableField from "../components/EditableField.tsx";
import { getProfessionalJobOffer, updateCustomerNotes } from "../../API.tsx";
import { Professional } from "../types/professional.ts";
import Sidebar from "../components/Sidebar.tsx";

export default function ProfessionaInfo() {
  const navigate = useNavigate();
  const { professionalId } = useParams();

  const [notesLoading, setNotesLoading] = useState(false);

  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [professional, setProfessional] = useState<Professional>({
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
    notes: "",
  });

  useEffect(() => {
    const professionalIdNumber = professionalId
      ? Number(professionalId)
      : undefined;
    setLoading(true);
    API.getProfessionalById(professionalIdNumber)
      .then((professional) => {
        setProfessional(professional);
        API.getProfessionalJobOffer(professional.id)
          .then((data) => {
            setJobOffers(data.content);
            /*
                    setPageable(data.pageable);
                    setTotalPages(data.totalPages);
                    */
          })
          .catch(() => {
            setError("Failed to fetch job offers");
          });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [professional.id]);

  function updateNotes(notes: string) {
    const professionalIdNumber = professionalId
      ? Number(professionalId)
      : undefined;
    setNotesLoading(true);

    API.updateProfessionalNotes(professionalIdNumber, notes)
      .then((professional) => setProfessional(professional))
      .catch((error) => setError(error))
      .finally(() => setNotesLoading(false));
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

  const notDoneOffers = jobOffers.filter(
    (offer) =>
      offer.offerStatus.toString() !== "DONE" &&
      offer.offerStatus.toString() !== "ABORT",
  );

  console.log(notDoneOffers);

  const doneOffers = jobOffers.filter(
    (offer) =>
      offer.offerStatus.toString() == "DONE" ||
      offer.offerStatus.toString() == "ABORT",
  );

  return (
    <>
      <Container fluid>
        <Row>
          <Col xs={2}>
            <Sidebar />
          </Col>
          <Col xs>
            <Row className="pb-3" style={{ borderBottom: "dotted grey 1px" }}>
              <h1>
                {professional?.contactInfo.name +
                  "\t" +
                  professional?.contactInfo.surname}
              </h1>
            </Row>
            <Row className="mt-3" style={{ justifyContent: "center" }}>
              <Col sm={6}>
                <h3>Contacts</h3>
              </Col>
              <Row className="pb-3" style={{ borderBottom: "dotted grey 1px" }}>
                {professional?.contactInfo?.addresses.map((address) => {
                  if (isEmailAddress(address)) {
                    return (
                      <Col sm={6} key={address.id}>
                        <b> Email </b>: <br /> {address.email}
                      </Col>
                    );
                  } else if (isPhoneAddress(address)) {
                    return (
                      <Col sm={6} key={address.id}>
                        <b>Telephone</b>: <br />
                        {address.phoneNumber}
                      </Col>
                    );
                  } else if (isDwellingAddress(address)) {
                    return (
                      <Col sm={6} key={address.id}>
                        <b>Address</b>: <br />
                        {address.street +
                          ", " +
                          address.city +
                          ", " +
                          address.district +
                          address.country}
                      </Col>
                    );
                  }
                })}
              </Row>
            </Row>

            <Row
              className="mt-3"
              style={{
                justifyContent: "center",
                borderBottom: "dotted grey 1px",
              }}
            >
              <Col sm={6}>
                <h3>Information</h3>
              </Col>
              <Row className="mt-3 pb-3">
                <Col sm={3}>
                  <b> Location </b>: <br /> {professional?.location}
                </Col>
                <Col sm={3}>
                  <b> SSN </b>: <br />{" "}
                  {professional?.contactInfo.ssn
                    ? professional?.contactInfo.ssn
                    : "N/A"}
                </Col>
                <Col sm={3}>
                  <b> Employment State </b>: <br />{" "}
                  {professional?.employmentState}
                </Col>
                <Col sm={3}>
                  <b> Daily Rate </b>: <br /> {professional?.dailyRate}
                </Col>
              </Row>
            </Row>

            <Row
              className="mt-3"
              style={{
                justifyContent: "center",
                borderBottom: "dotted grey 1px",
              }}
            >
              <h3>Skills</h3>
              <Form.Group controlId="formRequiredSkills" className="mb-3">
                {professional.skills.map((skill, index) => (
                  <InputGroup key={index} className="mb-2">
                    <Form.Control type="text" value={skill} disabled={true} />
                  </InputGroup>
                ))}
              </Form.Group>
            </Row>

            <Row
              className="mt-3 pb-3"
              style={{ borderBottom: "dotted grey 1px" }}
            >
              <h3>Notes</h3>
              <EditableField
                label=""
                name="Notes"
                initValue={professional.notes || ""}
                loading={notesLoading}
                validate={(value) => value.trim().length > 0}
                onEdit={(field, val) => updateNotes(val)}
              />
            </Row>

            <Row
              className="mt-3 pb-3"
              style={{ borderBottom: "dotted grey 1px" }}
            >
              <h3> Active Job Offer</h3>
              {notDoneOffers.length > 0 ? (
                <>
                  {notDoneOffers.map((offer) => (
                    <Row key={offer.id} xs={12} className="mb-4">
                      <Card>
                        <Card.Body>
                          <Card.Title>Job Offer ID: {offer.id}</Card.Title>
                          <Card.Text>
                            <strong>Description:</strong> {offer.description}{" "}
                            &nbsp;
                            <strong>Status:</strong> {offer.offerStatus}&nbsp;
                            <strong>Professional:</strong>{" "}
                            {offer.professional
                              ? offer.professional.contactInfo.name +
                                " " +
                                offer.professional.contactInfo.surname
                              : "N/A"}
                          </Card.Text>

                          <Button
                            variant="primary"
                            onClick={() =>
                              navigate(`/crm/RecruiterJobOffer/${offer.id}`, {
                                replace: true,
                              })
                            }
                          >
                            View
                          </Button>
                        </Card.Body>
                      </Card>
                    </Row>
                  ))}
                </>
              ) : (
                <p>No Active job offers available.</p>
              )}
            </Row>
            <Row className="mt-3 pb-3">
              <h3> Completed Job Offer</h3>
              {doneOffers.length > 0 ? (
                <>
                  {doneOffers.map((offer) => (
                    <Row key={offer.id} xs={12} className="mb-4">
                      <Card>
                        <Card.Body>
                          <Card.Title>Job Offer ID: {offer.id}</Card.Title>
                          <Card.Text>
                            <strong>Description:</strong> {offer.description}{" "}
                            &nbsp;
                            <strong>Status:</strong> {offer.offerStatus}&nbsp;
                            <strong>Professional:</strong>{" "}
                            {offer.professional
                              ? offer.professional.contactInfo.name +
                                " " +
                                offer.professional.contactInfo.surname
                              : "N/A"}
                          </Card.Text>

                          <Button
                            variant="primary"
                            onClick={() =>
                              navigate(`/crm/RecruiterJobOffer/${offer.id}`, {
                                replace: true,
                              })
                            }
                          >
                            View
                          </Button>
                        </Card.Body>
                      </Card>
                    </Row>
                  ))}
                </>
              ) : (
                <p>No completed job offers available.</p>
              )}
            </Row>
          </Col>
        </Row>
      </Container>
    </>
  );
}
