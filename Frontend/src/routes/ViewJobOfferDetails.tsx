import { useAuth } from "../contexts/auth.tsx";
import { Col, Container, Row } from "react-bootstrap";
import Sidebar from "../components/Sidebar.tsx";
import ViewCustomerJobOffer from "../components/ViewCustomerJobOffer.tsx";
import ViewRecruiterJobOffer from "../components/ViewRecruiterJobOffer.tsx";
import ViewProfessionalJobOffer from "../components/ViewProfessionalJobOffer.tsx";
import ViewJobOfferDetailsCustomers from "../components/ViewJobOfferDetailsCustomers.tsx";
import ViewJobOfferDetailsRecruiter from "../components/ViewJobOfferDetailRecruiter.tsx";
import ViewJobOfferDetailProfessional from "../components/ViewJobOfferDetailProfessional.tsx";

export default function ViewJobOfferDetails() {
  const { me } = useAuth();
  return (
    <>
      <Container fluid>
        <Row>
          <Col xs={2}>
            <Sidebar />
          </Col>
          <Col xs>
            {me?.roles.includes("customer") ? (
              <ViewJobOfferDetailsCustomers />
            ) : me?.roles.includes("operator") ||
              me?.roles.includes("manager") ? (
              <ViewJobOfferDetailsRecruiter />
            ) : (
              <ViewJobOfferDetailProfessional />
            )}
          </Col>
        </Row>
      </Container>
    </>
  );
}
