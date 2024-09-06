import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { useEffect, useState } from "react";
import { Message } from "../types/message.ts";
import { Pageable } from "../types/Pageable.ts";
import {
  Accordion,
  Button,
  Container,
  Spinner,
  Tab,
  Tabs,
} from "react-bootstrap";
import UpdateMessageStatusModal from "./UpdateMessageStatusModal.tsx";

export default function MessagesView() {
  const { me } = useAuth();
  const [messages, setMessages] = useState({});
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [pageable, setPageable] = useState<Pageable | null>(null);
  const [totalPages, setTotalPages] = useState<number | null>(null);
  const [status, setStatus] = useState<string | undefined>("RECEIVED");
  const [selected, setSelected] = useState<Message | null>(null);
  const [modalAction, setModalAction] = useState<string>("");
  const [modalStatusShow, setModalStatusShow] = useState<boolean>(false);
  const [dirty, setDirty] = useState<boolean>(false);
  type MessageAccordionProps = {
    msg: Message;
  };
  function MessageAccordion(props: MessageAccordionProps) {
    //const navigate = useNavigate();
    return (
      <div>
        <Accordion.Item eventKey={props.msg?.id?.toString()}>
          <Accordion.Header>{props.msg.subject}</Accordion.Header>
          <Accordion.Body>
            {props.msg.sender ? (
              <div>SENDER: {props.msg.sender.email}</div>
            ) : (
              ""
            )}
            <div>Message: {props.msg.body}</div>
            <div>
              Last Event: {props.msg.lastEvent.status} on{" "}
              {props.msg.lastEvent.timestamp?.toString()}
            </div>
            {}
            <div>
              Comment:{" "}
              {props.msg.lastEvent.comments
                ? props.msg.lastEvent.comments
                : "N/A"}
            </div>
            <div>Channel:{props.msg.channel}</div>
            <div>
              Date of the message: {props.msg.creationTimestamp?.toString()}
            </div>

            {props.msg.lastEvent.status === "RECEIVED" && (
              <Button
                className="primary mt-3"
                variant={"info"}
                style={{ marginRight: 10 }}
                onClick={() => {
                  setModalAction("read");
                  setSelected(props.msg);
                  setModalStatusShow(true);
                }}
              >
                Mark as Read
              </Button>
            )}

            {props.msg.lastEvent.status !== "DONE" &&
              props.msg.lastEvent.status !== "PROCESSING" &&
              props.msg.lastEvent.status !== "DISCARDED" &&
              props.msg.lastEvent.status !== "FAILED" &&
              props.msg.lastEvent.status !== "RECEIVED" && (
                <>
                  <Button
                    className="primary mt-3"
                    variant={"warning"}
                    style={{ marginRight: 10 }}
                    onClick={() => {
                      setModalAction("processing");
                      setSelected(props.msg);
                      setModalStatusShow(true);
                    }}
                  >
                    Mark as Processing
                  </Button>
                </>
              )}

            {props.msg.lastEvent.status !== "DONE" &&
              props.msg.lastEvent.status !== "DISCARDED" &&
              props.msg.lastEvent.status !== "FAILED" &&
              props.msg.lastEvent.status !== "RECEIVED" && (
                <Button
                  className="primary mt-3"
                  variant={"success"}
                  style={{ marginRight: 10 }}
                  onClick={() => {
                    setModalAction("done");
                    setSelected(props.msg);
                    setModalStatusShow(true);
                  }}
                >
                  Mark as Done
                </Button>
              )}

            {props.msg.lastEvent.status === "READ" && (
              <Button
                className="primary mt-3"
                variant={"danger"}
                onClick={() => {
                  setModalAction("discard");
                  setSelected(props.msg);
                  setModalStatusShow(true);
                }}
              >
                Discard Message
              </Button>
            )}
          </Accordion.Body>
        </Accordion.Item>
      </div>
    );
  }

  function handleSelect(key: string | null) {
    switch (key) {
      case "received":
        setStatus("RECEIVED");
        break;
      case "read":
        setStatus("READ");
        break;
      case "processing":
        setStatus("PROCESSING");
        break;
      case "done":
        setStatus("DONE");
        break;
      case "discarded":
        setStatus("DISCARDED");
        break;
      case "failed":
        setStatus("FAILED");
        break;
    }
  }

  useEffect(() => {
    const token = me?.xsrfToken;
    setLoading(true);
    API.getMessagges(token, status)
      .then((msg) => {
        setMessages(msg);
        setPageable(msg.pageable);
        setTotalPages(msg.totalPages);
      })
      .catch((err) => {
        console.log(err);
        setError(err);
      })
      .finally(() => {
        setLoading(false);
        setDirty(false);
      });
  }, [status, dirty]);

  //TODO: remove this useEffect
  useEffect(() => {
    console.log(messages);
  }, [messages, status, dirty]);

  if (error) {
    return (
      <Container className="text-center mt-5">
        <h4>Messages</h4>
        <p>{error}</p>
      </Container>
    );
  }

  return (
    <Container className="text-center mt-5">
      <UpdateMessageStatusModal
        show={modalStatusShow}
        action={modalAction}
        onHide={() => setModalStatusShow(false)}
        message={selected}
        setDirty={() => setDirty(true)}
      />

      <h4>Messages</h4>
      <Tabs
        defaultActiveKey="received"
        id="justify-tab-example"
        className="mb-3"
        justify
        onSelect={(eventKey) => handleSelect(eventKey)}
      >
        <Tab eventKey="received" title="Received">
          {loading && (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          )}

          {messages?.content?.length > 0 ? (
            <Accordion>
              {messages &&
                messages.content?.map((e) => (
                  <MessageAccordion key={e.id} msg={e} />
                ))}
            </Accordion>
          ) : (
            <div>There no received messages</div>
          )}
        </Tab>
        <Tab eventKey="read" title="Read">
          {loading && (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          )}

          {messages?.content?.length > 0 ? (
            <Accordion>
              {messages &&
                messages.content?.map((e) => (
                  <MessageAccordion key={e.id} msg={e} />
                ))}
            </Accordion>
          ) : (
            <div>There no read messages</div>
          )}
        </Tab>
        <Tab eventKey="processing" title="Processing">
          {loading && (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          )}

          {messages?.content?.length > 0 ? (
            <Accordion>
              {messages &&
                messages.content?.map((e) => (
                  <MessageAccordion key={e.id} msg={e} />
                ))}
            </Accordion>
          ) : (
            <div>There no processing messages</div>
          )}
        </Tab>
        <Tab eventKey="done" title="Done">
          {loading && (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          )}

          {messages?.content?.length > 0 ? (
            <Accordion>
              {messages &&
                messages.content?.map((e) => (
                  <MessageAccordion key={e.id} msg={e} />
                ))}
            </Accordion>
          ) : (
            <div>There no done messages</div>
          )}
        </Tab>
        <Tab eventKey="discarded" title="Discarded">
          {loading && (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          )}

          {messages?.content?.length > 0 ? (
            <Accordion>
              {messages &&
                messages.content?.map((e) => (
                  <MessageAccordion key={e.id} msg={e} />
                ))}
            </Accordion>
          ) : (
            <div>There no discarded messages</div>
          )}
        </Tab>
        <Tab eventKey="failed" title="Failed">
          {loading && (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          )}

          {messages?.content?.length > 0 ? (
            <Accordion>
              {messages &&
                messages.content?.map((e) => (
                  <MessageAccordion key={e.id} msg={e} />
                ))}
            </Accordion>
          ) : (
            <div>There no failed messages</div>
          )}
        </Tab>
      </Tabs>
      {pageable && (
        <div className="mt-4">
          <p>
            Page {pageable.pageNumber + 1} of {totalPages}
          </p>
        </div>
      )}
    </Container>
  );
}
