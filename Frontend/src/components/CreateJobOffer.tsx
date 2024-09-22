import React, { useEffect, useState } from "react";
import {
  Alert,
  Button,
  Col,
  Form,
  InputGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { Customer } from "../types/customer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";

import * as Icon from "react-bootstrap-icons";
import { MdDelete } from "react-icons/md";
import { JobOffer } from "../types/JobOffer.ts";

export type CreateJobOfferProps = {
  addJobOffer: (o: JobOffer) => void;
  setDirty: (b: boolean) => void;
};

export default function CreateJobOffer(props: CreateJobOfferProps) {
  const [loadingForm, setLoadingForm] = useState(true);
  const [description, setDescription] = useState("");
  const [requiredSkills, setRequiredSkills] = useState<string[]>([""]);
  const [duration, setDuration] = useState<number | "">("");
  const [notes, setNotes] = useState("");
  const { me } = useAuth();
  const [formError, setFormError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

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

    setLoadingForm(true);
    API.getCustomerFromCurrentUser()
      .then((customer) => {
        setUserInfo(customer);
      })
      .catch((err) => setFormError(err.message))
      .finally(() => setLoadingForm(false));
  }, [me, userInfo.id]);

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

  const handleSkillChange = (index: number, value: string) => {
    const newSkills = [...requiredSkills];
    newSkills[index] = value;
    setRequiredSkills(newSkills);
  };

  const handleAddSkill = () => {
    setRequiredSkills([...requiredSkills, ""]);
  };

  const handleRemoveSkill = (index: number) => {
    const newSkills = requiredSkills.filter((_, i) => i !== index);
    setRequiredSkills(newSkills);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const jobOffer = {
      description,
      requiredSkills,
      duration: typeof duration === "number" ? duration : parseInt(duration),
      notes: notes || null,
    };

    API.addJobOffer(jobOffer, userInfo.id)
      .then(() => {
        setDescription("");
        setRequiredSkills([""]);
        setDuration("");
        setNotes("");
        props.setDirty(true);
        setSuccessMessage("Job Offer created successfully!");
        setTimeout(() => {
          setSuccessMessage("");
        }, 3000);
      })
      .catch((err) => {
        console.log(err);
        setFormError("Failed to create job offer");
      });
  };
  const handleCloseSuccessMessage = () => {
    setSuccessMessage("");
  };

  if (loadingForm) {
    return <Spinner />;
  }

  if (formError) {
    return (
      <Alert variant="danger">
        <strong>Error:</strong> {formError}
      </Alert>
    );
  }

  return (
    <div className="mx-auto">
      <Form onSubmit={handleSubmit}>
        <Row>
          <Col xs={9}>
            {" "}
            <Form.Group controlId="jobDescription" className="mb-1">
              <InputGroup className="mb-3">
                <InputGroup.Text id="jobDescription">
                  <Icon.PersonWorkspace className="mx-1" /> Job Description
                </InputGroup.Text>
                <Form.Control
                  type="text"
                  placeholder="Enter job description"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />
              </InputGroup>
            </Form.Group>
          </Col>
          <Col xs={3}>
            <Form.Group controlId="jobDuration" className="mb-3">
              <InputGroup className="mb-3">
                <InputGroup.Text id="duration">
                  <Icon.Calendar className="mx-1" /> Duration (Months)
                </InputGroup.Text>
                <Form.Control
                  type="number"
                  placeholder="0"
                  value={duration}
                  onChange={(e) =>
                    setDuration(
                      e.target.value === "" ? "" : parseInt(e.target.value),
                    )
                  }
                  required
                  min={1}
                />
              </InputGroup>
            </Form.Group>
          </Col>
        </Row>
        <Row>
          <Col>
            <Form.Group controlId="requiredSkills" className="mb-3 mt-2">
              {requiredSkills.map((skill, index) => (
                <Row key={index} className="mb-">
                  <Col>
                    <InputGroup className="mb-3">
                      {/* Box con icona e testo "Skill" */}
                      <InputGroup.Text id="duration">
                        <Icon.Award className="me-2" /> Skill
                      </InputGroup.Text>

                      {/* Input del form */}
                      <Form.Control
                        type="text"
                        placeholder="Enter a required skill"
                        value={skill}
                        onChange={(e) =>
                          handleSkillChange(index, e.target.value)
                        }
                        required
                      />

                      {/* Pulsante di cancellazione se ci sono più skill */}
                      {requiredSkills.length > 1 && (
                        <InputGroup.Text
                          role="button"
                          onClick={() => handleRemoveSkill(index)}
                        >
                          <MdDelete size={20} />
                        </InputGroup.Text>
                      )}
                    </InputGroup>
                  </Col>
                </Row>
              ))}

              <Button
                variant="outline-info"
                className="mt-2"
                onClick={handleAddSkill}
              >
                Add Skill
              </Button>
            </Form.Group>
          </Col>
          <Col>
            {" "}
            <Form.Group controlId="jobNotes" className="mb-3">
              <InputGroup className="mb-3">
                <InputGroup.Text id="duration">
                  <Icon.Pencil className="mx-1" /> Notes
                </InputGroup.Text>

                <Form.Control
                  as="textarea"
                  rows={3}
                  placeholder="Enter any additional notes"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </InputGroup>
            </Form.Group>
          </Col>
        </Row>

        <Button variant="primary" type="submit">
          Create
        </Button>
        {successMessage && (
          <Alert
            variant="success"
            dismissible // Abilita la chiusura manuale
            onClose={handleCloseSuccessMessage} // Chiamata quando l'utente chiude l'alert
          >
            {successMessage}
          </Alert>
        )}
      </Form>
    </div>
  );
}
