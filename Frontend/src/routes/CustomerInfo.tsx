import * as API from "../../API.tsx";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { Customer } from "../types/customer.ts";
import { ReducedJobOffer } from "../types/JobOffer.ts";
import { useAuth } from "../contexts/auth.tsx";
import { Contact, ContactCategory } from "../types/contact.ts";
import {
  Alert,
  Button,
  Card,
  Col,
  Container,
  Row,
  Spinner,
} from "react-bootstrap";
import {
  isDwellingAddress,
  isEmailAddress,
  isPhoneAddress,
} from "../types/address.ts";
import EditableField from "../components/EditableField.tsx";
import { updateCustomerNotes } from "../../API.tsx";
import Sidebar from "../components/Sidebar.tsx";
import EditAccountForm from "../components/EditAccountForm.tsx";

export default function CustomerInfo() {
  const navigate = useNavigate();
  const { customerId } = useParams();

  const [notesLoading, setNotesLoading] = useState(false);

  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
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
        API.getCustomerJobOffers(customer.id)
          .then((data) => {
            setJobOffers(data.content);
            /*
                    setPageable(data.pageable);
                    setTotalPages(data.totalPages);
                    */
          })
          .catch(() => {
            setError("Failed to fetch job offers");
          });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [customer.id]);

  function updateNotes(notes: string) {
    const customerIdNumber = customerId ? Number(customerId) : undefined;
    setNotesLoading(true);

    API.updateCustomerNotes(customerIdNumber, notes)
      .then((customer) => setCustomer(customer))
      .catch((error) => setError(error))
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

  const notDoneOffers = jobOffers.filter(
    (offer) =>
      offer.offerStatus.toString() !== "DONE" &&
      offer.offerStatus.toString() !== "ABORT",
  );
  const doneOffers = jobOffers.filter(
    (offer) =>
      offer.offerStatus.toString() == "DONE" ||
      offer.offerStatus.toString() == "ABORT",
  );

  return (
    <>
      <Row className="pb-3" style={{ borderBottom: "dotted grey 1px" }}>
        <h1>
          {customer.contactInfo?.name + "\t" + customer.contactInfo?.surname}
        </h1>
      </Row>
      <Row className="mt-3" style={{ justifyContent: "center" }}>
        <Col sm={6}>
          <h3>Contacts</h3>
        </Col>
      </Row>
      <Row className="pb-3" style={{ borderBottom: "dotted grey 1px" }}>
        {customer.contactInfo?.addresses.map((address) => {
          if (isEmailAddress(address)) {
            return (
              <Col sm={6} key={address.id}>
                <b> Email </b>: <br /> {address.email}
              </Col>
            );
          } else if (isPhoneAddress(address)) {
            return (
              <Col sm={6} key={address.id}>
                <b>Telephone</b>: <br />
                {address.phoneNumber}
              </Col>
            );
          } else if (isDwellingAddress(address)) {
            return (
              <Col sm={6} key={address.id}>
                <b>Address</b>: <br />
                {address.street +
                  ", " +
                  address.city +
                  ", " +
                  address.district +
                  address.country}
              </Col>
            );
          }
        })}
      </Row>
      <Row className="pb-3" style={{ borderBottom: "dotted grey 1px" }}>
        <EditableField
          label="Notes"
          name="Notes"
          initValue={customer.notes || ""}
          loading={notesLoading}
          validate={(value) => value.trim().length > 0}
          onEdit={(_field, val) => updateNotes(val)}
        />
      </Row>
      <Row className="mt-3 pb-3" style={{ borderBottom: "dotted grey 1px" }}>
        <h3> Active Job Offer</h3>
        {notDoneOffers.length > 0 ? (
          <>
            {notDoneOffers.map((offer) => (
              <Row key={offer.id} xs={12} className="mb-4">
                <Card>
                  <Card.Body>
                    <Card.Title>Job Offer ID: {offer.id}</Card.Title>
                    <Card.Text>
                      <strong>Description:</strong> {offer.description} &nbsp;
                      <strong>Status:</strong> {offer.offerStatus}&nbsp;
                      <strong>Professional:</strong>{" "}
                      {offer.professional
                        ? offer.professional.contactInfo.name +
                          " " +
                          offer.professional.contactInfo.surname
                        : "N/A"}
                    </Card.Text>

                    <Button
                      variant="primary"
                      onClick={() =>
                        navigate(`/crm/RecruiterJobOffer/${offer.id}`, {
                          replace: true,
                        })
                      }
                    >
                      View
                    </Button>
                  </Card.Body>
                </Card>
              </Row>
            ))}
          </>
        ) : (
          <p>No Active job offers available.</p>
        )}
      </Row>
      <Row className="mt-3 pb-3">
        <h3> Completed Job Offer</h3>
        {doneOffers.length > 0 ? (
          <>
            {doneOffers.map((offer) => (
              <Row key={offer.id} xs={12} className="mb-4">
                <Card>
                  <Card.Body>
                    <Card.Title>Job Offer ID: {offer.id}</Card.Title>
                    <Card.Text>
                      <strong>Description:</strong> {offer.description} &nbsp;
                      <strong>Status:</strong> {offer.offerStatus}&nbsp;
                      <strong>Professional:</strong>{" "}
                      {offer.professional
                        ? offer.professional.contactInfo.name +
                          " " +
                          offer.professional.contactInfo.surname
                        : "N/A"}
                    </Card.Text>

                    <Button
                      variant="primary"
                      onClick={() =>
                        navigate(`/crm/RecruiterJobOffer/${offer.id}`, {
                          replace: true,
                        })
                      }
                    >
                      View
                    </Button>
                  </Card.Body>
                </Card>
              </Row>
            ))}
          </>
        ) : (
          <p>No completed job offers available.</p>
        )}
      </Row>
    </>
  );
}
