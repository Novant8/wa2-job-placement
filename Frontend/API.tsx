import { ProfessionalFilter } from "./src/types/professionalFilter.ts";
import { JobOfferFilter } from "./src/types/JobOfferFilter.ts";
import {
  JobOffer,
  JobOfferCreate,
  JobOfferResponse,
  JobOfferUpdateStatus,
} from "./src/types/JobOffer.ts";
import {
  CreateProfessionalReduced,
  Professional,
  UserSkill,
} from "./src/types/professional.ts";
import { Customer } from "./src/types/customer.ts";
import { Contact } from "./src/types/contact.ts";
import { Address, getAddressType } from "./src/types/address.ts";
import Cookies from "js-cookie";
import { CustomerFilter } from "./src/types/customerFilter.ts";
import { JobProposal } from "./src/types/JobProposal.ts";
import { DocumentHistory, DocumentMetadata } from "./src/types/documents.ts";
import { Message, MessageCreate } from "./src/types/message.ts";

interface ErrorResponseBody {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
}

type UnprocessableEntityResponseBody = ErrorResponseBody & {
  fieldErrors: {
    [field: string]: string;
  };
};

export class ApiError extends Error {
  fieldErrors?: UnprocessableEntityResponseBody["fieldErrors"];

  constructor(
    message: string,
    fieldErrors?: UnprocessableEntityResponseBody["fieldErrors"],
  ) {
    super(message);
    this.fieldErrors = fieldErrors;
  }
}

function isErrorResponseBody(body: any): body is ErrorResponseBody {
  return body.hasOwnProperty("");
}

function getCSRFCookie(): string {
  return Cookies.get("XSRF-TOKEN")!;
}

async function customFetch(
  input: RequestInfo | URL,
  init?: RequestInit,
): Promise<any | void> {
  let res;
  try {
    if (init && init.method !== "GET") {
      init.headers = {
        ...init.headers,
        "X-XSRF-TOKEN": getCSRFCookie()!,
      };
    }
    res = await fetch(input, init);
  } catch {
    throw new ApiError("Could not connect to the API server");
  }

  if (res.ok) {
    return res.json().catch(() => {});
  } else if (res.status === 422) {
    const errorBody = (await res.json()) as UnprocessableEntityResponseBody;
    throw new ApiError(
      `Server responded with: ${errorBody.title}`,
      errorBody.fieldErrors,
    );
  } else {
    const errorBody = (await res.json()) as ErrorResponseBody;
    const message = `Server responded with: ${isErrorResponseBody(errorBody) ? errorBody.title : "Generic error"}`;
    throw new ApiError(message);
  }
}
export function addJobOffer(
  job: JobOfferCreate,
  customerId: number,
): Promise<number> {
  return customFetch(`/crm/API/customers/${customerId}/job-offers`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(job),
  });
}
export function updateJobOffer(
  jobOfferId: string | undefined,
  job: JobOfferCreate,
): Promise<JobOffer> {
  return customFetch(`/crm/API/joboffers/${jobOfferId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(job),
  });
}

export function updateJobOfferStatus(
  jobOfferId: string | undefined,
  jobOfferUpdateStatus: JobOfferUpdateStatus,
): Promise<JobOffer> {
  return customFetch(`/crm/API/joboffers/${jobOfferId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(jobOfferUpdateStatus),
  });
}

export function getJobOfferDetails(
  jobOfferId: string | undefined,
): Promise<JobOffer> {
  return customFetch(`/crm/API/joboffers/${jobOfferId}`);
}

export function getCustomerJobOffers(
  customerId: number,
): Promise<JobOfferResponse> {
  return customFetch(`/crm/API/joboffers?customerId=${customerId}`);
}

export function getProfessionalJobOffer(
  professionalId: number,
): Promise<JobOfferResponse> {
  return customFetch(`/crm/API/joboffers?professionalId=${professionalId}`);
}
export function getProfessionalFromCurrentUser(): Promise<Professional> {
  return customFetch("/crm/API/professionals/user/me");
}
export function getCustomerFromCurrentUser(): Promise<Customer> {
  return customFetch("/crm/API/customers/user/me");
}

export function getContactFromCurrentUser(): Promise<Contact> {
  return customFetch("/crm/API/contacts/user/me");
}

export function updateProfessionalSkills(
  professionalId: number,
  skills: UserSkill[],
): Promise<Professional> {
  return customFetch(`/crm/API/professionals/${professionalId}/skills`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ skills }),
  });
}

export function bindContactToCustomer(contactId: number): Promise<Customer> {
  return customFetch(`/crm/API/contacts/${contactId}/customer`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: "{}",
  });
}

export function bindContactToProfessional(
  professionalId: number,
  professionalInfo: CreateProfessionalReduced,
): Promise<Professional> {
  return customFetch(`/crm/API/contacts/${professionalId}/professional`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(professionalInfo),
  });
}

export function updateContactField(
  contactId: number,
  field: "name" | "surname" | "ssn",
  value: string,
): Promise<Contact> {
  return customFetch(`/crm/API/contacts/${contactId}/${field}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ [field]: value }),
  });
}

export function addAddressToContact(
  contactId: number,
  address: Address,
): Promise<Contact> {
  const { id, ...addressData } = address;
  return customFetch(
    `/crm/API/contacts/${contactId}/${getAddressType(address)!.toLowerCase()}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(addressData),
    },
  );
}

export function editContactAddress(
  contactId: number,
  address: Address,
): Promise<Contact> {
  const { id: addressId, ...addressData } = address;
  return customFetch(
    `/crm/API/contacts/${contactId}/${getAddressType(address)!.toLowerCase()}/${addressId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(addressData),
    },
  );
}

export function removeAddressFromContact(
  contactId: number,
  address: Address,
): Promise<void> {
  return customFetch(
    `/crm/API/contacts/${contactId}/${getAddressType(address)!.toLowerCase()}/${address.id}`,
    {
      method: "DELETE",
    },
  );
}

export type ProfessionalField = "dailyRate" | "location" | "cvDocument";

export function updateProfessionalField<T extends ProfessionalField>(
  professionalId: number,
  field: T,
  value: Professional[T],
): Promise<Professional> {
  return customFetch(`/crm/API/professionals/${professionalId}/${field}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ [field]: value }),
  });
}
export function getCustomerById(
  customerId: number | undefined,
): Promise<Customer> {
  return customFetch(`/crm/API/customers/${customerId}`);
}
export function getProfessionalById(
  professionalId: number | undefined,
): Promise<Professional> {
  return customFetch(`/crm/API/professionals/${professionalId}`);
}
export function getCustomers(filter?: CustomerFilter): Promise<any> {
  let endpoint = "/crm/API/customers";

  if (filter) {
    const params = new URLSearchParams();

    if (filter.fullName) params.append("fullName", filter.fullName);

    if (filter.email) params.append("email", filter.email);

    if (filter.telephone) params.append("telephone", filter.telephone);

    if (filter.address) params.append("address", filter.address);

    const queryString = params.toString();

    if (queryString) {
      endpoint += "?" + queryString;
    }
  }

  return customFetch(endpoint);
}

export function updateCustomerNotes(
  customerId: number | undefined,
  notes: string,
): Promise<Customer> {
  return customFetch(`/crm/API/customers/${customerId}/notes`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ notes: notes }),
  });
}

export function updateProfessionalNotes(
  professionalId: number | undefined,
  notes: string,
): Promise<Professional> {
  return customFetch(`/crm/API/professionals/${professionalId}/notes`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ notes: notes }),
  });
}

export function addCandidate(
  jobOfferId: number | undefined,
  professionalId: number | undefined,
): Promise<JobOffer> {
  return customFetch(
    `/crm/API/joboffers/${jobOfferId}/candidate/${professionalId}`,
    {
      method: "POST",
    },
  );
}

export function addRefusedCandidate(
  jobOfferId: number | undefined,
  professionalId: number | undefined,
): Promise<JobOffer> {
  return customFetch(
    `/crm/API/joboffers/${jobOfferId}/refusedCandidate/${professionalId}`,
    {
      method: "POST",
    },
  );
}

export function removeCandidate(
  jobOfferId: number | undefined,
  professionalId: number | undefined,
): Promise<JobOffer> {
  return customFetch(
    `/crm/API/joboffers/${jobOfferId}/candidate/${professionalId}`,
    {
      method: "DELETE",
    },
  );
}

export function uploadDocument(document: File): Promise<DocumentMetadata> {
  const formData = new FormData();
  formData.append("document", document, document.name);
  return customFetch(`/upload/document`, {
    method: "POST",
    body: formData,
  });
}
export function createJobProposal(
  customerId: number | undefined,
  jobOfferId: number | undefined,
  professionalId: number | undefined,
): Promise<any> {
  return customFetch(
    `/crm/API/jobProposals/${customerId}/${professionalId}/${jobOfferId}`,
    {
      method: "POST",
    },
  );
}

export function updateDocument(
  historyId: number,
  document: File,
): Promise<DocumentMetadata> {
  const formData = new FormData();
  formData.append("document", document, document.name);
  return customFetch(`/upload/document/${historyId}`, {
    method: "PUT",
    body: formData,
  });
}

export function getJobProposalbyId(
  proposalId: number | undefined,
): Promise<JobProposal> {
  return customFetch(`/crm/API/jobProposals/${proposalId}`);
}

export function getDocumentHistory(
  historyId: number,
): Promise<DocumentHistory> {
  return customFetch(`/document-store/API/documents/${historyId}/history`);
}

export function deleteDocumentHistory(historyId: number): Promise<void> {
  return customFetch(`/document-store/API/documents/${historyId}`, {
    method: "DELETE",
  });
}

export function deleteDocumentVersion(
  historyId: number,
  versionId: number,
): Promise<void> {
  return customFetch(
    `/document-store/API/documents/${historyId}/version/${versionId}`,
    { method: "DELETE" },
  );
}
export function getJobProposalbyOfferAndProfessional(
  offerId: number | undefined,
  professionalId: number | undefined,
): Promise<JobProposal> {
  return customFetch(`/crm/API/jobProposals/${offerId}/${professionalId}`);
}

export function customerConfirmDeclineJobProposal(
  proposalId: number | undefined,
  customerId: number | undefined,
  customerConfirm: boolean | undefined,
): Promise<JobProposal> {
  return customFetch(`/crm/API/jobProposals/${proposalId}/${customerId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(customerConfirm),
  });
}

export function professionalConfirmDeclineJobProposal(
  proposalId: number | undefined,
  professionalId: number | undefined,
  professionalConfirm: boolean | undefined,
): Promise<JobProposal> {
  return customFetch(
    `/crm/API/jobProposals/professional/${proposalId}/${professionalId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(professionalConfirm),
    },
  );
}

export function loadJobProposalDocument(
  proposalId: number | undefined,
  documentId: number | null,
): Promise<JobProposal> {
  return customFetch(`/crm/API/jobProposals/${proposalId}/document`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(documentId),
  });
}

export function createMessage(msg: MessageCreate): Promise<number> {
  return customFetch(`/crm/API/message`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(msg),
  });
}

const url: string = "http://localhost:8080";

export async function getProfessionals(
  token: string | undefined,
  filterDTO?: ProfessionalFilter,
): Promise<any> {
  return new Promise((resolve, reject) => {
    let endpoint = url + "/crm/API/professionals";

    if (filterDTO) {
      const params = new URLSearchParams();

      if (filterDTO.skills && filterDTO.skills.length > 0) {
        params.append("skills", filterDTO.skills.join(","));
      }

      if (filterDTO.location) {
        params.append("location", filterDTO.location);
      }

      if (filterDTO.employmentState) {
        params.append("employmentState", filterDTO.employmentState);
      }

      const queryString = params.toString();
      if (queryString) {
        endpoint += "?" + queryString;
      }
    }

    fetch(endpoint, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-TOKEN": `${token}`,
      },
    })
      .then((response) => {
        if (response.ok) {
          response
            .json()
            .then((prof) => resolve(prof))
            .catch(() => {
              reject({ error: "Cannot parse server response." });
            });
        } else {
          response
            .json()
            .then((message) => {
              reject(message);
            })
            .catch(() => {
              reject({ error: "Cannot parse server response." });
            });
        }
      })
      .catch(() => {
        reject({ error: "Cannot communicate with the server." });
      });
  });
}

export async function getJobOfferRecruiter(
  token: string | undefined,
  filterDTO?: JobOfferFilter,
): Promise<any> {
  return new Promise((resolve, reject) => {
    let endpoint = url + "/crm/API/joboffers";

    if (filterDTO) {
      const params = new URLSearchParams();

      if (filterDTO.professionalId) {
        params.append("professionalId", filterDTO.professionalId);
      }

      if (filterDTO.status) {
        params.append("status", filterDTO.status);
      }

      if (filterDTO.customerId) {
        params.append("customerId", filterDTO.customerId);
      }

      const queryString = params.toString();
      if (queryString) {
        endpoint += "?" + queryString;
        console.log(endpoint);
      }
    }

    fetch(endpoint, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-TOKEN": `${token}`,
      },
    })
      .then((response) => {
        if (response.ok) {
          response
            .json()
            .then((prof) => resolve(prof))
            .catch(() => {
              reject({ error: "Cannot parse server response." });
            });
        } else {
          response
            .json()
            .then((message) => {
              reject(message);
            })
            .catch(() => {
              reject({ error: "Cannot parse server response." });
            });
        }
      })
      .catch(() => {
        reject({ error: "Cannot communicate with the server." });
      });
  });
}

const API = {
  getProfessionals,
  //getCustomers,
};
export default API;
