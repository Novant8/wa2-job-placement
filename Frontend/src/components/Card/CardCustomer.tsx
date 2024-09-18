import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Customer } from "../../types/customer.ts";
import * as API from "../../../API.tsx";
import { Alert, Button, Card, Col, Row } from "react-bootstrap";
import { CustomerAccordionProps } from "../../routes/CustomersView.tsx";
import EmploymentBadge from "../Badges/EmploymentBadge.tsx";
import { CiZoomIn } from "react-icons/ci";

export default function CardCustomer(props: CustomerAccordionProps) {
  const navigate = useNavigate();
  const [formError, setFormError] = useState("");
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
  });

  useEffect(() => {
    API.getCustomerById(props.cust.id)
      .then((customer) => setCustomer(customer))
      .catch((err) => setFormError(err.message));
  }, []);

  if (formError) {
    return (
      <Alert variant="danger">
        <strong>Error:</strong> {formError}
      </Alert>
    );
  }
  return (
    <Card>
      <Card.Body>
        <Row className={"align-items-center justify-content-center"}>
          <Col className="border border-top-0 border-bottom-0 border-start-0 p-1 ">
            <b>
              {props.cust.contactInfo?.name} {props.cust.contactInfo?.surname}
            </b>
          </Col>
          <Col
            xs={9}
            className="border border-top-0 border-bottom-0 border-start-0 p-1 "
          >
            <Col>
              {props.cust.notes ? (
                <div>{props.cust.notes}</div>
              ) : (
                <div style={{ color: "gray" }}>No notes</div>
              )}
            </Col>
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
              onClick={() => navigate(`/crm/customers/${props.cust.id}`)}
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
  );
}
