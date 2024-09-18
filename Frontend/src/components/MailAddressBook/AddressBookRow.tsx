import { Button, Card, Col, ListGroup, Row } from "react-bootstrap";
import { BsFillSendPlusFill } from "react-icons/bs";

export default function AddressBookRow({
  item,
  handleAddEmailAddrWithAddress,
}) {
  return (
    <>
      <Card key={item.id} className="my-1">
        <Card.Header>
          <Card.Title>
            {item.name} {item.surname}
          </Card.Title>
        </Card.Header>
        <Card.Body>
          <ListGroup className="list-group-flush">
            {item.address.map((a, index) => (
              <ListGroup.Item key={index}>
                <Row>
                  <Col>{a}</Col>
                  <Col></Col>
                  <Col xs={1} className="px-1">
                    <Button onClick={() => handleAddEmailAddrWithAddress(a)}>
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
