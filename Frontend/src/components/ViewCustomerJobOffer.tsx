import { useEffect, useState } from "react";
import { Button, Card, Col, Container, Row, Spinner } from "react-bootstrap";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { Customer } from "../types/customer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";
import { Pageable } from "../types/Pageable.ts";
import { ReducedJobOffer } from "../types/JobOffer.ts";
import { useNavigate } from "react-router-dom";
import CreateJobOffer from "./CreateJobOffer.tsx";
import { CiCircleInfo, CiZoomIn } from "react-icons/ci";
import PaginationCustom from "./PaginationCustom.tsx";
import JobOfferBadge from "./JobOfferBadge.tsx";
import ListJobOffer from "./CardJobOffer.tsx";
import CardJobOffer from "./CardJobOffer.tsx";

export default function ViewCustomerJobOffer() {
  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);

  const { me } = useAuth();
  const navigate = useNavigate();

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

  useEffect(() => {
    if (!me) return;

    const registeredRole = me.roles.find((role) =>
      ["customer", "professional"].includes(role),
    );
    if (registeredRole)
      updateInfoField(
        "category",
        registeredRole.toUpperCase() as ContactCategory,
      );

    let paging = {
      pageNumber: page - 1,
      pageSize: 5,
    };

    setLoading(true);
    API.getCustomerFromCurrentUser()
      .then((customer) => {
        setUserInfo(customer);
        let filter = {
          customerId: customer.id,
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
  }, [me, userInfo.id, page]);

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
  function addJobOffer(j) {
    setJobOffers([...jobOffers, j]);
  }
  return (
    <Container>
      <CardJobOffer
        offers={jobOffers}
        page={page}
        setPage={setPage}
        totalPage={totalPage}
        cardInfo={
          "In this section, you can view your job offers and track their progress."
        }
        cardTitle={"Job Offers List"}
      />
    </Container>
  );
}
