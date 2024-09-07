import { Button, Card, ListGroup } from "react-bootstrap";
import React from "react";
import { BsFillSendPlusFill } from "react-icons/bs";
export default function AddressBook({ item, handleAddEmailAddrWithAddress }) {
  return (
    <Card key={item.id}>
      <Card.Body>
        <Card.Title>
          {item.name} {item.surname}
        </Card.Title>
        <ListGroup className="list-group-flush">
          {item.address.map((a, index) => (
            <ListGroup.Item className="list-group-item-action" key={index}>
              <Button onClick={() => handleAddEmailAddrWithAddress(a)}>
                <BsFillSendPlusFill />
              </Button>
              {a}
            </ListGroup.Item>
          ))}
        </ListGroup>
      </Card.Body>
    </Card>
  );
}
