import EditAccountForm from "../components/EditAccountForm.tsx";
import PageLayout from "../components/PageLayout.tsx";
import { Container } from "react-bootstrap";

export default function EditAccount() {
  return (
    <PageLayout>
      <Container>
        <EditAccountForm />
      </Container>
    </PageLayout>
  );
}
