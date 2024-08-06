const url :string = "http://localhost:8080"
interface JobOfferCreateDTO {
    description: string;
    requiredSkills: string[];
    duration: number;
    notes: string |null;
}
async function addJobOffer(job: JobOfferCreateDTO, token: string | undefined):Promise<number> {
    return new Promise((resolve, reject)=>{
        //TODO: change fixed customer id
        fetch(url+ '/crm/API/customers/1/job-offers',{
            method: 'POST',

            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': `${token}`
            },
            body: JSON.stringify(job),
        }).then((response)=>{
            if (response.ok){
                response.json()
                    .then((id)=>resolve(id))
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }else{
                // analyze the cause of error
                response.json()
                    .then((message) => { reject(message); }) // error message in the response body
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }
        }).catch(() => { reject({ error: "Cannot communicate with the server." }) })
    })
}


async function getProfessionals(token: string | undefined):Promise<any> {
    return new Promise((resolve, reject)=>{
        fetch(url+ '/crm/API/professionals',{
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': `${token}`
            },
        }).then((response)=>{
            if (response.ok){
                response.json()
                    .then((prof)=>resolve(prof))
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }else{
                response.json()
                    .then((message) => { reject(message); })
                    .catch(() => { reject({ error: "Cannot parse server response." }) });
            }
        }).catch(() => { reject({ error: "Cannot communicate with the server." }) })
    })
}




const API = {
    addJobOffer,
    getProfessionals
};
export default API;
import {
    CreateProfessionalReduced,
    Professional,
    UserSkill
} from "./src/types/professional.ts";
import {Customer} from "./src/types/customer.ts";
import {Contact} from "./src/types/contact.ts";
import {Address, getAddressType} from "./src/types/address.ts";
import Cookies from "js-cookie";

interface ErrorResponseBody {
    type: string,
    title: string,
    status: number,
    detail: string,
    instance: string
}

type UnprocessableEntityResponseBody = ErrorResponseBody & {
    fieldErrors: {
        [field: string]: string
    }
}

export class ApiError extends Error {
    fieldErrors?: UnprocessableEntityResponseBody["fieldErrors"]

    constructor(message: string, fieldErrors?: UnprocessableEntityResponseBody["fieldErrors"]) {
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

async function customFetch(input: RequestInfo | URL, init?: RequestInit): Promise<any | void> {
    let res;
    try {
        if(init && init.method !== "GET") {
            init.headers = {
                ...init.headers,
                "X-XSRF-TOKEN": getCSRFCookie()!
            }
        }
        res = await fetch(input, init);
    } catch {
        throw new ApiError("Could not connect to the API server");
    }

    if (res.ok) {
        return res.json().catch(() => {});
    } else if (res.status === 422) {
        const errorBody = await res.json() as UnprocessableEntityResponseBody;
        throw new ApiError(`Server responded with: ${errorBody.title}`, errorBody.fieldErrors);
    } else {
        const errorBody = await res.json();
        const message = `Server responded with: ${isErrorResponseBody(errorBody) ? errorBody.title : "Generic error"}`;
        throw new ApiError(message);
    }
}

export function getProfessionalFromCurrentUser(): Promise<Professional> {
    return customFetch("/crm/API/professionals/user/me");
}

export function getContactFromCurrentUser(): Promise<Contact> {
    return customFetch("/crm/API/contacts/user/me");
}

export function updateProfessionalSkills(professionalId: number, skills: UserSkill[]): Promise<Professional> {
    return customFetch(`/crm/API/professionals/${professionalId}/skills`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ skills })
    });
}

export function bindContactToCustomer(contactId: number): Promise<Customer> {
    return customFetch(`/crm/API/contacts/${contactId}/customer`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: "{}"
    });
}

export function bindContactToProfessional(professionalId: number, professionalInfo: CreateProfessionalReduced): Promise<Professional> {
    return customFetch(`/crm/API/contacts/${professionalId}/professional`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(professionalInfo)
    });
}

export function updateContactField(contactId: number, field: "name" | "surname" | "ssn", value: string): Promise<Contact> {
    return customFetch(`/crm/API/contacts/${contactId}/${field}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ [field]: value })
    })
}

export function addAddressToContact(contactId: number, address: Address): Promise<Contact> {
    const { id, ...addressData } = address;
    return customFetch(`/crm/API/contacts/${contactId}/${getAddressType(address)!.toLowerCase()}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(addressData)
    })
}

export function editContactAddress(contactId: number, address: Address): Promise<Contact> {
    const { id: addressId, ...addressData } = address;
    return customFetch(`/crm/API/contacts/${contactId}/${getAddressType(address)!.toLowerCase()}/${addressId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(addressData)
    })
}

export function removeAddressFromContact(contactId: number, address: Address): Promise<void> {
    return customFetch(`/crm/API/contacts/${contactId}/${getAddressType(address)!.toLowerCase()}/${address.id}`, {
        method: "DELETE"
    })
}

export function updateProfessionalDailyRate(professionalId: number, dailyRate: number): Promise<Professional> {
    return customFetch(`/crm/API/professionals/${professionalId}/dailyRate`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ dailyRate })
    })
}
