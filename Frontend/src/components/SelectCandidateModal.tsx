import Modal from "react-bootstrap/Modal";
import {
  Accordion,
  Button,
  Container,
  Form,
  InputGroup,
} from "react-bootstrap";
import * as Icon from "react-bootstrap-icons";
import { useEffect, useState } from "react";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import TagsInput from "react-tagsinput";
import { Professional } from "../types/professional.ts";
import { JobOffer } from "../types/JobOffer.ts";

export type CandidateAccordionProps = {
  prof: Professional;
  jobOffer: JobOffer;
  setDirty: (dirty: boolean) => void;
  onHide: () => void;
};

function ProfessionalAccordion(props: CandidateAccordionProps) {
  const [error, setError] = useState<string | null>(null);

  function handleCandidate() {
    API.addCandidate(props.jobOffer.id, props.prof.id)
      .then(() => {
        props.setDirty(true);
        props.onHide();
      })
      .catch((err) => setError(err));
  }

  if (error) {
    return (
      <Container className="text-center mt-5">
        <p>{error}</p>
      </Container>
    );
  }
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

          {props.jobOffer.refusedCandidates.some(
            (refusedCandidate) => refusedCandidate.id === props.prof.id,
          ) ? (
            <p style={{ color: "red" }}>
              The customer has already refused this candidate
            </p>
          ) : (
            <Button className="primary mt-3" onClick={() => handleCandidate()}>
              {" "}
              Propose Candidate{" "}
            </Button>
          )}
        </Accordion.Body>
      </Accordion.Item>
    </div>
  );
}

export default function SelectCandidateModal(props: any) {
  //const navigate = useNavigate();
  const [professional, setProfessional] = useState<Professional[] | null>(null);
  const { me } = useAuth();

  const [location, setLocation] = useState("");
  //const [skills, setSkills] = useState("");
  const [skills, setSkills] = useState<string[]>([]); // Array di skill

  useEffect(() => {
    const token = me?.xsrfToken;

    const filterDTO = {
      location: location,
      skills: skills,
      employmentState: "UNEMPLOYED",
    };

    API.getProfessionals(token, filterDTO)
      .then((prof) => {
        setProfessional(prof.content);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [location, skills]);

  return (
    <Modal
      {...props}
      size="lg"
      aria-labelledby="contained-modal-title-vcenter"
      centered
    >
      <Modal.Header closeButton>
        <Modal.Title id="contained-modal-title-vcenter">
          Select Candidate
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
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

          {professional != undefined && professional.length > 0 ? (
            <Accordion>
              {professional &&
                professional.map(
                  (e) => (
                    <ProfessionalAccordion
                      key={e.id}
                      prof={e}
                      jobOffer={props.jobOffer}
                      setDirty={props.setDirty}
                      onHide={props.onHide}
                    />
                  ),

                  /* candidates && filteredCandidates.map((e) =>
                                                   <ProfessionalAccordion key={e.id} prof={e}/>*/
                )}
            </Accordion>
          ) : (
            <div>There no candidates matching the filters</div>
          )}
        </Container>
      </Modal.Body>
      <Modal.Footer></Modal.Footer>
    </Modal>
  );
}
