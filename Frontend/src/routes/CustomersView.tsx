import { useEffect, useState } from "react";

import * as API from "../../API.tsx";

import {
  Card,
  Col,
  Container,
  Form,
  InputGroup,
  Pagination,
  Row,
} from "react-bootstrap";
import * as Icon from "react-bootstrap-icons";
import { Customer } from "../types/customer.ts";

import Sidebar from "../components/Sidebar.tsx";
import { CiCircleInfo, CiSearch } from "react-icons/ci";
import CardCustomer from "../components/Card/CardCustomer.tsx";

export type CustomerAccordionProps = {
  cust: Customer;
};

export default function CustomersView() {
  //const {me} = useAuth()

  const [customers, setCustomers] = useState<Customer[]>();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [telephone, setTelephone] = useState("");
  const [address, setAddress] = useState("");
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  useEffect(() => {
    const filter = {
      fullName: fullName,
      email: email,
      telephone: telephone,
      address: address,
    };
    let paging = {
      pageNumber: page - 1,
      pageSize: 5,
    };
    API.getCustomers(filter, paging)
      .then((customer) => {
        setCustomers([]);
        setTotalPage(customer.totalPages);
        setCustomers(customer.content);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [fullName, address, email, telephone, page]);

  //TODO: remove this useEffect
  useEffect(() => {
    console.log(customers);
    //console.log(candidates.content);
  }, [customers]);

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
                <Card.Title>Customers Research Tool</Card.Title>
                <Card.Body>
                  <CiCircleInfo size={30} color={"green"} /> In this section,
                  you can explore customers enrolled in our system, allowing you
                  to search for any customer that meets your specific needs
                </Card.Body>
              </Card>
              <br />
              {/* Search Filters */}
              <Card>
                <Card.Header>
                  <Card.Title>
                    <CiSearch size={30} />
                    Find by ...
                  </Card.Title>
                </Card.Header>
                <Row className="mb-4  pt-5 px-4">
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

                <Row className="mb-4 px-4">
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
              </Card>
              {/* Customer List */}
              <br />
              <Card>
                <Card.Header>
                  <Card.Title> Customers's List</Card.Title>
                </Card.Header>
                <Card.Body>
                  {customers && customers.length > 0 ? (
                    customers.map((c) => (
                      <>
                        <CardCustomer key={c.id} cust={c} />
                        <br />
                      </>
                    ))
                  ) : (
                    <div className="text-center">
                      No customers matching the filters
                    </div>
                  )}

                  <Pagination
                    className={
                      " d-flex justify-content-center align-items-center"
                    }
                  >
                    <Pagination.First onClick={() => setPage(1)} />
                    <Pagination.Prev
                      onClick={() => {
                        if (page - 1 >= 1) {
                          setPage(page - 1);
                        }
                      }}
                    />
                    <Pagination.Item>{`Page ${page} of ${totalPage}`}</Pagination.Item>
                    <Pagination.Next
                      onClick={() => {
                        if (page + 1 <= totalPage) {
                          setPage(page + 1);
                        }
                      }}
                    />
                    <Pagination.Last onClick={() => setPage(totalPage)} />
                  </Pagination>
                </Card.Body>
              </Card>
            </Container>
          </Col>
        </Row>
      </Container>
    </>
  );
}
