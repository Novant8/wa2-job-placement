import { useEffect, useState } from "react";
import {
  Button,
  Card,
  Container,
  Form,
  InputGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { Pageable } from "../types/Pageable.ts";
import { ReducedJobOffer } from "../types/JobOffer.ts";
import { useNavigate } from "react-router-dom";
//import * as Icon from "react-bootstrap-icons";

export default function ViewRecruiterJobOffer() {
  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
  const [pageable, setPageable] = useState<Pageable | null>(null);
  const [totalPages, setTotalPages] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { me } = useAuth();
  const [professionalId, setProfessionalId] = useState("");
  const [status, setStatus] = useState("");
  const [customerId, setCustomerId] = useState("");
  const navigate = useNavigate();

  const [mappedProfessional, setMappedProfessional] = useState<OptionItem[]>(
    [],
  );
  const [mappedCustomer, setMappedCustomer] = useState<OptionItem[]>([]);

  interface OptionItem {
    id: number;
    fullName: string;
  }

  interface ContactInfo {
    id: number;
    name: string;
    surname: string;
    category: string;
  }

  interface ContentItem {
    id: number;
    contactInfo: ContactInfo;
    location: string;
    skills: string[];
    employmentState: string;
  }
  interface ContentItemCustomer {
    id: number;
    contactInfo: ContactInfo;
    notes: string | null;
  }

  useEffect(() => {
    const token = me?.xsrfToken;
    API.getProfessionals(token)
      .then((prof) => {
        const mappedOptions: OptionItem[] = prof.content.map(
          (item: ContentItem) => ({
            id: item.id,
            fullName: `${item.contactInfo.name} ${item.contactInfo.surname}`,
          }),
        );
        setMappedProfessional(mappedOptions);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  useEffect(() => {
    const token = me?.xsrfToken;
    API.getCustomers(token)
      .then((customer) => {
        const mappedOptions: OptionItem[] = customer.content.map(
          (item: ContentItemCustomer) => ({
            id: item.id,
            fullName: `${item.contactInfo.name} ${item.contactInfo.surname}`,
          }),
        );
        setMappedCustomer(mappedOptions);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  useEffect(() => {
    const token = me?.xsrfToken;

    const filterDTO = {
      professionalId: professionalId,
      status: status,
      customerId: customerId,
    };
    console.log(filterDTO);

    setLoading(true);
    API.getJobOfferRecruiter(token, filterDTO)
      .then((data) => {
        console.log(data.content);
        setJobOffers(data.content);
        setPageable(data.pageable);
        setTotalPages(data.totalPages);
      })
      .catch((err) => {
        console.log(err);
        setError("Failed to fetch job offers");
      })
      .finally(() => setLoading(false));
  }, [professionalId, status, customerId]);

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
    <Container className="mt-5">
      <h1>Job Offers</h1>
      <InputGroup className="mb-3">
        <Form.Select
          aria-label="Search Offer Status"
          value={status}
          name="status"
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value="">Select Offer Status</option>
          {/* Opzione di default */}
          <option value="CREATED">CREATED</option>
          <option value="SELECTION_PHASE">SELECTION PHASE</option>
          <option value="CANDIDATE_PROPOSAL">CANDIDATE PROPOSAL</option>
          <option value="CONSOLIDATED">CONSOLIDATED</option>
          <option value="DONE">DONE</option>
          <option value="ABORTED">ABORTED</option>
        </Form.Select>
      </InputGroup>

      <InputGroup className="mb-3">
        {/*<Icon.Briefcase/>*/}
        <Form.Select
          value={professionalId}
          onChange={(e) => setProfessionalId(e.target.value)}
        >
          <option value="">Select Professional</option>
          {/* Opzione di default */}
          {mappedProfessional.map((prof) => (
            <option key={prof.id} value={prof.id}>
              {prof.fullName}
            </option>
          ))}
        </Form.Select>
      </InputGroup>

      <InputGroup style={{ marginBottom: 20 }}>
        {/*<Icon.Buildings/>*/}
        <Form.Select
          value={customerId}
          onChange={(e) => setCustomerId(e.target.value)}
        >
          <option value=""> Select Customer</option>
          {/* Opzione di default */}
          {mappedCustomer.map((cust) => (
            <option key={cust.id} value={cust.id}>
              {cust.fullName}
            </option>
          ))}
        </Form.Select>
      </InputGroup>

      {jobOffers.map((offer) => (
        <Row key={offer.id} xs={12} className="mb-4">
          <Card>
            <Card.Body>
              <Card.Title>Job Offer ID: {offer.id}</Card.Title>
              <Card.Text>
                <strong>Description:</strong> {offer.description} &nbsp;
                <strong>Status:</strong> {offer.offerStatus}&nbsp;
                <strong>Professional:</strong>{" "}
                {offer.professional ? offer.professional : "N/A"}
              </Card.Text>

              <Button
                variant="primary"
                onClick={() => navigate(`RecruiterJobOffer/${offer.id}`)}
              >
                View
              </Button>
            </Card.Body>
          </Card>
        </Row>
      ))}

      {pageable && (
        <div className="mt-4">
          <p>
            Page {pageable.pageNumber + 1} of {totalPages}
          </p>
        </div>
      )}
    </Container>
  );
}
