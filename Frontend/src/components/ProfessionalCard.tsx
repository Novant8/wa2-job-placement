import { Professional } from "../types/professional.ts";
import { useNavigate } from "react-router-dom";
import { Card, Col, Row } from "react-bootstrap";
import { CiZoomIn } from "react-icons/ci";
import EmploymentBadge from "./Badges/EmploymentBadge.tsx";

export type ProfessionalAccordionProps = {
  prof: Professional;
};

export default function CardProfessional(props: ProfessionalAccordionProps) {
  const navigate = useNavigate();
  return (
    <>
      <Card>
        <Card.Body>
          <Row className={"align-items-center justify-content-center"}>
            <Col className="border border-top-0 border-bottom-0 border-start-0 p-1 ">
              <EmploymentBadge status={props.prof.employmentState} />
            </Col>
            <Col className="border border-top-0 border-bottom-0 border-start-0 p-1 ">
              <b>
                {props.prof.contactInfo?.name} {props.prof.contactInfo?.surname}
              </b>
            </Col>
            <Col className="border border-top-0 border-bottom-0 border-start-0 p-1 ">
              {props.prof.location}
            </Col>
            <Col className="border border-top-0 border-bottom-0 border-start-0 p-1 ">
              <Col>
                {props.prof.notes ? (
                  <div>Notes: {props.prof.notes}</div>
                ) : (
                  <div style={{ color: "gray" }}>No notes</div>
                )}
              </Col>
            </Col>
            <Col xs={1}>
              <CiZoomIn
                size={30}
                color={"black"}
                onMouseOver={(e) => (e.currentTarget.style.color = "blue")} // Hover color
                onMouseOut={(e) => (e.currentTarget.style.color = "black")} // Original color
                onClick={() => navigate(`/crm/professionals/${props.prof.id}`)}
              />
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </>
  );
}
