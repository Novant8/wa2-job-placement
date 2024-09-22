import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { useEffect, useState } from "react";
import { Message } from "../types/message.ts";

import {
  Accordion,
  Button,
  Card,
  Container,
  Spinner,
  Tab,
  Tabs,
} from "react-bootstrap";
import UpdateMessageStatusModal from "../components/UpdateMessageStatusModal.tsx";
import { ApiError } from "../../API.tsx";
import { CiCircleInfo } from "react-icons/ci";
import PaginationCustom from "../components/PaginationCustom.tsx";
import PageLayout from "../components/PageLayout.tsx";

export default function MessagesView() {
  const { me } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [page, setPage] = useState<number>(1);
  const [totalPage, setTotalPage] = useState<number>(1);
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
        <Accordion.Item eventKey={props.msg?.id?.toString()} className="my-2">
          <Accordion.Header className="d-flex justify-content-between align-items-center">
            <div>{props.msg.subject}&nbsp; </div>
            <div style={{ color: "gray", textAlign: "right" }}>
              {props.msg.sender.email}
            </div>
          </Accordion.Header>

          <Accordion.Body>
            <Card>
              <Card.Title>
                {" "}
                <Card.Header>
                  {props.msg.subject} <br />
                </Card.Header>
              </Card.Title>
              <Card.Body className="text-start">
                <strong> From: </strong>
                {props.msg.sender.email} {" ("}
                {props.msg.channel}) Received at{" "}
                {props.msg.creationTimestamp?.toString()}
                <hr />
                <strong>Message: </strong>
                {props.msg.body}
                <hr />
                <strong>Comment:</strong>{" "}
                {props.msg.lastEvent.comments
                  ? props.msg.lastEvent.comments
                  : "N/A"}
                <hr />
              </Card.Body>
            </Card>

            {}
            <div></div>

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

    let paging = {
      pageNumber: page - 1,
      pageSize: 5,
      sort: undefined,
    };

    API.getMessagges(token, status, paging)
      .then((msg) => {
        setMessages([]);
        setMessages(msg.content);
        setTotalPage(msg.totalPages);
      })
      .catch((err) => {
        console.log(err);
        setError(err);
      })
      .finally(() => {
        setLoading(false);
        setDirty(false);
      });
  }, [status, dirty, page]);

  useEffect(() => {
    setPage(1);
  }, [status]);
  if (error) {
    return (
      <Container className="text-center mt-5">
        <h4>Messages</h4>
        <p>Error</p>
      </Container>
    );
  }

  return (
    <PageLayout>
      <Container>
        <UpdateMessageStatusModal
          show={modalStatusShow}
          action={modalAction}
          onHide={() => setModalStatusShow(false)}
          message={selected}
          setDirty={() => setDirty(true)}
        />
        <Card>
          <Card.Title className="my-3">Messages Tool</Card.Title>
          <Card.Body>
            <CiCircleInfo size={30} color={"green"} /> In this section, you can
            explore all the messages received, allowing you to process them
          </Card.Body>
        </Card>
        <br />
        <Card>
          <Card.Body>
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
                {messages?.length > 0 ? (
                  <Accordion>
                    {messages &&
                      messages.map((e) => (
                        <MessageAccordion key={e.id} msg={e} />
                      ))}
                  </Accordion>
                ) : (
                  <div>There no received messages</div>
                )}
                <PaginationCustom
                  setPage={setPage}
                  page={page}
                  totalPage={totalPage}
                />
              </Tab>
              <Tab eventKey="read" title="Read">
                {loading && (
                  <Container className="text-center mt-5">
                    <Spinner animation="border" />
                  </Container>
                )}

                {messages?.length > 0 ? (
                  <Accordion>
                    {messages &&
                      messages.map((e) => (
                        <MessageAccordion key={e.id} msg={e} />
                      ))}
                  </Accordion>
                ) : (
                  <div>There no read messages</div>
                )}
                <PaginationCustom
                  setPage={setPage}
                  page={page}
                  totalPage={totalPage}
                />
              </Tab>
              <Tab eventKey="processing" title="Processing">
                {loading && (
                  <Container className="text-center mt-5">
                    <Spinner animation="border" />
                  </Container>
                )}

                {messages?.length > 0 ? (
                  <Accordion>
                    {messages &&
                      messages.map((e) => (
                        <MessageAccordion key={e.id} msg={e} />
                      ))}
                  </Accordion>
                ) : (
                  <div>There no processing messages</div>
                )}
                <PaginationCustom
                  setPage={setPage}
                  page={page}
                  totalPage={totalPage}
                />
              </Tab>
              <Tab eventKey="done" title="Done">
                {loading && (
                  <Container className="text-center mt-5">
                    <Spinner animation="border" />
                  </Container>
                )}

                {messages?.length > 0 ? (
                  <Accordion>
                    {messages &&
                      messages.map((e) => (
                        <MessageAccordion key={e.id} msg={e} />
                      ))}
                  </Accordion>
                ) : (
                  <div>There no done messages</div>
                )}
                <PaginationCustom
                  setPage={setPage}
                  page={page}
                  totalPage={totalPage}
                />
              </Tab>
              <Tab eventKey="discarded" title="Discarded">
                {loading && (
                  <Container className="text-center mt-5">
                    <Spinner animation="border" />
                  </Container>
                )}

                {messages?.length > 0 ? (
                  <Accordion>
                    {messages &&
                      messages.map((e) => (
                        <MessageAccordion key={e.id} msg={e} />
                      ))}
                  </Accordion>
                ) : (
                  <div>There no discarded messages</div>
                )}
                <PaginationCustom
                  setPage={setPage}
                  page={page}
                  totalPage={totalPage}
                />
              </Tab>
              <Tab eventKey="failed" title="Failed">
                {loading && (
                  <Container className="text-center mt-5">
                    <Spinner animation="border" />
                  </Container>
                )}

                {messages?.length > 0 ? (
                  <Accordion>
                    {messages &&
                      messages.map((e) => (
                        <MessageAccordion key={e.id} msg={e} />
                      ))}
                  </Accordion>
                ) : (
                  <div>There no failed messages</div>
                )}
                <PaginationCustom
                  setPage={setPage}
                  page={page}
                  totalPage={totalPage}
                />
              </Tab>
            </Tabs>
          </Card.Body>
        </Card>
      </Container>
    </PageLayout>
  );
}
