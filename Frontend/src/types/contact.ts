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

export interface ReducedContact {
    id : number;
    name : string;
    surname: string;
    category : ContactCategory;
}