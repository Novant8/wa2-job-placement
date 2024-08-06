import {Address} from "./address.ts";

export type ContactCategory = "CUSTOMER" | "PROFESSIONAL" | "UNKNOWN"

export interface Contact {
    id: number;
    name: string;
    surname: string;
    category: ContactCategory;
    ssn?: string;
    addresses: Address[];
}

export interface Customer {
    id: number;
    contactInfo: Contact;
    notes?: string;
}

export type UserSkill = string;
export type EmploymentState = "UNEMPLOYED" | "EMPLOYED" | "NOT_AVAILABLE";
export interface Professional {
    id: number;
    contactInfo: Contact;
    location: string;
    skills: UserSkill[];
    dailyRate: number;
    employedState: EmploymentState;
    notes?: string;
}