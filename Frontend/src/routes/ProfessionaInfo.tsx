import * as API from "../../API.tsx";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { ReducedJobOffer } from "../types/JobOffer.ts";
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
import {
  isDwellingAddress,
  isEmailAddress,
  isPhoneAddress,
} from "../types/address.ts";
import EditableField from "../components/EditableField.tsx";
import { Professional } from "../types/professional.ts";
import Sidebar from "../components/Sidebar.tsx";
import { FaCircleArrowLeft } from "react-icons/fa6";
import CardJobOffer from "../components/Card/CardJobOffer.tsx";
import { JobOfferFilter } from "../types/JobOfferFilter.ts";

export default function ProfessionaInfo() {
  const navigate = useNavigate();
  const { professionalId } = useParams();

  const [notesLoading, setNotesLoading] = useState(false);

  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
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

        let paging = {
          pageNumber: page - 1,
          pageSize: 5,
        };
        let filter: JobOfferFilter = {
          professionalId: professionalId,
          customerId: undefined,
          status: undefined,
        };
        API.getJobOffers(paging, filter)
          .then((data) => {
            setJobOffers([]);
            setJobOffers(data.content);
            setTotalPage(data.totalPages);
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

  return (
    <>
      <Container fluid>
        <Row>
          <Col xs={2}>
            <Sidebar />
          </Col>
          <Col xs={10}>
            <Card>
              <Card.Header>
                <Card.Title as="h2">
                  <Row className="justify-content-begin">
                    <Col xs={4}>
                      <Button
                        className="d-flex align-items-center text-sm-start"
                        onClick={() => navigate("/crm/professionals")}
                      >
                        <FaCircleArrowLeft /> &nbsp; Back
                      </Button>
                    </Col>
                    <Col xs={4}>
                      <div className="text-center">
                        {professional.contactInfo?.name +
                          " " +
                          professional.contactInfo?.surname}
                      </div>
                    </Col>
                  </Row>
                </Card.Title>
              </Card.Header>
              <Card.Body>
                <Row>
                  <Col>
                    <h3>Contacts</h3>
                  </Col>
                </Row>
                <Row
                  className="pb-3"
                  style={{ borderBottom: "dotted grey 1px" }}
                >
                  <Col sm={4}>
                    <b>Email</b>
                  </Col>
                  <Col sm={4}>
                    <b>Telephone</b>
                  </Col>
                  <Col sm={4}>
                    <b>Address </b>
                  </Col>

                  {professional.contactInfo?.addresses.map((address) => {
                    if (isEmailAddress(address)) {
                      return (
                        <Col sm={4} key={address.id}>
                          {address.email}
                          <br />
                        </Col>
                      );
                    } else if (isPhoneAddress(address)) {
                      return (
                        <Col sm={4} key={address.id}>
                          {address.phoneNumber}
                          <br />
                        </Col>
                      );
                    } else if (isDwellingAddress(address)) {
                      return (
                        <Col sm={4} key={address.id}>
                          {address.street +
                            ", " +
                            address.city +
                            ", " +
                            address.district +
                            address.country}
                          <br />
                        </Col>
                      );
                    }
                  })}
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
                        <Form.Control
                          type="text"
                          value={skill}
                          disabled={true}
                        />
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
                    onEdit={(_field, val) => updateNotes(val)}
                  />
                </Row>

                <Row className="m-2">
                  <CardJobOffer
                    page={page}
                    setPage={setPage}
                    totalPage={totalPage}
                    cardInfo={
                      "In this section you can consult all the job offers where the professional is involved"
                    }
                    cardTitle={"Job Offers"}
                    offers={jobOffers}
                  />
                </Row>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  );
}
