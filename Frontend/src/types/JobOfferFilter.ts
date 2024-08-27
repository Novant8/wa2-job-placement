//export type Statuses = string;
//export type EmploymentState = "UNEMPLOYED" | "EMPLOYED" | "NOT_AVAILABLE";
export interface JobOfferFilter {
    professionalId: string;
    customerId: string;
    status: string;//Statuses[];
}