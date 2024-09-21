import { Col, Container, Row } from "react-bootstrap";
import Sidebar from "./Sidebar.tsx";
import { PropsWithChildren } from "react";

export default function PageLayout({ children }: PropsWithChildren) {
  return (
    <Container fluid>
      <Row>
        <Col lg={2} className="my-3">
          <Sidebar />
        </Col>
        <Col lg={10}>{children}</Col>
      </Row>
    </Container>
  );
}
