import {Contact} from "./contact.ts";

export type UserSkill = string;
export type EmploymentState = "UNEMPLOYED" | "EMPLOYED" | "NOT_AVAILABLE";
export interface Professional {
    id: number;
    contactInfo: Contact;
    location: string;
    skills: UserSkill[];
    dailyRate: number;
    employmentState: EmploymentState;
    notes?: string;
}
export type CreateProfessionalReduced = Pick<Professional, "location" | "dailyRate" | "skills" | "notes">