import * as API from "../../API.tsx";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { Customer } from "../types/customer.ts";
import { ReducedJobOffer } from "../types/JobOffer.ts";
import { useAuth, userHasAnyRole } from "../contexts/auth.tsx";
import { Button, Card, Col, Container, Row, Spinner } from "react-bootstrap";
import {
  isDwellingAddress,
  isEmailAddress,
  isPhoneAddress,
} from "../types/address.ts";
import EditableField from "../components/EditableField.tsx";
import { FaCircleArrowLeft } from "react-icons/fa6";
import CardJobOffer from "../components/Card/CardJobOffer.tsx";
import { JobOfferFilter } from "../types/JobOfferFilter.ts";
import PageLayout from "../components/PageLayout.tsx";

export default function CustomerInfo() {
  const navigate = useNavigate();
  const { customerId } = useParams();
  const { me } = useAuth();
  const [notesLoading, setNotesLoading] = useState(false);

  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);

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
    notes: "",
  });

  useEffect(() => {
    const customerIdNumber = customerId ? Number(customerId) : undefined;
    setLoading(true);

    API.getCustomerById(customerIdNumber)
      .then((customer) => {
        setCustomer(customer);
        let paging = {
          pageNumber: page - 1,
          pageSize: 5,
          sort: undefined,
        };
        let filter: JobOfferFilter = {
          customerId: customerId,
          professionalId: undefined,
          status: undefined,
        };
        API.getJobOffers(paging, filter)
          .then((data) => {
            setJobOffers([]);

            setJobOffers(data.content);

            setTotalPage(data.totalPages);
          })
          .catch(() => {
            setError("Failed to fetch job offers");
          });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [page]);

  function updateNotes(notes: string) {
    const customerIdNumber = customerId ? Number(customerId) : undefined;
    setNotesLoading(true);

    API.updateCustomerNotes(customerIdNumber, notes)
      .then((customer) => setCustomer(customer))
      .catch(() => setError("Error occured when updating Customer Notes"))
      .finally(() => setNotesLoading(false));
  }

  if (loading) {
    return (
      <Container className="text-center mt-5">
        <Spinner animation="border" />
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="text-center mt-5">
        <p>{error}</p>
      </Container>
    );
  }

  return (
    <PageLayout>
      <Card>
        <Card.Header>
          <Card.Title as="h2">
            <Row className="justify-content-begin">
              <Col xs={4}>
                <Button
                  className="d-flex align-items-center text-sm-start"
                  onClick={() => navigate(-1)}
                >
                  <FaCircleArrowLeft /> &nbsp; Back
                </Button>
              </Col>
              <Col xs={4}>
                <div className="text-center">
                  {customer.contactInfo?.name +
                    " " +
                    customer.contactInfo?.surname}
                </div>
              </Col>
            </Row>
          </Card.Title>
        </Card.Header>
        <Card.Body>
          <Row>
            <Col>
              <h3>Contacts</h3>
            </Col>
          </Row>
          <Row
            className="pb-3 justify-content-around"
            style={{ borderBottom: "dotted grey 1px" }}
          >
            {customer.contactInfo?.addresses.map((address) => {
              if (isEmailAddress(address)) {
                return (
                  <Col sm={4} key={address.id}>
                    <b>Email</b>
                    <br />
                    {address.email}
                  </Col>
                );
              } else if (isPhoneAddress(address)) {
                return (
                  <Col sm={4} key={address.id}>
                    <b>Telephone</b>
                    <br />
                    {address.phoneNumber}
                    <br />
                  </Col>
                );
              } else if (isDwellingAddress(address)) {
                return (
                  <Col sm={4} key={address.id}>
                    <b>Address </b>
                    <br />
                    {address.street}, {address.city} ({address.district}),{" "}
                    {address.country}
                    <br />
                  </Col>
                );
              }
            })}
          </Row>
          {userHasAnyRole(me, ["operator", "manager"]) && (
            <Row
              className="mt-3 pb-3"
              style={{ borderBottom: "dotted grey 1px" }}
            >
              <h3>Notes</h3>
              <EditableField
                name="Notes"
                initValue={customer.notes || ""}
                loading={notesLoading}
                validate={(value) => value.trim().length > 0}
                onEdit={(_field, val) => updateNotes(val)}
              />
            </Row>
          )}

          <Row className="m-2">
            <CardJobOffer
              page={page}
              setPage={setPage}
              totalPage={totalPage}
              cardInfo={
                "In this section you can consult all the job offers created by the customers "
              }
              cardTitle={"Job Offers"}
              offers={jobOffers}
            />
          </Row>
        </Card.Body>
      </Card>
    </PageLayout>
  );
}
