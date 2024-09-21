import { useEffect, useState } from "react";
import { Container, Spinner } from "react-bootstrap";
import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { Customer } from "../types/customer.ts";
import { Contact, ContactCategory } from "../types/contact.ts";

import { ReducedJobOffer } from "../types/JobOffer.ts";

import CardJobOffer from "./Card/CardJobOffer.tsx";
import { JobOfferFilter } from "../types/JobOfferFilter.ts";

export default function ViewProfessionalJobOffer() {
  const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { me } = useAuth();

  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);

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

    setLoading(true);
    API.getProfessionalFromCurrentUser()
      .then((prof) => {
        setUserInfo(prof);
        let paging = {
          pageSize: 5,
          pageNumber: page - 1,
          sort: "status,asc",
        };
        let filter: JobOfferFilter = {
          customerId: undefined,
          status: undefined,
          professionalId: prof.id.toString(),
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

  return (
    <Container>
      <br />
      <CardJobOffer
        offers={jobOffers}
        page={page}
        setPage={setPage}
        totalPage={totalPage}
        cardInfo={
          "In this section, you can view the job offers that has been selected for you and track their progress."
        }
        cardTitle={"Job Offers List"}
      />
    </Container>
  );
}
