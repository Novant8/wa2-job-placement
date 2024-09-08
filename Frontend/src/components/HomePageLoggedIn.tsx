import {
  Button,
  Card,
  Col,
  Container,
  ListGroup,
  Nav,
  Navbar,
  Row,
} from "react-bootstrap";
import TopNavbar from "./TopNavbar.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { useState } from "react";
import Aside from "./Aside.tsx";
import AsideContent from "./AsideContent.tsx";
import { useNavigate } from "react-router-dom";
import Sidebar from "./Sidebar.tsx";

export default function HomePageLoggedIn() {
  const navigate = useNavigate();
  return (
    <>
      <DashboardMain />
    </>
  );
}

/*export default function HomePageLoggedIn() {
  const [selectedItem, setSelectedItem] = useState<SelectedItem | null>(null);

  const handleSelect = (item: SelectedItem) => {
    setSelectedItem(item);
  };

  return (
    <Container fluid>
      <Row>
        <Col xs={3}>
          <Aside onSelect={handleSelect} />
        </Col>
        <Col xs>
          <AsideContent selectedItem={selectedItem} />
        </Col>
      </Row>
    </Container>
  );
}*/

// Navbar Component
/*const DashboardNavBar = () => {
  return (
    <Navbar bg="dark" variant="dark" expand="lg" sticky="top">
      <Container>
        <Navbar.Brand href="#home">JobFind</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ml-auto">
            <Nav.Link href="#dashboard">Dashboard</Nav.Link>
            <Nav.Link href="#profile">Profile</Nav.Link>
            <Nav.Link href="#jobs">Jobs</Nav.Link>
            <Nav.Link href="#notifications">Notifications</Nav.Link>
            <Button variant="outline-primary" className="ml-2">
              Sign Out
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};*/

// Sidebar Component

// Dashboard Main Component
const DashboardMain = () => {
  return (
    <Container fluid className="my-4">
      <Row>
        <Col md={3}>
          <Sidebar />
        </Col>
        <Col md={9}>
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
    </Container>
  );
};
