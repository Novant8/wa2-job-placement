import {
  Button,
  Card,
  Col,
  Container,
  Form,
  ListGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import { useEffect, useState } from "react";
import { ContactCategory } from "../types/contact.ts";
import * as API from "../../API.tsx";
import { sendEmail } from "../../API.tsx";
import { sendEmailStruct } from "../types/sendEmail.ts";
import { useAuth } from "../contexts/auth.tsx";
import { EmailAddress, getAddressType } from "../types/address.ts";

interface EmailContacts {
  id: String;
  name: String;
  surname: String;
  role: String;
  address: Array<String>;
}

export default function SendEmail() {
  const [loading, setLoading] = useState(true);
  const [emailAddr, setEmailAddr] = useState("");
  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  const [sending, setSending] = useState(false);
  const [emailContacts, setEmailContacts] = useState([]);
  const { me } = useAuth();
  useEffect(() => {
    const fetchProfessionals = async () => {
      try {
        const token = me?.xsrfToken;
        if (!token) return;

        // Fetch professionals in parallel
        const { content: professionals } = await API.getProfessionals(token);

        // Fetch professional details in parallel
        const professionalDetails = await Promise.all(
          professionals.map((p) => API.getProfessionalById(p.id)),
        );

        // Transform the fetched data into emailContacts
        const contacts = professionalDetails.map((item) => {
          return {
            id: "P" + item.id,
            name: item.contactInfo.name,
            surname: item.contactInfo.surname,
            role: "PROFESSIONAL",
            address: item.contactInfo.addresses
              .filter((a) => getAddressType(a) === "EMAIL")
              .map((a) => (a as EmailAddress).email),
          };
        });

        // Update state
        setEmailContacts((prevContacts) => {
          // Avoid adding duplicates
          const existingIds = new Set(prevContacts.map((c) => c.id));
          const uniqueContacts = contacts.filter((c) => !existingIds.has(c.id));
          return [...prevContacts, ...uniqueContacts];
        });
      } catch (error) {
        console.error("Error fetching professionals:", error);
      }
    };
    const fetchCustomer = async () => {
      try {
        const token = me?.xsrfToken;
        if (!token) return;

        // Fetch professionals in parallel
        const { content: customers } = await API.getCustomers();

        // Fetch professional details in parallel
        const customerDetails = await Promise.all(
          customers.map((c) => API.getCustomerById(c.id)),
        );

        // Transform the fetched data into emailContacts
        const contacts = customerDetails.map((item) => {
          return {
            id: "C" + item.id,
            name: item.contactInfo.name,
            surname: item.contactInfo.surname,
            role: "CUSTOMER",
            address: item.contactInfo.addresses
              .filter((a) => getAddressType(a) === "EMAIL")
              .map((a) => (a as EmailAddress).email),
          };
        });

        // Update state
        setEmailContacts((prevContacts) => {
          // Avoid adding duplicates
          const existingIds = new Set(prevContacts.map((c) => c.id));
          const uniqueContacts = contacts.filter((c) => !existingIds.has(c.id));
          return [...prevContacts, ...uniqueContacts];
        });
      } catch (error) {
        console.error("Error fetching professionals:", error);
      }
    };
    fetchProfessionals();
    fetchCustomer();
  }, [me?.xsrfToken]);

  const handleSendEmail = () => {
    // Simulate sending an email
    setSending(true);
    const msg: sendEmailStruct = {
      to: emailAddr, // Assuming email is a variable holding the recipient's email address
      subject: subject, // You can replace this with a dynamic subject
      body: message, // Assuming message is a variable holding the email body
    };

    API.sendEmail(msg)
      .then(() => {
        setSending(false);
        alert(`Email sent to ${emailAddr}!`);
        setMessage("");
        setEmailAddr("");
        setSubject("");
      })
      .catch((error) => {
        setSending(false);
        alert(`Failed to send email: ${error.message}`);
      });
  };

  return (
    <Container fluid>
      <Row>
        {/* Column 1: List of Contacts */}
        <Col>
          {emailContacts.length > 0 ? (
            emailContacts.map((item) => (
              <Card key={item.id} className="mb-4">
                <Card.Body>
                  <Card.Title>
                    {item.name} {item.surname}
                  </Card.Title>
                  <ListGroup className="list-group-flush">
                    {item.address.map((a, index) => (
                      <ListGroup.Item
                        className="list-group-item-action"
                        key={index}
                      >
                        <Button onClick={() => setEmailAddr(a)}>+ </Button> {a}
                      </ListGroup.Item>
                    ))}
                  </ListGroup>
                </Card.Body>
              </Card>
            ))
          ) : (
            <p>No items to display</p>
          )}
        </Col>

        {/* Column 2: Form */}
        <Col>
          <Form>
            <Form.Group className="mb-8" controlId="emailInput">
              <Form.Label>Email address</Form.Label>
              <Form.Control
                type="email"
                placeholder="name@example.com"
                value={emailAddr}
                onChange={(e) => setEmailAddr(e.target.value)}
              />
            </Form.Group>
            <Form.Group className="mb-8" controlId="subjectInput">
              <Form.Label>Subject</Form.Label>
              <Form.Control
                type="text"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
              />
            </Form.Group>
            <Form.Group className="mb-8" controlId="messageTextarea">
              <Form.Label>Message</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                placeholder="Type your message here..."
                value={message}
                onChange={(e) => setMessage(e.target.value)}
              />
            </Form.Group>

            <Button
              variant="primary"
              onClick={handleSendEmail}
              disabled={sending || !emailAddr || !message}
            >
              {sending ? (
                <>
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                  />{" "}
                  Sending...
                </>
              ) : (
                "Send Email"
              )}
            </Button>
          </Form>
        </Col>
      </Row>

      <Row>
        <Col>
          <Card style={{ width: "18rem" }} className="mb-12">
            <Card.Body>
              <Card.Title>Send an email</Card.Title>
              <Card.Text>
                In this section, a recruiter can send an email to a customer or
                professional.
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}
