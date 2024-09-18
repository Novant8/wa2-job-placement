import { useAuth } from "../contexts/auth.tsx";
import { useNavigate } from "react-router-dom";
import { Button, ListGroup } from "react-bootstrap";
import { CiHome, CiLogout } from "react-icons/ci";

export default function Sidebar() {
  const { me } = useAuth();
  const navigate = useNavigate();
  return (
    <>
      <ListGroup variant="flush">
        <ListGroup.Item>
          <strong>
            {me?.name} {me?.surname}
          </strong>
          <br />
          <span className="text-muted">{me?.roles?.join(", ")}</span>
        </ListGroup.Item>
        <ListGroup.Item action onClick={() => navigate("/")}>
          <CiHome size={20} /> Homepage
        </ListGroup.Item>
        <ListGroup.Item action onClick={() => navigate("/edit-account")}>
          Manage Profile
        </ListGroup.Item>
        <ListGroup.Item action onClick={() => navigate("/crm/job-offers")}>
          Job Offers
        </ListGroup.Item>
        <ListGroup.Item action onClick={() => navigate("/crm/professionals")}>
          Professionals
        </ListGroup.Item>
        <ListGroup.Item action onClick={() => navigate("/crm/customers")}>
          Customers
        </ListGroup.Item>
        <ListGroup.Item
          action
          onClick={() => navigate("/communication-manager")}
        >
          Send Email
        </ListGroup.Item>
      </ListGroup>

      <Button onClick={() => (window.location.href = me?.logoutUrl as string)}>
        <CiLogout size={24} />
        Logout
      </Button>
    </>
  );
}
