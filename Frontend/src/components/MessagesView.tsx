import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { useEffect, useState } from "react";
import { Message } from "../types/message.ts";
import { Pageable } from "../types/Pageable.ts";
import { Container, Spinner, Tab, Tabs } from "react-bootstrap";
export default function MessagesView() {
  const { me } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageStatuses, setMessageStatuses] = useState<string[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [pageable, setPageable] = useState<Pageable | null>(null);
  const [totalPages, setTotalPages] = useState<number | null>(null);
  const [status, setStatus] = useState<string | undefined>("RECEIVED");
  /*
  useEffect(() => {
    const token = me?.xsrfToken;
    setLoading(true);
    API.getMessagges(token)
      .then((msg) => {
        setMessages(msg);
        setPageable(msg.pageable);
        setTotalPages(msg.totalPages);
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => setLoading(false));
  }, []);*/

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
      })
      .finally(() => setLoading(false));
  }, [status]);

  useEffect(() => {
    console.log(messages);
  }, [messages, status]);

  /*if (loading) {
    return (
      <Container className="text-center mt-5">
        <Spinner animation="border" />
      </Container>
    );
  }*/

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
      <h4>Messages</h4>
      <Tabs
        defaultActiveKey="received"
        id="justify-tab-example"
        className="mb-3"
        justify
        onSelect={(eventKey) => handleSelect(eventKey)}
      >
        <Tab eventKey="received" title="Received">
          {loading ? (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          ) : (
            "Content for received"
          )}
        </Tab>
        <Tab eventKey="read" title="Read">
          {loading ? (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          ) : (
            "Content for read"
          )}
        </Tab>
        <Tab eventKey="processing" title="Processing">
          {loading ? (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          ) : (
            "Content for processing"
          )}
        </Tab>
        <Tab eventKey="done" title="Done">
          {loading ? (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          ) : (
            "Content for done"
          )}
        </Tab>
        <Tab eventKey="discarded" title="Discarded">
          {loading ? (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          ) : (
            "Content for discarded"
          )}
        </Tab>
        <Tab eventKey="failed" title="Failed">
          {loading ? (
            <Container className="text-center mt-5">
              <Spinner animation="border" />
            </Container>
          ) : (
            "Content for failed"
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
