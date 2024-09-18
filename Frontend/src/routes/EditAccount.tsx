import EditAccountForm from "../components/EditAccountForm.tsx";
import Sidebar from "../components/Sidebar.tsx";
import { Col, Container, Row } from "react-bootstrap";

export default function EditAccount() {
  return (
    <>
      <Container fluid>
        <Row>
          <Col xs={2}>
            <Sidebar />
          </Col>
          <Col xs>
            <Container>
              <EditAccountForm />
            </Container>
          </Col>
        </Row>
      </Container>
    </>
  );
}
