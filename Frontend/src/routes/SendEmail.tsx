import {
  Button,
  Card,
  Col,
  Container,
  Form,
  Row,
  Spinner,
} from "react-bootstrap";

import * as API from "../../API.tsx";
import { sendEmailStruct } from "../types/sendEmail.ts";
import { useAuth } from "../contexts/auth.tsx";
import { Address, EmailAddress, getAddressType } from "../types/address.ts";
import { BiMailSend } from "react-icons/bi";
import AddressBook from "../components/MailAddressBook/AddressBook.tsx";
import { EmailContacts } from "../types/emailContacts.ts";
import { Customer } from "../types/customer.ts";
import { ReducedProfessional } from "../types/professional.ts";
import { Contact } from "../types/contact.ts";
import { useEffect, useState } from "react";
import { CiCircleInfo } from "react-icons/ci";
import PageLayout from "../components/PageLayout.tsx";

export default function SendEmail() {
  const [EmailAddr, setEmailAddr] = useState<string[]>([""]);
  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  const [sending, setSending] = useState(false);

  //save email address of different category
  const [emailContactsCust, setEmailContactsCust] = useState<EmailContacts[]>(
    [],
  );
  const [emailContactsProf, setemailContactsProf] = useState<EmailContacts[]>(
    [],
  );
  const [emailContactsGen, setemailContactsGen] = useState<EmailContacts[]>([]);

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
        pageSize: 5,
        sort: undefined,
      };
      const { totalPages: totalPages, content: professionals } =
        await API.getProfessionals(token, undefined, page);
      setTotalPageProf(totalPages);
      setemailContactsProf([]);
      // Fetch professional details in parallel
      const professionalDetails = await Promise.all(
        professionals.map((p: ReducedProfessional) =>
          API.getProfessionalById(p.id),
        ),
      );

      // Transform the fetched data into emailContacts
      const contacts = professionalDetails.map((item) => {
        let contact: EmailContacts = {
          id: item.id,
          name: item.contactInfo.name,
          surname: item.contactInfo.surname,
          role: "PROFESSIONAL",
          address: item.contactInfo.addresses
            .filter((a: Address) => getAddressType(a) === "EMAIL")
            .map((a: EmailAddress) => a.email),
        };
        return contact;
      });

      setemailContactsProf((prevContacts: EmailContacts[]) => {
        // Avoid adding duplicates
        const existingIds = new Set(
          prevContacts.map((c: EmailContacts) => c.id),
        );
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
        pageSize: 5,
        sort: undefined,
      };
      // Fetch professionals in parallel
      const { totalPages: totalPages, content: Contact } =
        await API.getUnknowContacts(token, page);
      setTotalPageGen(totalPages);
      setemailContactsGen([]);
      const ContactDetails = await Promise.all(
        Contact.map((p: Contact) => API.getContactById(p.id)),
      );
      // Transform the fetched data into emailContacts
      const contacts = ContactDetails.map((item) => {
        let contact: EmailContacts = {
          id: item.id,
          name: item.name,
          surname: item.surname,
          role: "UKNOWN",
          address: item.addresses
            .filter((a: Address) => getAddressType(a) === "EMAIL")
            .map((a: EmailAddress) => a.email),
        };
        return contact;
      });

      setemailContactsGen((prevContacts: EmailContacts[]) => {
        // Avoid adding duplicates
        const existingIds = new Set(
          prevContacts.map((c: EmailContacts) => c.id),
        );
        const uniqueContacts = contacts.filter((c) => !existingIds.has(c.id));
        return [...prevContacts, ...uniqueContacts];
      });
    } catch (error) {
      console.error("Error fetching uknown:", error);
    }
  };
  const fetchCustomer = async () => {
    try {
      const token = me?.xsrfToken;
      if (!token) return;

      let page = {
        pageNumber: pageCust - 1,
        pageSize: 5,
        sort: undefined,
      };
      // Fetch professionals in parallel
      const { totalPages: totalPages, content: customers } =
        await API.getCustomers(undefined, page);
      setTotalPageCust(totalPages);
      setEmailContactsCust([]);

      const customerDetails = await Promise.all(
        customers.map((c: Customer) => API.getCustomerById(c.id)),
      );

      // Transform the fetched data into emailContacts
      const contacts = customerDetails.map((item) => {
        let contact: EmailContacts = {
          id: item.id,
          name: item.contactInfo.name,
          surname: item.contactInfo.surname,
          role: "CUSTOMER",
          address: item.contactInfo.addresses
            .filter((a: Address) => getAddressType(a) === "EMAIL")
            .map((a: EmailAddress) => a.email),
        };
        return contact;
      });

      // Update state
      setEmailContactsCust((prevContacts: EmailContacts[]) => {
        // Avoid adding duplicates
        const existingIds = new Set(
          prevContacts.map((c: EmailContacts) => c.id),
        );
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
  const handleAddEmailAddrWithAddress = (addr: string) => {
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
    <PageLayout>
      <Container>
        <Row>
          <Col>
            <Card>
              <Card.Title className="my-3">Email sender</Card.Title>
              <Card.Body>
                <Card.Text>
                  <CiCircleInfo size={30} color={"green"} />
                  In this section, an operator can send an email to another
                  user.
                </Card.Text>
              </Card.Body>
            </Card>
          </Col>
        </Row>
        <br />
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
            <Card>
              <Card.Header>
                <Card.Title> Email </Card.Title>
              </Card.Header>

              <Card.Body>
                <Form>
                  <Form.Group controlId="Subject(s)" className="my-2">
                    <Form.Label>Email Addresses</Form.Label>
                    {EmailAddr.map((email, index) => (
                      <Row key={index} className="my-2">
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

                  <Form.Group className="mb-8 my-2" controlId="subjectInput">
                    <Form.Label>Subject</Form.Label>
                    <Form.Control
                      type="text"
                      placeholder={"Type the subject of your mail"}
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                    />
                  </Form.Group>
                  <Form.Group className="mb-8 my-2" controlId="messageTextarea">
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
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </PageLayout>
  );
}
