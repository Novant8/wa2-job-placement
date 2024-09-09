import { /*React,*/ useEffect, useState } from "react";
import API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import {
  Accordion,
  Container,
  InputGroup,
  Form,
  Button,
  Col,
  Card,
  Row,
} from "react-bootstrap";
import * as Icon from "react-bootstrap-icons";
import { Professional } from "../types/professional.ts";
import TagsInput from "react-tagsinput";
import "react-tagsinput/react-tagsinput.css";
import "../styles/CandidateManagement.css";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar.tsx";
import { CiCircleInfo, CiSearch } from "react-icons/ci";
import PaginationCustom from "../components/PaginationCustom.tsx";

//TODO: Add pagination

export type ProfessionalAccordionProps = {
  prof: Professional;
};

function ProfessionalCard(props: ProfessionalAccordionProps) {
  const navigate = useNavigate();
  return (
    <>
      <Card>
        <Card.Body>
          <Row className={"align-items-center justify-content-center"}>
            <Col>
              <b>
                {props.prof.contactInfo?.name} {props.prof.contactInfo?.surname}
              </b>
            </Col>
            <Col>
              {props.prof.contactInfo.ssn ? (
                <div>SSN: {props.prof.contactInfo.ssn}</div>
              ) : (
                ""
              )}
              {/*TODO: mostrare gli addresses della contact info ???*/}
              <div>Location: {props.prof.location}</div>
              <div>
                Skills:{" "}
                {props.prof.skills.map((e, index) =>
                  index == props.prof.skills.length - 1 ? (
                    props.prof.skills.length > 2 ? (
                      <span key={index}>,{e}</span>
                    ) : (
                      <span key={index}>{e}</span>
                    )
                  ) : (
                    <span key={index}>{e}, </span>
                  ),
                )}
              </div>
              <div>Employment State:{props.prof.employmentState}</div>
              {props.prof.notes ? <div>Notes: {props.prof.notes}</div> : ""}
            </Col>
            <Col>
              <Button
                className="primary mt-3"
                onClick={() => navigate(`${props.prof?.id}`)}
              >
                View Details
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </>
  );
}

export default function ProfessionalsView() {
  const { me } = useAuth();
  const [professional, setProfessional] = useState({});
  const [location, setLocation] = useState("");
  const [skills, setSkills] = useState<string[]>([]); // Array di skill
  const [employmentState, setEmploymentState] = useState("");
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  useEffect(() => {
    const token = me?.xsrfToken;
    API.getProfessionals(token)
      .then((prof) => {
        setProfessional(prof);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  useEffect(() => {
    const token = me?.xsrfToken;

    const filterDTO = {
      location: location,
      skills: skills,
      employmentState: employmentState,
    };
    let paging = {
      pageNumber: page - 1,
      pageSize: 1,
    };
    API.getProfessionals(token, filterDTO, paging)
      .then((prof) => {
        setTotalPage(prof.totalPages);
        setProfessional(prof);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [location, skills, employmentState, page]);

  //TODO: remove this useEffect
  useEffect(() => {
    console.log(professional);
    console.log(me);
    //console.log(candidates.content);
  }, [professional]);

  return (
    <>
      <Container fluid>
        <Row>
          <Col xs={2}>
            <Sidebar />
          </Col>
          <Col xs>
            <Container>
              <Card>
                <Card.Title>Professionals Research Tool</Card.Title>
                <Card.Body>
                  <CiCircleInfo size={30} color={"green"} /> In this section,
                  you can explore professionals enrolled in our system, allowing
                  you to search for any professionals that meets your specific
                  needs
                </Card.Body>
              </Card>
              <br />
              <Card>
                <Card.Title>
                  <CiSearch size={30} />
                  Find by ...
                </Card.Title>
                <Card.Body>
                  <InputGroup className="mb-3">
                    <InputGroup.Text id="basic-addon1">
                      <Icon.Geo /> Location
                    </InputGroup.Text>
                    <Form.Control
                      placeholder="Search Location"
                      aria-label="Search Professional"
                      aria-describedby="basic-addon1"
                      value={location}
                      name="location"
                      onChange={(e) => setLocation(e.target.value)}
                    />
                  </InputGroup>

                  <InputGroup className="mb-3">
                    <InputGroup.Text id="basic-addon1">
                      <Icon.ListColumnsReverse /> Skills
                    </InputGroup.Text>

                    <div style={{ flex: 1 }}>
                      <TagsInput
                        value={skills}
                        onChange={(tags: any) => setSkills(tags)}
                        inputProps={{ placeholder: "Add a skill" }}
                        className="tags-input"
                      />
                    </div>
                  </InputGroup>

                  <InputGroup className="mb-3">
                    <Form.Select
                      aria-label="Search Employment State"
                      value={employmentState}
                      name="employment"
                      onChange={(e) => setEmploymentState(e.target.value)}
                    >
                      <option value="">Select Employment State</option>{" "}
                      {/* Opzione di default */}
                      <option value="UNEMPLOYED">UNEMPLOYED</option>
                      <option value="EMPLOYED">EMPLOYED</option>
                    </Form.Select>
                  </InputGroup>
                </Card.Body>
              </Card>
              <br />
              <Card>
                <Card.Title> Professional's List</Card.Title>
                <Card.Body>
                  {professional?.content?.length > 0 ? (
                    professional.content?.map((e) => (
                      <ProfessionalCard key={e.id} prof={e} />
                    ))
                  ) : (
                    <div>There no candidates matching the filters</div>
                  )}
                </Card.Body>
                <PaginationCustom
                  page={page}
                  setPage={setPage}
                  totalPage={totalPage}
                />
              </Card>
            </Container>
          </Col>
        </Row>
      </Container>
    </>
  );
}
