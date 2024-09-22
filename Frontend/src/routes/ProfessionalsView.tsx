import { useEffect, useState } from "react";
import API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import {
  Card,
  Col,
  Container,
  Form,
  InputGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import * as Icon from "react-bootstrap-icons";
import TagsInput from "react-tagsinput";
import "react-tagsinput/react-tagsinput.css";
import "../styles/CandidateManagement.css";
import { CiCircleInfo, CiSearch } from "react-icons/ci";
import PaginationCustom from "../components/PaginationCustom.tsx";
import CardProfessional from "../components/Card/ProfessionalCard.tsx";
import EmploymentBadge from "../components/Badges/EmploymentBadge.tsx";
import { ProfessionalFilter } from "../types/professionalFilter.ts";
import { ReducedProfessional } from "../types/professional.ts";
import PageLayout from "../components/PageLayout.tsx";
import React from "react";

export default function ProfessionalsView() {
  const { me } = useAuth();
  const [professional, setProfessional] = useState<ReducedProfessional[]>([]);
  const [location, setLocation] = useState("");
  const [skills, setSkills] = useState<string[]>([]); // Array di skill

  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [loading, setLoading] = useState(true);
  let statuses = ["EMPLOYED", "UNEMPLOYED"];
  const [checkedItems, setCheckedItems] = useState(
    statuses.reduce((acc: { [key: string]: boolean }, status: string) => {
      acc[status] = true;
      return acc;
    }, {}),
  );

  const handleCheckboxChange = (status: string) => {
    setCheckedItems((prevState) => ({
      ...prevState,
      [status]: !prevState[status], // Toggle the checked state for the clicked checkbox
    }));
  };

  useEffect(() => {
    const token = me?.xsrfToken;
    API.getProfessionals(token)
      .then((prof) => {
        setProfessional(prof.content);
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const filteredCheckedItems = Object.entries(checkedItems)
      .filter(([_status, isChecked]) => isChecked)
      .map(([status]) => status);

    if (filteredCheckedItems.length == 0) {
      setProfessional([]);
      return;
    }
    const token = me?.xsrfToken;
    setLoading(true);
    //workaround
    let filterDTO: ProfessionalFilter;
    if (filteredCheckedItems.length == 2) {
      filterDTO = {
        location: location,
        skills: skills,
        employmentState: undefined,
      };
    } else {
      filterDTO = {
        location: location,
        skills: skills,
        employmentState: filteredCheckedItems.toString(),
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
      sort: undefined,
    };
    API.getProfessionals(token, filterDTO, paging)
      .then((prof) => {
        setTotalPage(prof.totalPages);
        setProfessional(prof.content);
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => setLoading(false));
  }, [location, skills, page, checkedItems]);

  return (
    <PageLayout>
      <Container>
        <Card>
          <Card.Title>Professionals Research Tool</Card.Title>
          <Card.Body>
            <CiCircleInfo size={30} color={"green"} /> In this section, you can
            explore professionals enrolled in our system, allowing you to search
            for any professionals that meets your specific needs
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
                  <React.Fragment key={`fragment-${index}`}>
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
                      <EmploymentBadge status={status} key={`badge-${index}`} />
                    </div>
                  </React.Fragment>
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
                      inputProps={{ placeholder: "Search skills" }}
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
            {loading ? (
              <Spinner />
            ) : professional?.length > 0 ? (
              professional?.map((e: ReducedProfessional) => (
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
    </PageLayout>
  );
}
