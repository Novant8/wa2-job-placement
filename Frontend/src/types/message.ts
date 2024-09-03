import { Address } from "./address.ts";

export type MessageChannel =
  | "EMAIL"
  | "PHONE_CALL"
  | "TEXT_MESSAGE"
  | "POSTAL_MAIL";

export type MessageStatus =
  | "RECEIVED"
  | "READ"
  | "DISCARDED"
  | "PROCESSING"
  | "DONE"
  | "FAILED";

export interface LocalDateTime {
  year: number | null;
  month: number | null;
  day: number | null;
  hour: number | null;
  minute: number | null;
  second: number | null;
  nano: number | null;
}

export interface MessageEventDTO {
  status: MessageStatus;
  timestamp: LocalDateTime | string | null;
  comments: string | null;
}

export interface Message {
  id: number;
  sender: Address;
  channel: MessageChannel;
  subject: string;
  body: string;
  priority: number;
  creationTimestamp: LocalDateTime | string | null;
  lastEvent: MessageEventDTO;
}

export interface MessageCreate {
  sender: {
    email: string;
  };
  channel: MessageChannel;
  subject: string;
  body: string;
}
