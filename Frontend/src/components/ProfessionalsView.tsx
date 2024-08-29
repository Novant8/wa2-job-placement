import { /*React,*/ useEffect, useState } from "react";
import API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import {
  Accordion,
  Container,
  InputGroup,
  Form,
  Button,
} from "react-bootstrap";
import * as Icon from "react-bootstrap-icons";
import { Professional } from "../types/professional.ts";
import TagsInput from "react-tagsinput";
import "react-tagsinput/react-tagsinput.css";
import "../styles/CandidateManagement.css";
import { useNavigate } from "react-router-dom";

//TODO: Add pagination

export type ProfessionalAccordionProps = {
  prof: Professional;
};

function ProfessionalAccordion(props: ProfessionalAccordionProps) {
  const navigate = useNavigate();
  return (
    <div>
      <Accordion.Item eventKey={props.prof?.id?.toString()}>
        <Accordion.Header>
          {props.prof.contactInfo?.name} {props.prof.contactInfo?.surname}
        </Accordion.Header>
        <Accordion.Body>
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

          <Button
            className="primary mt-3"
            onClick={() => navigate(`professionals/${props.prof?.id}`)}
          >
            {" "}
            View Details{" "}
          </Button>
        </Accordion.Body>
      </Accordion.Item>
    </div>
  );
}
export default function ProfessionalsView() {
  const { me } = useAuth();

  const [professional, setProfessional] = useState({});

  const [location, setLocation] = useState("");
  //const [skills, setSkills] = useState("");
  const [skills, setSkills] = useState<string[]>([]); // Array di skill
  const [employmentState, setEmploymentState] = useState("");

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

    API.getProfessionals(token, filterDTO)
      .then((prof) => {
        setProfessional(prof);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [location, skills, employmentState]);

  //TODO: remove this useEffect
  useEffect(() => {
    console.log(professional);
    console.log(me);
    //console.log(candidates.content);
  }, [professional]);

  return (
    <>
      <h1>Professionals</h1>
      <Container>
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

        {professional?.content?.length > 0 ? (
          <Accordion>
            {
              /*candidates && candidates.content.map((e) =>
                            <ProfessionalAccordion key={e.id} prof={e}/>*/
              professional &&
                professional.content?.map(
                  (e) => <ProfessionalAccordion key={e.id} prof={e} />,

                  /* candidates && filteredCandidates.map((e) =>
                                 <ProfessionalAccordion key={e.id} prof={e}/>*/
                )
            }
          </Accordion>
        ) : (
          <div>There no candidates matching the filters</div>
        )}
      </Container>
    </>
  );
}
