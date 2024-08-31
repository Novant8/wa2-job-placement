import {Contact, ReducedContact} from "./contact.ts";

export type UserSkill = string;
export type EmploymentState = "UNEMPLOYED" | "EMPLOYED" | "NOT_AVAILABLE";
export interface Professional {
    id: number;
    contactInfo: Contact;
    location: string;
    skills: UserSkill[];
    dailyRate: number;
    employmentState: EmploymentState;
    cvDocument?: number;
    notes?: string;
}

export interface ReducedProfessional {
    id : number ;
    contactInfo : ReducedContact;
    location: string;
    skills: UserSkill[];
    employedState: EmploymentState;
}
export type CreateProfessionalReduced = Pick<Professional, "location" | "dailyRate" | "skills" | "notes">