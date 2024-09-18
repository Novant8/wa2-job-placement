import { Button, Card, Col, ListGroup, Row } from "react-bootstrap";
import { BsFillSendPlusFill } from "react-icons/bs";
import { emailContacts } from "../../types/emailContacts.ts";

export type AddressBookRowProps = {
  item: emailContacts;
  handleAddEmailAddrWithAddress: (email: string) => void;
};

export default function AddressBookRow(props: AddressBookRowProps) {
  return (
    <>
      <Card key={props.item.id} className="my-1">
        <Card.Header>
          <Card.Title>
            {props.item.name} {props.item.surname}
          </Card.Title>
        </Card.Header>
        <Card.Body>
          <ListGroup className="list-group-flush">
            {props.item.address.map((a: string, index: number) => (
              <ListGroup.Item key={index}>
                <Row>
                  <Col>{a}</Col>
                  <Col></Col>
                  <Col xs={1} className="px-1">
                    <Button
                      onClick={() => props.handleAddEmailAddrWithAddress(a)}
                    >
                      <BsFillSendPlusFill />
                    </Button>
                  </Col>
                </Row>
              </ListGroup.Item>
            ))}
          </ListGroup>
        </Card.Body>
      </Card>
    </>
  );
}
