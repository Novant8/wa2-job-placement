import { Pageable } from "./Pageable.ts";
import { ReducedProfessional } from "./professional.ts";
import { ReducedCustomer } from "./customer.ts";

export type JobOfferStatus =
  | "CREATED"
  | "SELECTION_PHASE"
  | "CANDIDATE_PROPOSAL"
  | "CONSOLIDATED"
  | "DONE"
  | "ABORTED";
export interface ReducedJobOffer {
  id: number;
  description: string;
  offerStatus: JobOfferStatus;
  professional: string | null;
}

export interface JobOfferResponse {
  content: ReducedJobOffer[];
  pageable: Pageable;
  totalPages: number;
}

export interface JobOffer {
  id: number;
  description: string;
  customer: ReducedCustomer;
  requiredSkills: string[];
  duration: number;
  notes?: string;
  professional: ReducedProfessional;
  value: number;
  offerStatus: JobOfferStatus;
  candidates: ReducedProfessional[];
}

export interface JobOfferCreate {
  description: string;
  requiredSkills: string[];
  duration: number;
  notes: string | null | undefined;
}

export interface JobOfferUpdateStatus {
  status: JobOfferStatus;
  professionalId?: number; //ReducedProfessional;
}
