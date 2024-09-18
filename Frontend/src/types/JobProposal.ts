import { Customer } from "./customer.ts";
import { Professional } from "./professional.ts";
import { JobOffer } from "./JobOffer.ts";

export type JobProposalStatus = "CREATED" | "ACCEPTED" | "DECLINED";

export interface JobProposal {
  id: number;
  customer: Customer;
  professional: Professional;
  jobOffer: JobOffer;
  documentId: number | null;
  status: JobProposalStatus;
  customerConfirmation: boolean;
  professionalSignedContract: number | null;
}
