import {
  Accordion,
  Button,
  Card,
  Col,
  Container,
  Form,
  ListGroup,
  Pagination,
  Row,
  Spinner,
} from "react-bootstrap";
import React, { useEffect, useState } from "react";
import * as API from "../../API.tsx";
import { sendEmailStruct } from "../types/sendEmail.ts";
import { useAuth } from "../contexts/auth.tsx";
import { EmailAddress, getAddressType } from "../types/address.ts";
import { BiMailSend } from "react-icons/bi";
import AddressBook from "../components/AddressBook.tsx";

interface EmailContacts {
  id: String;
  name: String;
  surname: String;
  role: String;
  address: Array<String>;
}

export default function SendEmail() {
  const [loading, setLoading] = useState(true);
  const [EmailAddr, setEmailAddr] = useState<string[]>([""]);
  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  const [sending, setSending] = useState(false);

  //save email address of different category
  const [emailContactsCust, setEmailContactsCust] = useState([]);
  const [emailContactsProf, setemailContactsProf] = useState([]);
  const [emailContactsGen, setemailContactsGen] = useState([]);

  //Internal address book paging
  const [pageCust, setPageCust] = useState(1);
  const [TotalPageCust, setTotalPageCust] = useState(1);
  const [pageProf, setPageProf] = useState(1);
  const [TotalPageProf, setTotalPageProf] = useState(1);
  const [pageGen, setPageGen] = useState(1);
  const [TotalPageGen, setTotalPageGen] = useState(1);

  const { me } = useAuth();

  const fetchProfessionals = async () => {
    try {
      const token = me?.xsrfToken;
      if (!token) return;

      // Fetch professionals in parallel
      let page = {
        pageNumber: pageProf - 1,
        pageSize: 1,
      };
      const { totalPages: totalPages, content: professionals } =
        await API.getProfessionals(token, undefined, page);
      setTotalPageProf(totalPages);
      setemailContactsProf([]);
      // Fetch professional details in parallel
      const professionalDetails = await Promise.all(
        professionals.map((p) => API.getProfessionalById(p.id)),
      );

      // Transform the fetched data into emailContacts
      const contacts = professionalDetails.map((item) => {
        return {
          id: item.id,
          name: item.contactInfo.name,
          surname: item.contactInfo.surname,
          role: "PROFESSIONAL",
          address: item.contactInfo.addresses
            .filter((a) => getAddressType(a) === "EMAIL")
            .map((a) => (a as EmailAddress).email),
        };
      });

      setemailContactsProf((prevContacts) => {
        // Avoid adding duplicates
        const existingIds = new Set(prevContacts.map((c) => c.id));
        const uniqueContacts = contacts.filter((c) => !existingIds.has(c.id));
        return [...prevContacts, ...uniqueContacts];
      });
    } catch (error) {
      console.error("Error fetching professionals:", error);
    }
  };
  const fetchUknown = async () => {
    try {
      const token = me?.xsrfToken;
      if (!token) return;
      let page = {
        pageNumber: pageGen - 1,
        pageSize: 1,
      };
      // Fetch professionals in parallel
      const { totalPages: totalPages, content: Contact } =
        await API.getUnknowContacts(token, page);
      setTotalPageGen(totalPages);
      setemailContactsGen([]);
      const ContactDetails = await Promise.all(
        Contact.map((p) => API.getContactById(p.id)),
      );
      // Transform the fetched data into emailContacts
      const contacts = ContactDetails.map((item) => {
        return {
          id: "U" + item.id,
          name: item.name,
          surname: item.surname,
          role: "UNKNOWN",
          address: item.addresses
            .filter((a) => getAddressType(a) === "EMAIL")
            .map((a) => (a as EmailAddress).email),
        };
      });

      setemailContactsGen((prevContacts) => {
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

      let page = {
        pageNumber: pageCust - 1,
        pageSize: 1,
      };
      // Fetch professionals in parallel
      const { totalPages: totalPages, content: customers } =
        await API.getCustomers(undefined, page);
      setTotalPageCust(totalPages);
      setEmailContactsCust([]);

      const customerDetails = await Promise.all(
        customers.map((c) => API.getCustomerById(c.id)),
      );

      // Transform the fetched data into emailContacts
      const contacts = customerDetails.map((item) => {
        return {
          id: item.id,
          name: item.contactInfo.name,
          surname: item.contactInfo.surname,
          role: "CUSTOMER",
          address: item.contactInfo.addresses
            .filter((a) => getAddressType(a) === "EMAIL")
            .map((a) => (a as EmailAddress).email),
        };
      });

      // Update state
      setEmailContactsCust((prevContacts) => {
        // Avoid adding duplicates
        const existingIds = new Set(prevContacts.map((c) => c.id));
        const uniqueContacts = contacts.filter((c) => !existingIds.has(c.id));
        return [...prevContacts, ...uniqueContacts];
      });
    } catch (error) {
      console.error("Error fetching professionals:", error);
    }
  };

  useEffect(() => {
    fetchCustomer();
  }, [me?.xsrfToken, pageCust]);
  useEffect(() => {
    fetchProfessionals();
  }, [me?.xsrfToken, pageProf]);
  useEffect(() => {
    fetchUknown();
  }, [me?.xsrfToken, pageGen]);

  const handleEmailAddrChange = (index: number, value: string) => {
    const newSubject = [...EmailAddr];
    newSubject[index] = value;
    setEmailAddr(newSubject);
  };
  const handleAddEmailAddr = () => {
    setEmailAddr([...EmailAddr, ""]);
  };
  const handleAddEmailAddrWithAddress = (addr) => {
    if (EmailAddr[0] == "" && EmailAddr.length == 1) {
      setEmailAddr([addr]);
    } else {
      setEmailAddr([...EmailAddr, addr]);
    }
  };
  const handleRemoveEmailAddr = (index: number) => {
    const newSubject = EmailAddr.filter((_, i) => i !== index);
    setEmailAddr(newSubject);
  };
  const handleSendEmail = async () => {
    // Simulate sending an email

    setSending(true);

    try {
      // Create an array of promises for sending emails

      const listOfEmails = Array.from(
        new Set(EmailAddr.filter((e) => e != "")),
      );
      if (listOfEmails.length == 0) {
        alert(`No Email Address inserted!`);
        setSending(false);
        return;
      }
      if (subject == "") {
        alert(`No Subject inserted!`);
        setSending(false);
        return;
      }
      if (message == "") {
        alert(`No Message inserted!`);
        setSending(false);
        return;
      }

      const emailPromises = listOfEmails.map((addr) => {
        const msg: sendEmailStruct = {
          to: addr,
          subject: subject,
          body: message,
        };
        return API.sendEmail(msg); // Return the promise
      });

      // Wait for all emails to be sent
      await Promise.all(emailPromises);

      setSending(false);
      alert(`All emails sent successfully!`);
      setMessage("");
      setSubject("");
      setEmailAddr([""]);
    } catch (error) {
      setSending(false);
      alert(`Failed to send one or more emails`);
    }
  };

  return (
    <Container fluid>
      <Row>
        <Col>
          <Card>
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
      <Row>
        {/* Column 1: List of Contacts */}
        <Col md={4}>
          <AddressBook
            header="Customer's Address Book"
            emails={emailContactsCust}
            setPage={setPageCust}
            page={pageCust}
            totalPage={TotalPageCust}
            handleAddEmailAddrWithAddress={handleAddEmailAddrWithAddress}
          ></AddressBook>
          <AddressBook
            header="Professionals's Address Book"
            emails={emailContactsProf}
            setPage={setPageProf}
            page={pageProf}
            totalPage={TotalPageProf}
            handleAddEmailAddrWithAddress={handleAddEmailAddrWithAddress}
          ></AddressBook>
          <AddressBook
            header="Generic User's Address Book"
            emails={emailContactsGen}
            setPage={setPageGen}
            page={pageGen}
            totalPage={TotalPageGen}
            handleAddEmailAddrWithAddress={handleAddEmailAddrWithAddress}
          ></AddressBook>
        </Col>
        <Col md={8}>
          <Form>
            <Form.Group controlId="Subject(s)">
              <Form.Label>Email Addresses</Form.Label>
              {EmailAddr.map((email, index) => (
                <Row key={index}>
                  <Col>
                    <Form.Control
                      type="text"
                      placeholder="Enter a new email address"
                      value={email}
                      onChange={(e) =>
                        handleEmailAddrChange(index, e.target.value)
                      }
                      required
                    />
                  </Col>
                  <Col xs="auto">
                    <Button
                      variant="danger"
                      onClick={() => handleRemoveEmailAddr(index)}
                    >
                      Remove
                    </Button>
                  </Col>
                </Row>
              ))}
              <Button
                variant="outline-info"
                className="mt-2"
                onClick={handleAddEmailAddr}
              >
                Add Email
              </Button>
            </Form.Group>

            <Form.Group className="mb-8" controlId="subjectInput">
              <Form.Label>Subject</Form.Label>
              <Form.Control
                type="text"
                placeholder={"Type the subject of your mail"}
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

            <Button variant="primary" onClick={handleSendEmail}>
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
                <>
                  <BiMailSend /> {"Send Email"}
                </>
              )}
            </Button>
          </Form>
        </Col>
      </Row>
    </Container>
  );
}
