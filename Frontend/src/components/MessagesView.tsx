import * as API from "../../API.tsx";
import { useAuth } from "../contexts/auth.tsx";
import { useEffect, useState } from "react";
import { Message } from "../types/message.ts";
export default function MessagesView() {
  const { me } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageStatuses, setMessageStatuses] = useState<string[]>([]);

  useEffect(() => {
    const token = me?.xsrfToken;
    API.getMessagges(token)
      .then((prof) => {
        setMessages(prof);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  useEffect(() => {
    console.log(messages);
  }, [messages]);
  return <h4>Messages</h4>;
}
