import { ListGroup } from "react-bootstrap";
import { useAuth } from "../contexts/auth.tsx";

enum SelectedItem {
  ViewJobOffers = "ViewJobOffers",
  CreateJobOffer = "CreateJobOffer",
  Professionals = "Professionals",
  Customers = "Customers",
}

interface AsideProps {
  onSelect: (item: SelectedItem) => void;
}

export default function Aside({ onSelect }: AsideProps) {
  const { me } = useAuth();
  return (
    <div>
      <h2>CRM</h2>
      <ListGroup>
        <ListGroup.Item
          className="my-3"
          action
          onClick={() => onSelect(SelectedItem.ViewJobOffers)}
        >
          View Job Offers
        </ListGroup.Item>

        {me?.roles.includes("customer") && (
          <ListGroup.Item
            action
            onClick={() => onSelect(SelectedItem.CreateJobOffer)}
          >
            Create Job Offer
          </ListGroup.Item>
        )}

        {me?.roles.includes("operator") && (
          <>
            <ListGroup.Item
              style={{ marginBottom: 20 }}
              action
              onClick={() => onSelect(SelectedItem.Professionals)}
            >
              Professionals
            </ListGroup.Item>

            <ListGroup.Item
              action
              onClick={() => onSelect(SelectedItem.Customers)}
            >
              Customers
            </ListGroup.Item>
          </>
        )}
        {me?.roles.includes("manager") && (
          <>
            <ListGroup.Item
              style={{ marginBottom: 20 }}
              action
              onClick={() => onSelect(SelectedItem.Professionals)}
            >
              Professionals
            </ListGroup.Item>

            <ListGroup.Item
              action
              onClick={() => onSelect(SelectedItem.Customers)}
            >
              Customers
            </ListGroup.Item>
          </>
        )}
      </ListGroup>
    </div>
  );
}
