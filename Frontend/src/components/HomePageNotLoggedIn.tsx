import { Button, Card, Col, Container, Row } from "react-bootstrap";

import { CiLogin } from "react-icons/ci";
import { useAuth } from "../contexts/auth.tsx";

export default function HomePageNotLoggedIn() {
  return (
    <div>
      <HeroSection />
      <ServicesSection />
      <TestimonialsSection />
      <Footer />
    </div>
  );
}

// Hero Section Component
const HeroSection = () => {
  const { me } = useAuth();
  return (
    <Container
      fluid
      className="p-5 bg-light text-center"
      style={{ height: "50vh" }}
    >
      <Row className="align-items-center h-100">
        <Col>
          <h1 className="display-4 font-weight-bold">Find Your Dream Job</h1>
          <p className="lead">
            Connecting professionals with top companies around the world.
          </p>

          <Button
            size="lg"
            variant={"info"}
            onClick={() => (window.location.href = me?.loginUrl as string)}
          >
            <CiLogin size={24} /> Login/Register{" "}
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

// Services Section
const ServicesSection = () => {
  const { me } = useAuth();
  return (
    <Container id="services" className="my-5">
      <h2 className="text-center mb-4">Our Services</h2>
      <Row>
        <Col md={6}>
          <Card className="mb-6 shadow-sm">
            <Card.Body>
              <Card.Title>Looking for a New Job?</Card.Title>
              <Card.Text>
                We specialize in connecting talented individuals like you with
                top employers across various industries. Whether you're just
                starting your career or are looking for the next big step, our
                platform makes it easy to find the perfect job that matches your
                skills, experience, and career goals.
              </Card.Text>

              <Button
                variant="primary"
                onClick={() => (window.location.href = me?.loginUrl as string)}
              >
                Find Jobs Now
              </Button>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6}>
          <Card className="mb-6 shadow-sm">
            <Card.Body>
              <Card.Title>Looking for New Employees?</Card.Title>

              <Card.Text>
                Finding the right talent is crucial to the success of your
                business. Our platform helps streamline the hiring process by
                connecting you with a pool of highly qualified candidates.
                Whether you're searching for entry-level professionals or
                experienced leaders, we can help you to hire the perfect fit for
                your team.
              </Card.Text>
              <Button
                variant="primary"
                onClick={() => (window.location.href = me?.loginUrl as string)}
              >
                Find Candidates Now
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

// Testimonials Section
const TestimonialsSection = () => {
  return (
    <Container id="testimonials" className="my-5 bg-light py-5">
      <h2 className="text-center mb-4">What People Are Saying</h2>
      <Row>
        <Col md={4}>
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Text>
                "JobFind helped me land a role at my dream company. Their
                services are top-notch!"
              </Card.Text>
              <Card.Footer className="text-muted">
                - Jane Doe, Software Engineer
              </Card.Footer>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Text>
                "The resume review service really helped me refine my
                application and stand out."
              </Card.Text>
              <Card.Footer className="text-muted">
                - John Smith, Product Manager
              </Card.Footer>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Text>
                "I couldn’t have done it without their career counseling. Highly
                recommend!"
              </Card.Text>
              <Card.Footer className="text-muted">
                - Sarah Johnson, Data Analyst
              </Card.Footer>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

// Footer Component
const Footer = () => {
  return (
    <footer className="bg-dark text-light py-4">
      <Container>
        <Row>
          <Col md={6}>
            <h5>JobFind</h5>
            <p>© 2024 JobFind Inc. All rights reserved.</p>
          </Col>
          <Col md={6} className="text-md-right">
            <a href="#facebook" className="text-light ml-2">
              Facebook
            </a>
            <a href="#twitter" className="text-light ml-2">
              Twitter
            </a>
            <a href="#linkedin" className="text-light ml-2">
              LinkedIn
            </a>
          </Col>
        </Row>
      </Container>
    </footer>
  );
};
