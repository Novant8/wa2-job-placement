import {Contact} from "./contact.ts";

export interface Customer {
    id: number;
    contactInfo: Contact;
    notes?: string;
}