import { useEffect, useState } from "react";

import * as API from "../../API.tsx";
//import {useAuth} from "../contexts/auth.tsx";
import {
  Accordion,
  Container,
  InputGroup,
  Form,
  Row,
  Col,
  Alert,
  Button,
} from "react-bootstrap";
import * as Icon from "react-bootstrap-icons";
import { Customer } from "../types/customer.ts";
import { isEmailAddress, isPhoneAddress } from "../types/address.ts";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar.tsx";

export type CustomerAccordionProps = {
  cust: Customer;
};

function CustomerAccordion(props: CustomerAccordionProps) {
  const navigate = useNavigate();
  const [formError, setFormError] = useState("");
  const [customer, setCustomer] = useState<Customer>({
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
    API.getCustomerById(props.cust.id)
      .then((customer) => setCustomer(customer))
      .catch((err) => setFormError(err.message));
  }, []);

  if (formError) {
    return (
      <Alert variant="danger">
        <strong>Error:</strong> {formError}
      </Alert>
    );
  }
  return (
    <div>
      <Accordion.Item eventKey={props.cust?.id?.toString()}>
        <Accordion.Header>
          {customer.contactInfo?.name} {customer.contactInfo?.surname}
        </Accordion.Header>
        <Accordion.Body>
          {props.cust.contactInfo.ssn ? (
            <div>SSN: {customer.contactInfo.ssn}</div>
          ) : (
            ""
          )}

          <div>Notes: {customer.notes ? props.cust.notes : "No notes"}</div>

          {customer.contactInfo?.addresses.map((address) => {
            if (isEmailAddress(address)) {
              return <div key={address.id}>Email: {address.email}</div>;
            } else if (isPhoneAddress(address)) {
              return (
                <div key={address.id}>Telephone: {address.phoneNumber}</div>
              );
            }
          })}

          <Button
            className="primary mt-3"
            onClick={() => navigate(`${customer.id}`)}
          >
            {" "}
            View Details{" "}
          </Button>
        </Accordion.Body>
      </Accordion.Item>
    </div>
  );
}

export default function CustomersView() {
  //const {me} = useAuth()

  const [customers, setCustomers] = useState({});
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [telephone, setTelephone] = useState("");
  const [address, setAddress] = useState("");

  useEffect(() => {
    const filter = {
      fullName: fullName,
      email: email,
      telephone: telephone,
      address: address,
    };
    API.getCustomers(filter)
      .then((customer) => {
        setCustomers(customer);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [fullName, address, email, telephone]);

  //TODO: remove this useEffect
  useEffect(() => {
    console.log(customers);
    //console.log(candidates.content);
  }, [customers]);

  return (
    <>
      <Container fluid>
        <Row>
          <Col xs={3}>
            <Sidebar />
          </Col>
          <Col xs>
            <h1 className="mb-5">Customer Search</h1>
            <Container>
              {/* Search Filters */}
              <Row className="mb-4">
                <Col md={6}>
                  <InputGroup className="mb-3">
                    <InputGroup.Text id="full-name">
                      <Icon.PersonVcardFill className="mx-1" /> Full Name
                    </InputGroup.Text>
                    <Form.Control
                      placeholder="Search Full Name"
                      aria-label="Search Customer Full Name"
                      aria-describedby="full-name"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                    />
                  </InputGroup>
                </Col>

                <Col md={6}>
                  <InputGroup className="mb-3">
                    <InputGroup.Text id="address">
                      <Icon.House className="mx-1" /> Address
                    </InputGroup.Text>
                    <Form.Control
                      placeholder="Search Address"
                      aria-label="Search Customer Address"
                      aria-describedby="address"
                      value={address}
                      onChange={(e) => setAddress(e.target.value)}
                    />
                  </InputGroup>
                </Col>
              </Row>

              <Row className="mb-4">
                <Col md={6}>
                  <InputGroup className="mb-3">
                    <InputGroup.Text id="email">
                      <Icon.Envelope className="mx-1" /> Email
                    </InputGroup.Text>
                    <Form.Control
                      placeholder="Search Email"
                      aria-label="Search Customer Email"
                      aria-describedby="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                    />
                  </InputGroup>
                </Col>

                <Col md={6}>
                  <InputGroup className="mb-3">
                    <InputGroup.Text id="telephone">
                      <Icon.Telephone className="mx-1" /> Telephone
                    </InputGroup.Text>
                    <Form.Control
                      placeholder="Search Telephone"
                      aria-label="Search Customer Telephone"
                      aria-describedby="telephone"
                      value={telephone}
                      onChange={(e) => setTelephone(e.target.value)}
                    />
                  </InputGroup>
                </Col>
              </Row>

              {/* Customer List */}
              {customers?.content?.length > 0 ? (
                <Accordion>
                  {customers.content.map((customer) => (
                    <CustomerAccordion key={customer.id} cust={customer} />
                  ))}
                </Accordion>
              ) : (
                <div className="text-center">
                  No customers matching the filters
                </div>
              )}
            </Container>
          </Col>
        </Row>
      </Container>
    </>
  );
}
