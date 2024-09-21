import { useEffect, useState } from "react";
import {
  Card,
  Col,
  Container,
  Form,
  InputGroup,
  Row,
  Spinner,
} from "react-bootstrap";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";

import { ReducedJobOffer } from "../types/JobOffer.ts";

import CardJobOffer from "./Card/CardJobOffer.tsx";
import { CiCircleInfo, CiSearch } from "react-icons/ci";
import * as Icon from "react-bootstrap-icons";
import JobOfferBadge from "./Badges/JobOfferBadge.tsx";
import { JobOfferFilter } from "../types/JobOfferFilter.ts";

export default function ViewRecruiterJobOffer() {
  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { me } = useAuth();
  const [professionalId, setProfessionalId] = useState("");
  const [customerId, setCustomerId] = useState("");
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [mappedProfessional, setMappedProfessional] = useState<OptionItem[]>(
    [],
  );
  let statuses = [
    "DONE",
    "CANDIDATE_PROPOSAL",
    "ABORTED",
    "CONSOLIDATED",
    "CREATED",
    "SELECTION_PHASE",
  ];
  const [checkedItems, setCheckedItems] = useState(
    statuses.reduce((acc: { [key: string]: boolean }, status: string) => {
      acc[status] = true;
      return acc;
    }, {}),
  );

  const handleCheckboxChange = (status: string) => {
    setCheckedItems((prevState) => ({
      ...prevState,
      [status]: !prevState[status], // Toggle the checked state for the clicked checkbox
    }));
  };
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
    API.getCustomers(undefined, undefined)
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
    console.log(checkedItems);

    const filteredCheckedItems = Object.entries(checkedItems)
      .filter(([_status, isChecked]) => isChecked)
      .map(([status]) => status);

    const filterDTO: JobOfferFilter = {
      professionalId: professionalId,
      status: filteredCheckedItems.toString(),
      customerId: customerId,
    };

    let paging = {
      pageNumber: page - 1,
      pageSize: 5,
      sort: "status,asc",
    };
    setLoading(true);
    API.getJobOffers(paging, filterDTO)
      .then((data) => {
        setJobOffers(data.content);

        setTotalPage(data.totalPages);
      })
      .catch((err) => {
        console.log(err);
        setError("Failed to fetch job offers");
      })
      .finally(() => setLoading(false));
  }, [professionalId, checkedItems, customerId, page]);

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
      <Card>
        <Card.Title>Job Offers Tool</Card.Title>
        <Card.Body>
          <CiCircleInfo size={30} color={"green"} /> In this section, you can
          efficiently manage your job offers by filtering and sorting them to
          quickly find the ones that best match your criteria. Utilize the
          filtering options to narrow down your search, and easily handle
          multiple offers to stay organized and make informed decisions.
        </Card.Body>
      </Card>
      <br />
      <Card>
        <Card.Title>
          <CiSearch size={30} />
          Find by ...
        </Card.Title>
        <Row className="mb-4  pt-5 px-4">
          <Col md={2} className="border">
            Status
            {statuses.map((status, index) => (
              <>
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    marginBottom: "10px",
                  }}
                >
                  <Form.Check
                    inline
                    name={`group-${index}`}
                    type={"checkbox"}
                    id={`inline-checkbox-${index}`}
                    checked={checkedItems[status]}
                    onChange={() => handleCheckboxChange(status)}
                  />
                  <JobOfferBadge status={status} />
                </div>
              </>
            ))}
          </Col>

          <Col md={8}>
            <InputGroup className="mb-3">
              <InputGroup.Text id="address">
                <Icon.Briefcase className="mx-1" /> Professional
              </InputGroup.Text>

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
              <InputGroup.Text id="address">
                <Icon.Buildings className="mx-1" /> Customer
              </InputGroup.Text>

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
          </Col>
        </Row>

        <Row className="mb-4 px-4">
          <Col md={6}></Col>

          <Col md={6}></Col>
        </Row>
      </Card>
      <br />
      <CardJobOffer
        page={page}
        setPage={setPage}
        totalPage={totalPage}
        offers={jobOffers}
        cardTitle={"Job Offers"}
        cardInfo={
          "Here all the job offers in the system, ready to be processed"
        }
      />
    </Container>
  );
}
