import { Card, Container, Spinner } from "react-bootstrap";
import { useAuth } from "../contexts/auth.tsx";
import { useEffect, useState } from "react";
import * as API from "../../API.tsx";
import CardJobOffer from "./Card/CardJobOffer.tsx";
import { JobOfferFilter } from "../types/JobOfferFilter.ts";
import { Message } from "../types/message.ts";
import { CiCircleInfo } from "react-icons/ci";
import PaginationCustom from "./PaginationCustom.tsx";
import PageLayout from "./PageLayout.tsx";
import { Link } from "react-router-dom";

export default function HomePageLoggedIn() {
  const { me } = useAuth();
  return (
    <>
      {me?.roles.includes("customer") ? (
        <DashboardCustomer />
      ) : me?.roles.includes("operator") || me?.roles.includes("manager") ? (
        <DashboardOperator />
      ) : me?.roles.includes("professional") ? (
        <DashboardProfessional />
      ) : (
        <DashboardUnknown />
      )}
    </>
  );
}

function DashboardProfessional() {
  const { me } = useAuth();
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [jobOffers, setJobOffers] = useState([]);

  const [page1, setPage1] = useState(1);
  const [totalPage1, setTotalPage1] = useState(1);
  const [jobOffers1, setJobOffers1] = useState([]);
  const [loading1, setLoading1] = useState(true);
  const [loading2, setLoading2] = useState(true);
  useEffect(() => {
    setLoading1(true);
    if (!me) return;

    let paging = {
      pageNumber: page - 1,
      pageSize: 2,
      sort: undefined,
    };

    API.getProfessionalFromCurrentUser()

      .then((prof) => {
        let filter: JobOfferFilter = {
          professionalId: prof.id.toString(),
          status: "CANDIDATE_PROPOSAL",
          customerId: undefined,
        };

        API.getJobOffers(paging, filter)
          .then((data) => {
            setJobOffers([]);
            setTotalPage(data.totalPages);
            setJobOffers(data.content);
            setLoading1(false);
          })
          .catch(() => {
            "Failed to fetch job offers";
          });
      })
      .catch(() => "Error");
  }, [me, page]);

  useEffect(() => {
    if (!me) return;
    setLoading2(true);
    let paging = {
      pageNumber: page1 - 1,
      pageSize: 2,
      sort: undefined,
    };

    API.getProfessionalFromCurrentUser()
      .then((prof) => {
        let filter: JobOfferFilter = {
          professionalId: prof.id.toString(),
          status: "CONSOLIDATED",
          customerId: undefined,
        };

        API.getJobOffers(paging, filter)
          .then((data) => {
            setJobOffers1([]);
            setTotalPage1(data.totalPages);
            setJobOffers1(data.content);
            setLoading2(false);
          })
          .catch(() => {
            "Failed to fetch job offers";
          });
      })
      .catch(() => "Error");
  }, [me, page1]);
  if (loading2 || loading1) {
    return <Spinner />;
  }
  return (
    <PageLayout>
      <CardJobOffer
        cardTitle={"Actual Job"}
        cardInfo={"Here the Job that  you are employed"}
        page={page1}
        totalPage={totalPage1}
        setPage={setPage1}
        offers={jobOffers1}
      />
      <br />
      <CardJobOffer
        cardTitle={"Proposed job"}
        cardInfo={
          "Here the proposed job for you, Inspect the job offer to see the available action"
        }
        page={page}
        totalPage={totalPage}
        setPage={setPage}
        offers={jobOffers}
      />
    </PageLayout>
  );
}

function DashboardCustomer() {
  const { me } = useAuth();
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [jobOffers, setJobOffers] = useState([]);

  const [page1, setPage1] = useState(1);
  const [totalPage1, setTotalPage1] = useState(1);
  const [jobOffers1, setJobOffers1] = useState([]);
  const [loading1, setLoading1] = useState(true);
  const [loading2, setLoading2] = useState(true);
  useEffect(() => {
    setLoading1(true);
    if (!me) return;

    let paging = {
      pageNumber: page - 1,
      pageSize: 2,
      sort: undefined,
    };

    API.getCustomerFromCurrentUser()

      .then((customer) => {
        let filter: JobOfferFilter = {
          customerId: customer.id.toString(),
          status: "CANDIDATE_PROPOSAL",
          professionalId: undefined,
        };

        API.getJobOffers(paging, filter)
          .then((data) => {
            setJobOffers([]);
            setTotalPage(data.totalPages);
            setJobOffers(data.content);
            setLoading1(false);
          })
          .catch(() => {
            "Failed to fetch job offers";
          });
      })
      .catch(() => "Error");
  }, [me, page]);

  useEffect(() => {
    if (!me) return;
    setLoading2(true);
    let paging = {
      pageNumber: page1 - 1,
      pageSize: 2,
      sort: undefined,
    };

    API.getCustomerFromCurrentUser()
      .then((customer) => {
        let filter: JobOfferFilter = {
          customerId: customer.id.toString(),
          status: "CONSOLIDATED",
          professionalId: undefined,
        };

        API.getJobOffers(paging, filter)
          .then((data) => {
            setJobOffers1([]);
            setTotalPage1(data.totalPages);
            setJobOffers1(data.content);
            setLoading2(false);
          })
          .catch(() => {
            "Failed to fetch job offers";
          });
      })
      .catch(() => "Error");
  }, [me, page1]);
  if (loading2 || loading1) {
    return <Spinner />;
  }
  return (
    <PageLayout>
      <Container>
        <CardJobOffer
          cardTitle={"Candidate to your job"}
          cardInfo={
            "Here the proposed candidate for your job, Inspect the job offer to see the available action"
          }
          page={page}
          totalPage={totalPage}
          setPage={setPage}
          offers={jobOffers}
        />
        <br />
        <CardJobOffer
          cardTitle={"Working for you"}
          cardInfo={"Here the professionals working for you"}
          page={page1}
          totalPage={totalPage1}
          setPage={setPage1}
          offers={jobOffers1}
        />
      </Container>
    </PageLayout>
  );
}

function DashboardOperator() {
  const { me } = useAuth();
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [jobOffers, setJobOffers] = useState([]);

  const [page1, setPage1] = useState(1);
  const [totalPage1, setTotalPage1] = useState(1);
  const [jobOffers1, setJobOffers1] = useState([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<Message[]>([]);
  const [page2, setPage2] = useState(1);
  const [totalPage2, setTotalPage2] = useState(1);

  useEffect(() => {
    setLoading(true);
    if (!me) return;

    let paging = {
      pageNumber: page - 1,
      pageSize: 2,
      sort: undefined,
    };

    let filter: JobOfferFilter = {
      customerId: undefined,
      status: "CREATED",
      professionalId: undefined,
    };

    API.getJobOffers(paging, filter)
      .then((data) => {
        setJobOffers([]);
        setTotalPage(data.totalPages);
        setJobOffers(data.content);
        setLoading(false);
      })
      .catch(() => {
        "Failed to fetch job offers";
      });
  }, [me, page]);
  useEffect(() => {
    setLoading(true);
    if (!me) return;

    let paging = {
      pageNumber: page1 - 1,
      pageSize: 2,
      sort: undefined,
    };

    let filter: JobOfferFilter = {
      customerId: undefined,
      status: "SELECTION_PHASE",
      professionalId: undefined,
    };

    API.getJobOffers(paging, filter)
      .then((data) => {
        setJobOffers1([]);
        setTotalPage1(data.totalPages);
        setJobOffers1(data.content);
        setLoading(false);
      })
      .catch(() => {
        "Failed to fetch job offers";
      });
  }, [me, page1]);

  useEffect(() => {
    const token = me?.xsrfToken;
    setLoading(true);
    let paging = {
      pageNumber: page2 - 1,
      sort: undefined,
      pageSize: 5,
    };

    API.getMessagges(token, "RECEIVED", paging)
      .then((msg) => {
        setMsg(msg.content);
        setTotalPage2(msg.totalPages);
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [page2]);

  if (loading) {
    return <Spinner />;
  }
  return (
    <PageLayout>
      <Container>
        <Card>
          <Card.Header>
            <Card.Title>Messages Received</Card.Title>
          </Card.Header>

          <Card.Body>
            <div className="d-flex">
              <CiCircleInfo size={30} color="green" className="me-2" />
              <span>Here the messages that has to be processed</span>
            </div>
            <hr />
            {msg.map((m, index) => (
              <div className="row mb-2" key={index}>
                <div className="col">
                  <strong>{m.subject}</strong>
                </div>
                <div className="col">{m.sender.email}</div>
              </div>
            ))}
            <PaginationCustom
              totalPage={totalPage2}
              page={page2}
              setPage={setPage2}
            />
          </Card.Body>
        </Card>
        <br />
        <CardJobOffer
          cardTitle={"Job offer to process"}
          cardInfo={"Here the new Job Offer, ready to be processed"}
          page={page}
          totalPage={totalPage}
          setPage={setPage}
          offers={jobOffers}
        />
        <br />
        <CardJobOffer
          cardTitle={"Waiting for interview"}
          cardInfo={"Here the Job Offer in Selection Phase"}
          page={page1}
          totalPage={totalPage1}
          setPage={setPage1}
          offers={jobOffers1}
        />
      </Container>
    </PageLayout>
  );
}

function DashboardUnknown() {
  return (
    <PageLayout>
      <Container>
        <Card>
          <Card.Header>
            <Card.Title>Unknown role</Card.Title>
          </Card.Header>

          <Card.Body>
            <div className="d-flex">
              <CiCircleInfo size={30} color="green" className="me-2" />
              <span>
                You have not yet chosen a role for your account. Please select
                it by navigating to the{" "}
                <Link to="/edit-account">Manage Profile</Link> section.
              </span>
            </div>
          </Card.Body>
        </Card>
      </Container>
    </PageLayout>
  );
}
