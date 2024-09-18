//export type Statuses = string;
//export type EmploymentState = "UNEMPLOYED" | "EMPLOYED" | "NOT_AVAILABLE";
export interface JobOfferFilter {
  professionalId: string | undefined;
  customerId: string | undefined;
  status: string | undefined; //Statuses[];
}
