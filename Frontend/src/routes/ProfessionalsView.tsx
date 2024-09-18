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
import { CiCircleInfo, CiSearch, CiZoomIn } from "react-icons/ci";
import PaginationCustom from "../components/PaginationCustom.tsx";
import CardProfessional from "../components/Card/ProfessionalCard.tsx";
import JobOfferBadge from "../components/Badges/JobOfferBadge.tsx";
import EmploymentBadge from "../components/Badges/EmploymentBadge.tsx";

export default function ProfessionalsView() {
  const { me } = useAuth();
  const [professional, setProfessional] = useState({});
  const [location, setLocation] = useState("");
  const [skills, setSkills] = useState<string[]>([]); // Array di skill

  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);

  let statuses = ["EMPLOYED", "UNEMPLOYED"];
  const [checkedItems, setCheckedItems] = useState(
    statuses.reduce((acc, status) => {
      acc[status] = true;
      return acc;
    }, {}),
  );

  const handleCheckboxChange = (status) => {
    setCheckedItems((prevState) => ({
      ...prevState,
      [status]: !prevState[status], // Toggle the checked state for the clicked checkbox
    }));
  };

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
    const filteredCheckedItems = Object.entries(checkedItems)
      .filter(([status, isChecked]) => isChecked)
      .map(([status]) => status);
    //workaround
    let filterDTO = {};
    if (filteredCheckedItems.length == 2) {
      filterDTO = {
        location: location,
        skills: skills,
      };
    } else {
      filterDTO = {
        location: location,
        skills: skills,
        employmentState: filteredCheckedItems,
      };
    }
    if (filteredCheckedItems.length == 0) {
      setTotalPage(0);
      setProfessional([]);
      return;
    }

    let paging = {
      pageNumber: page - 1,
      pageSize: 5,
    };
    API.getProfessionals(token, filterDTO, paging)
      .then((prof) => {
        setTotalPage(prof.totalPages);
        setProfessional(prof);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [location, skills, page, checkedItems]);

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
                <Card.Header>
                  <Card.Title>
                    <CiSearch size={30} />
                    Find by ...
                  </Card.Title>
                </Card.Header>
                <Card.Body>
                  <Row className={"align-items-center justify-content-center"}>
                    <Col md={2}>
                      Status
                      {statuses.map((status, index) => (
                        <>
                          <div
                            style={{
                              display: "flex",
                              alignItems: "center",
                              marginBottom: "10px",
                            }}
                          >
                            <Form.Check
                              inline
                              name={`group-${index}`}
                              type={"checkbox"}
                              id={`inline-checkbox-${index}`}
                              checked={checkedItems[status]}
                              onChange={() => handleCheckboxChange(status)}
                            />
                            <EmploymentBadge status={status} />
                          </div>
                        </>
                      ))}
                    </Col>
                    <Col>
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
                    </Col>

                    <Col>
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
                    </Col>
                  </Row>
                </Card.Body>
              </Card>
              <br />
              <Card>
                <Card.Header>
                  <Card.Title> Professionals's List</Card.Title>
                </Card.Header>

                <Card.Body>
                  {professional?.content?.length > 0 ? (
                    professional.content?.map((e) => (
                      <>
                        <CardProfessional key={e.id} prof={e} />
                        <br />
                      </>
                    ))
                  ) : (
                    <div>There no professionals matching the filters</div>
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
