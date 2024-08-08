import {Pageable} from "./Pageable.ts";

export interface JobOffer {
    id: number;
    description: string;
    offerStatus: string;
    professional: string | null;
}

export interface JobOfferResponse {
    content: JobOffer[];
    pageable: Pageable;
    totalPages:number
}