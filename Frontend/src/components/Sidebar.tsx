import { useAuth, userHasAnyRole } from "../contexts/auth.tsx";
import { useLocation, useNavigate } from "react-router-dom";
import { Button, ListGroup } from "react-bootstrap";
import { CiHome, CiLogout } from "react-icons/ci";

export default function Sidebar() {
  const { me } = useAuth();

  const navigate = useNavigate();
  const location = useLocation();

  return (
    <>
      <ListGroup variant="flush" className="mb-1">
        <ListGroup.Item>
          <strong>
            {me?.name} {me?.surname}
          </strong>
          <br />
          <span className="text-muted">{me?.roles?.join(", ")}</span>
        </ListGroup.Item>
        <ListGroup.Item
          action
          active={location.pathname === "/"}
          onClick={() => navigate("/")}
        >
          <CiHome size={20} /> Homepage
        </ListGroup.Item>
        <ListGroup.Item
          action
          active={location.pathname === "/edit-account"}
          onClick={() => navigate("/edit-account")}
        >
          Manage Profile
        </ListGroup.Item>
        <ListGroup.Item
          action
          active={location.pathname === "/crm/job-offers"}
          onClick={() => navigate("/crm/job-offers")}
        >
          Job Offers
        </ListGroup.Item>
        {userHasAnyRole(me, ["manager", "operator", "customer"]) && (
          <ListGroup.Item
            action
            active={location.pathname === "/crm/professionals"}
            onClick={() => navigate("/crm/professionals")}
          >
            Professionals
          </ListGroup.Item>
        )}
        {userHasAnyRole(me, ["manager", "operator", "professional"]) && (
          <ListGroup.Item
            action
            active={location.pathname === "/crm/customers"}
            onClick={() => navigate("/crm/customers")}
          >
            Customers
          </ListGroup.Item>
        )}
        {userHasAnyRole(me, ["manager", "operator"]) && (
          <>
            <ListGroup.Item
              action
              active={location.pathname === "/communication-manager"}
              onClick={() => navigate("/communication-manager")}
            >
              Send Email
            </ListGroup.Item>
            <ListGroup.Item
              action
              active={location.pathname === "/messages"}
              onClick={() => navigate("/messages")}
            >
              Messages Received
            </ListGroup.Item>
          </>
        )}
        {userHasAnyRole(me, ["manager"]) && (
          <ListGroup.Item
            action
            onClick={() => (window.location.href = "http://localhost:3000")}
          >
            Monitoring & Analytics
          </ListGroup.Item>
        )}
        <ListGroup.Item></ListGroup.Item>
      </ListGroup>

      <Button onClick={() => (window.location.href = me?.logoutUrl as string)}>
        <CiLogout size={24} />
        Logout
      </Button>
    </>
  );
}
