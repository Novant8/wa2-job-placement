export interface Address {
    id?: string
}

export interface EmailAddress extends Address {
    email: string
}

export interface PhoneAddress extends Address {
    phoneNumber: string
}

export interface DwellingAddress extends Address {
    street: string;
    city: string;
    district: string;
    country: string;
}

export type AddressType = "EMAIL" | "TELEPHONE" | "ADDRESS"

export function getAddressType(address: Address): AddressType | undefined {
    if(isEmailAddress(address))
        return "EMAIL"
    if(isPhoneAddress(address))
        return "TELEPHONE"
    if(isDwellingAddress(address))
        return "ADDRESS"
}

export function isEmailAddress(address: Address): address is EmailAddress {
    return address.hasOwnProperty("email");
}

export function isPhoneAddress(address: Address): address is PhoneAddress {
    return address.hasOwnProperty("phoneNumber");
}

export function isDwellingAddress(address: Address): address is DwellingAddress {
    return ["street", "city", "district", "country"].every(prop => address.hasOwnProperty(prop));
}