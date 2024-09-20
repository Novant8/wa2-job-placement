import { Button, Card, Col, ListGroup, Row, Spinner } from "react-bootstrap";
import { useAuth } from "../contexts/auth.tsx";
import { useEffect, useState } from "react";
import Sidebar from "./Sidebar.tsx";
import * as API from "../../API.tsx";
import CardJobOffer from "./Card/CardJobOffer.tsx";
import { JobOfferFilter } from "../types/JobOfferFilter.ts";
import { Message } from "../types/message.ts";
import { CiCircleInfo } from "react-icons/ci";
import PaginationCustom from "./PaginationCustom.tsx";

export default function HomePageLoggedIn() {
  const { me } = useAuth();
  return (
    <>
      {me?.roles.includes("customer") ? (
        <DashboardCustomer />
      ) : me?.roles.includes("operator") ? (
        <DashboardOperator />
      ) : (
        <DashboardMain />
      )}
    </>
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
    <>
      <Row>
        <Col xs={2}>
          <Sidebar />
        </Col>
        <Col xs={10}>
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
        </Col>
      </Row>
    </>
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
    <>
      <Row>
        <Col xs={2}>
          <Sidebar />
        </Col>

        <Col xs={10}>
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
        </Col>
      </Row>
    </>
  );
}

// Dashboard Main Component
const DashboardMain = () => {
  return (
    <>
      <Row>
        <Col xs={2}>
          <Sidebar />
        </Col>
        <Col xs={10}>
          <h2>Dashboard Overview</h2>

          {/* Job Applications Card */}
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Title>Recent Applications</Card.Title>
              <Card.Text>Track your job application progress here.</Card.Text>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  Software Engineer - ABC Corp
                  <Button variant="link" className="float-right">
                    View Details
                  </Button>
                </ListGroup.Item>
                <ListGroup.Item>
                  Product Manager - XYZ Inc
                  <Button variant="link" className="float-right">
                    View Details
                  </Button>
                </ListGroup.Item>
                <ListGroup.Item>
                  Data Analyst - 123 Tech
                  <Button variant="link" className="float-right">
                    View Details
                  </Button>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>

          {/* Notifications Card */}
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Title>Notifications</Card.Title>
              <Card.Text>
                Stay updated with the latest job openings and messages.
              </Card.Text>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  New job opening for Front-End Developer at DEF Corp
                  <Button variant="link" className="float-right">
                    View
                  </Button>
                </ListGroup.Item>
                <ListGroup.Item>
                  Reminder: Update your profile to increase visibility
                  <Button variant="link" className="float-right">
                    Update
                  </Button>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>

          {/* Profile Status Card */}
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Title>Profile Completeness</Card.Title>
              <Card.Text>
                Ensure your profile is up-to-date to attract more employers.
              </Card.Text>
              <Button variant="primary">Update Profile</Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </>
  );
};
