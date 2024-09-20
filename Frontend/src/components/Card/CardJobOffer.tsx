import { Card, Col, Row } from "react-bootstrap";
import JobOfferBadge from "../Badges/JobOfferBadge.tsx";
import { CiCircleInfo, CiZoomIn } from "react-icons/ci";
import PaginationCustom from "../PaginationCustom.tsx";
import { useNavigate } from "react-router-dom";
import { ReducedJobOffer } from "../../types/JobOffer.ts";

export type CardJobOfferProps = {
  offers: ReducedJobOffer[];
  page: number;
  setPage: (n: number) => void;
  totalPage: number;
  cardTitle: string;
  cardInfo: string;
};

export default function CardJobOffer(props: CardJobOfferProps) {
  let offers = props.offers;
  let page = props.page;
  let setPage = props.setPage;
  let totalPage = props.totalPage;
  let title = props.cardTitle;
  let info = props.cardInfo;

  const navigate = useNavigate();
  return (
    <Card>
      <Card.Header>
        <Card.Title>{title}</Card.Title>
      </Card.Header>
      <Card.Body>
        <div className="d-flex">
          <CiCircleInfo size={30} color="green" className="me-2" />
          <span>{info}</span>
        </div>
        {offers.length == 0 ? (
          <>
            <Card className="m-3">
              <Card.Body>
                <strong>There is no job offers available</strong>
              </Card.Body>
            </Card>
          </>
        ) : (
          <></>
        )}
        {offers.map((offer) => (
          <Card className="m-1" key={offer.id}>
            <Card.Body>
              <Row className="align-items-center">
                <Col xs={3}>
                  {offer.customer?.contactInfo.name +
                    " " +
                    offer.customer?.contactInfo.surname}
                  <br />
                </Col>
                <Col
                  xs={6}
                  className="border border-top-0 border-bottom-0 border-end-0  "
                >
                  <strong> {offer.description}</strong>
                </Col>
                <Col
                  xs={2}
                  className="border border-left-0  border-top-0 border-bottom-0  p-1"
                >
                  <JobOfferBadge status={offer.offerStatus} /> <br />
                  {offer.professional ? (
                    offer.professional.contactInfo.name +
                    " " +
                    offer.professional.contactInfo.surname
                  ) : (
                    <></>
                  )}
                </Col>
                <Col xs={1}>
                  <CiZoomIn
                    size={30}
                    color="black"
                    style={{
                      backgroundColor: "white",
                      borderRadius: "20%", // Makes the background fully rounded
                      // padding: "10px", // Adds space around the icon inside the circle
                      transition: "color 0.3s ease, background-color 0.3s ease",
                    }}
                    onClick={() => navigate(`/crm/job-offers/${offer.id}`)}
                    onMouseOver={(e) => {
                      e.currentTarget.style.backgroundColor = "#e9ecef";
                      e.currentTarget.style.cursor = "pointer"; // Optional: change cursor on hover
                    }}
                    onMouseOut={(e) => {
                      e.currentTarget.style.backgroundColor = "white";
                    }}
                  />
                </Col>
              </Row>
            </Card.Body>
          </Card>
        ))}

        <PaginationCustom setPage={setPage} page={page} totalPage={totalPage} />
      </Card.Body>
    </Card>
  );
}
