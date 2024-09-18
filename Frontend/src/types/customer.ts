import { Contact, ReducedContact } from "./contact.ts";

export interface Customer {
  id: number;
  contactInfo: Contact;
  notes?: string;
}

export interface ReducedCustomer {
  id: number;
  contactInfo: ReducedContact;
  notes?: string;
}
