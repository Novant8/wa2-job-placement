import {Alert, Button, Col, Dropdown, DropdownButton, Form, Row, Spinner} from "react-bootstrap";
import {useEffect, useState} from "react";
import {useAuth} from "../contexts/auth.tsx";
import EditableField from "./EditableField.tsx";
import EditableFieldGroup from "./EditableFieldGroup.tsx";
import {Contact, ContactCategory} from "../types/contact.ts"
import {Professional, UserSkill} from "../types/professional.ts";
import {
    Address,
    EmailAddress,
    PhoneAddress,
    DwellingAddress,
    AddressType,
    isEmailAddress,
    isPhoneAddress,
    isDwellingAddress
} from "../types/address.ts";
import * as API from "../../API.tsx";
import {ApiError} from "../../API.tsx";

type ProfessionalUserInfo =
    Contact
    & Omit<Professional, "contact" | "notes">
    & { professional: Omit<Professional, "contactInfo"> }
type UserInfo = Contact | ProfessionalUserInfo

type EmailAddressErrors = { [field in keyof EmailAddress]: string };
type PhoneAddressErrors = { [field in keyof PhoneAddress]: string };
type DwellingAddressErrors = { [field in keyof DwellingAddress]: string };
type AddressErrors = EmailAddressErrors | PhoneAddressErrors | DwellingAddressErrors
type UserSkillErrors = string[]
type ProfessionalInfoErrors = {
    [field in keyof Omit<ProfessionalUserInfo["professional"], "id" | "notes" | "skills" | "employedState">]: string
} & {
    skills: UserSkillErrors
}
type UserInfoErrors = {
    [field in keyof Omit<UserInfo, "id" | "addresses" | "professional">]: string;
} & {
    addresses: AddressErrors[],
    professional: ProfessionalInfoErrors
}

type ProfessionalInfoLoading = {
    [field in keyof Omit<ProfessionalUserInfo["professional"], "id" | "notes" | "skills" | "employedState">]: boolean
} & {
    skills: boolean[]
}
type UserInfoLoading = {
    [field in keyof Omit<UserInfo, "id" | "addresses" | "skills" >]: boolean;
} & {
    addresses: boolean[],
    professional: ProfessionalInfoLoading
}

type SingleUpdatableField = "name" | "surname" | "ssn"

function isProfessional(userInfo: UserInfo): userInfo is ProfessionalUserInfo {
    return userInfo.hasOwnProperty("professional");
}

export default function EditAccountForm() {
    const [ loadingForm, setLoadingForm ] = useState(true);
    const [ formError, setFormError ] = useState("");

    const [ userInfo, setUserInfo ] = useState<UserInfo>({
        id: 0,
        name: "",
        surname: "",
        ssn: "",
        category: "UNKNOWN",
        addresses: []
    });
    const [ errors, setErrors ] = useState<UserInfoErrors>({
        name: "",
        surname: "",
        ssn: "",
        category: "",
        addresses: [],
        professional: {
            location: "",
            skills: [],
            dailyRate: ""
        }
    });
    const [ loadingSubmit, setLoadingSubmit ] = useState<UserInfoLoading>({
        name: false,
        surname: false,
        ssn: false,
        category: false,
        addresses: [],
        professional: {
            location: false,
            skills: [],
            dailyRate: false
        }
    })

    const { me, refreshToken } = useAuth();

    useEffect(() => {
        if(!me || userInfo.id > 0) return;

        const registeredRole = me.roles.find(role => ["customer", "professional"].includes(role));
        if(registeredRole)
            updateInfoField("category", registeredRole.toUpperCase() as ContactCategory);

        setLoadingForm(true);
        let request;
        switch(registeredRole) {
            case "professional":
                request =
                    API.getProfessionalFromCurrentUser()
                        .then(({ contactInfo, ...professional }) => {
                            setUserInfo({ ...contactInfo, professional });
                        })
                break;
            default:
                request =
                    API.getContactFromCurrentUser()
                        .then(contact => setUserInfo(contact))
                break;
        }
        request
            .catch(err => setFormError(err.message))
            .finally(() => setLoadingForm(false));
    }, [ me, userInfo.id ])

    function updateInfoField<K extends keyof UserInfo>(field: K, value: UserInfo[K]) {
        setUserInfo({ ...userInfo, [field]: value });
    }

    function addAddressField(addressType: AddressType) {
        let newAddress: Address;
        switch(addressType) {
            case "EMAIL":
                newAddress = { email: "" } as EmailAddress;
                break;
            case "TELEPHONE":
                newAddress = { phoneNumber: "" } as PhoneAddress;
                break;
            case "ADDRESS":
                newAddress = {
                    street: "",
                    city: "",
                    district: "",
                    country: ""
                } as DwellingAddress
        }
        setUserInfo({ ...userInfo, addresses: [ ...userInfo.addresses, newAddress ] });
        setErrors({ ...errors, addresses: [ ...errors.addresses, newAddress as AddressErrors ] })
    }

    function updateUserSkills(skills: UserSkill[], index: number) {
        loadingSubmit.professional.skills[index] = true;
        setLoadingSubmit({ ...loadingSubmit });
        setLoadingSubmit({ ...loadingSubmit });

        const professionalInfo = userInfo as ProfessionalUserInfo
        API.updateProfessionalSkills(professionalInfo.professional.id, skills)
            .then(({ contactInfo, ...professional }) => {
                setUserInfo({ ...contactInfo, professional });
            })
            .catch((err: ApiError) => {
                errors.professional.skills[index] = err.message;
                setErrors({ ...errors });
            })
            .finally(() => {
                loadingSubmit.professional.skills[index] = false;
                setLoadingSubmit({ ...loadingSubmit });
            });
    }

    function addUserSkill() {
        const professionalInfo = userInfo as ProfessionalUserInfo;
        setUserInfo({
            ...professionalInfo,
            professional: {
                ...professionalInfo.professional,
                skills: [ ...professionalInfo.professional.skills, "" ]
            }
        });
    }

    function handleCancelSkill(index: number) {
        const professionalInfo = userInfo as ProfessionalUserInfo;
        if(professionalInfo.professional.skills[index].length === 0) {
            professionalInfo.professional.skills.splice(index, 1);
            setUserInfo({ ...userInfo });
        }
    }

    function removeUserSkill(index: number) {
        handleCancelSkill(index);
        const professionalInfo = userInfo as ProfessionalUserInfo;
        const updatedSkills = professionalInfo.professional.skills.filter((skill, i) => skill.trim().length > 0 && i != index);
        updateUserSkills(updatedSkills, index);
    }

    function updateUserSkill(index: number, value: string) {
        const professionalInfo = userInfo as ProfessionalUserInfo;
        professionalInfo.professional.skills[index] = value;
        const updatedSkills =
            professionalInfo.professional.skills
                .map((skill, i) => i === index ? value : skill)
                .filter((skill) => skill.trim().length > 0);
        updateUserSkills(updatedSkills, index);
    }

    async function updateUserCategory(category: ContactCategory) {
        if(category == "UNKNOWN") return;
        setLoadingSubmit({ ...loadingSubmit, category: true })
        try {
            if (category === "PROFESSIONAL") {
                const {contactInfo, ...professional} = await API.bindContactToProfessional(userInfo.id, {
                    location: "",
                    skills: [],
                    dailyRate: 0
                });
                await refreshToken();
                setUserInfo({...contactInfo, professional});
            } else {
                const {contactInfo} = await API.bindContactToCustomer(userInfo.id);
                setUserInfo(contactInfo);
            }
        } catch (err: any) {
            if(err instanceof ApiError)
                setErrors({ ...errors, category: err.message });
        }
        setLoadingSubmit({ ...loadingSubmit, category: false });
    }

    function updateContactField(field: SingleUpdatableField, value: string) {
        setErrors({ ...errors, [field]: "" });
        setLoadingSubmit({ ...loadingSubmit, [field]: true });
        API.updateContactField(userInfo.id, field, value)
            .then(contact => setUserInfo({ ...userInfo, ...contact }))
            .then(() => ["name", "surname"].includes(field) ? refreshToken() : Promise.resolve())
            .catch((err: Error) => {
                if(err instanceof ApiError && err.fieldErrors)
                    setErrors({ ...errors, ...err.fieldErrors });
                else
                    setErrors({ ...errors, [field]: err.message })
            })
            .finally(() => setLoadingSubmit({ ...loadingSubmit, [field]: false }))
    }

    function addressIsEmpty(address: Address): boolean {
        if(isEmailAddress(address))
            return address.email.length === 0;
        if(isPhoneAddress(address))
            return address.phoneNumber.length === 0;
        if(isDwellingAddress(address))
            return address.street.length === 0 && address.city.length === 0 && address.district.length === 0 && address.country.length === 0;
        return true;
    }

    function handleCancelAddress(index: number) {
        if(addressIsEmpty(userInfo.addresses[index])) {
            userInfo.addresses.splice(index, 1);
            setUserInfo({ ...userInfo });
        }
    }

    async function addOrEditAddress(index: number, address: Address) {
        errors.addresses[index] = Object.fromEntries(Object.keys(address).map(field => [field, ""])) as AddressErrors;
        setErrors({...errors });
        loadingSubmit.addresses[index] = true;
        setLoadingSubmit({ ...loadingSubmit });

        try {
            const oldAddress = userInfo.addresses[index];
            const addressId = oldAddress.id;

            let contact;
            if(addressId) {
                contact = await API.editContactAddress(userInfo.id, {...address, id: addressId})
                if(isEmailAddress(oldAddress) && oldAddress.email == me?.email)
                    await refreshToken();
            } else {
                contact = await API.addAddressToContact(userInfo.id, address);
            }
            setUserInfo({ ...userInfo, ...contact });
        } catch(err) {
            if(err instanceof ApiError) {
                if(err.fieldErrors)
                    errors.addresses[index] = { ...errors.addresses[index], ...err.fieldErrors };
                else
                    errors.addresses[index] = Object.fromEntries(Object.keys(address).map(field => [field, err.message])) as AddressErrors;
                setErrors({ ...errors });
            }
        }
        loadingSubmit.addresses[index] = false;
        setLoadingSubmit({ ...loadingSubmit });
    }

    function removeAddress(index: number) {
        const address = userInfo.addresses[index];
        loadingSubmit.addresses[index] = true;
        setLoadingSubmit({ ...loadingSubmit });
        errors.addresses[index] = Object.fromEntries(Object.keys(address).map(field => [field, ""])) as AddressErrors;
        setErrors({ ...errors });

        API.removeAddressFromContact(userInfo.id, address)
            .then(() => {
                userInfo.addresses.splice(index, 1);
                setUserInfo({ ...userInfo, addresses: userInfo.addresses });
            })
            .catch((err: ApiError) => {
                errors.addresses[index] = Object.fromEntries(Object.keys(address).map(field => [field, err.message])) as AddressErrors;
                setErrors({ ...errors })
            })
            .finally(() => {
                loadingSubmit.addresses[index] = false;
                setLoadingSubmit({ ...loadingSubmit });
            })
    }

    function updateDailyRate(dailyRate: number) {
        if(!isProfessional(userInfo)) return;
        API.updateProfessionalDailyRate(userInfo.professional.id, dailyRate)
            .then(({ contactInfo, ...professional }) => {
                setUserInfo({ ...contactInfo, professional });
            })
            .catch((err: ApiError) => {
                errors.professional.dailyRate = err.fieldErrors?.dailyRate || err.message;
                setErrors({ ...errors });
            });
    }

    function firstUpper(str: string) {
        return str.slice(0,1).toUpperCase() + str.slice(1).toLowerCase();
    }

    if(loadingForm) {
        return <Spinner />
    }

    if(formError) {
        return <Alert variant="danger"><strong>Error:</strong> {formError}</Alert>
    }

    return (
        <div>
            <h2>Basic Information</h2>
            <hr/>
            {
                loadingSubmit.category ? <Spinner size="sm" /> :
                    <Form.Group controlId="register-user-userType" className="my-2">
                        <Form.Label>I'm a... <span className="text-danger">*</span></Form.Label>
                        <Form.Select
                            name="category"
                            onChange={e => updateUserCategory(e.target.value as ContactCategory)}
                            disabled={userInfo.category !== "UNKNOWN" || loadingSubmit.category}
                            value={userInfo.category}
                            isInvalid={!!errors.category}
                        >
                            {
                                userInfo.category === "UNKNOWN" ?
                                    <>
                                        <option value="UNKNOWN">Select...</option>
                                        <option value="CUSTOMER">Customer</option>
                                        <option value="PROFESSIONAL">Professional</option>
                                    </>
                                    :
                                    <option value={userInfo.category}>{firstUpper(userInfo.category)}</option>
                            }
                        </Form.Select>
                        <Form.Control.Feedback type="invalid">{errors.category}</Form.Control.Feedback>
                    </Form.Group>
            }
            <Row>
                <Col sm={6}>
                    <EditableField
                        label="Name"
                        name="name"
                        initValue={userInfo.name}
                        loading={loadingSubmit.name}
                        error={errors.name}
                        validate={value => (value).trim().length > 0}
                        onEdit={(field, val) => updateContactField(field as SingleUpdatableField, val)}
                    />
                </Col>
                <Col sm={6}>
                    <EditableField
                        label="Surname"
                        name="surname"
                        initValue={userInfo.surname}
                        loading={loadingSubmit.surname}
                        error={errors.surname}
                        validate={value => value.length > 0}
                        onEdit={(field, val) => updateContactField(field as SingleUpdatableField, val)}
                    />
                </Col>
            </Row>
            <EditableField
                label="SSN"
                name="ssn"
                initValue={userInfo.ssn || ""}
                loading={loadingSubmit.ssn as boolean}
                error={errors.ssn}
                validate={(value) => value?.length > 0}
                onEdit={(field, val) => updateContactField(field as SingleUpdatableField, val)}
            />
            <h2 className="mt-3">Contact Information</h2>
            <hr/>
            {
                userInfo.addresses.map((address, i) => (
                    <AddressFieldGroup
                        key={`address-${i}`}
                        index={i}
                        address={address}
                        errors={errors}
                        loading={loadingSubmit}
                        onEdit={addOrEditAddress}
                        onDelete={removeAddress}
                        onCancel={handleCancelAddress}
                    />
                ))
            }
            <div className="text-center my-2">
                <DropdownButton
                    title="Add new address"
                    id="add-address"
                    variant="light"
                    onSelect={(key) => addAddressField(key as AddressType)}
                >
                    <Dropdown.Item eventKey="EMAIL">Email</Dropdown.Item>
                    <Dropdown.Item eventKey="TELEPHONE">Phone Number</Dropdown.Item>
                    <Dropdown.Item eventKey="ADDRESS">Home Address</Dropdown.Item>
                </DropdownButton>
            </div>
            {
                isProfessional(userInfo) &&
                <>
                    <div className="my-2">
                        <h3>Skills</h3>
                        <hr/>
                        {
                            userInfo.professional.skills.map((skill, i) => (
                                <EditableField
                                    key={`skill-${i}`}
                                    name={`skill-${i}`}
                                    initValue={skill}
                                    initEdit={skill.trim().length === 0}
                                    loading={loadingSubmit.professional.skills[i]}
                                    onEdit={(_, value) => updateUserSkill(i, value)}
                                    showDelete
                                    onDelete={() => removeUserSkill(i)}
                                    onCancel={() => handleCancelSkill(i)}
                                />
                            ))
                        }
                        <Button variant="light" onClick={addUserSkill}>Add new skill</Button>
                    </div>
                    <h3>Employment</h3>
                    <hr/>
                    <EditableField
                        type="number"
                        name="dailyRate"
                        label="Daily Rate"
                        initValue={userInfo.professional.dailyRate}
                        loading={loadingSubmit.professional.dailyRate}
                        onEdit={(_, value) => updateDailyRate(value)}
                    />
                </>
            }
        </div>
    )
}

interface AddressFieldGroupProps {
    index: number;
    address: Address;
    loading: UserInfoLoading;
    errors: UserInfoErrors;
    onEdit?: (index: number, newAddress: Address) => void;
    onDelete?: (index: number) => void;
    onCancel?: (index: number) => void;
}

function AddressFieldGroup({ index, address, loading, errors, onEdit, onDelete, onCancel }: AddressFieldGroupProps) {

    const { me } = useAuth();

    if(isEmailAddress(address)) {
        const emailErrors = (errors.addresses[index]) as EmailAddressErrors | undefined
        return (
            <EditableFieldGroup
                groupName="email"
                title="Email Address"
                showDelete={address.email !== me?.email}
                initEdit={!address.id}
                loading={loading.addresses[index]}
                fields={[
                    {
                        name: "email",
                        label: "Email",
                        type: "text",
                        value: address.email,
                        error: emailErrors?.email
                    }
                ]}
                onEdit={(_, values) => onEdit?.(index, values as Address)}
                onDelete={() => onDelete?.(index)}
                onCancel={() => onCancel?.(index)}
            />
        )
    } else if (isPhoneAddress(address)) {
        const phoneErrors = (errors.addresses[index]) as PhoneAddressErrors | undefined
        return (
            <EditableFieldGroup
                groupName="telephone"
                title="Phone Number"
                showDelete
                initEdit={!address.id}
                loading={loading.addresses[index]}
                fields={[
                    {
                        name: "phoneNumber" ,
                        label: "Phone Number",
                        type: "text",
                        value: address.phoneNumber,
                        error: phoneErrors?.phoneNumber
                    }
                ]}
                onEdit={(_, values) => onEdit?.(index, values as Address)}
                onDelete={() => onDelete?.(index)}
                onCancel={() => onCancel?.(index)}
            />
        )
    } else if (isDwellingAddress(address)) {
        const dwellingErrors = (errors.addresses[index]) as DwellingAddressErrors | undefined
        return (
            <EditableFieldGroup
                groupName="address"
                title="Home Address"
                showDelete
                initEdit={!address.id}
                loading={loading.addresses[index]}
                fields={[
                    {
                        name: "street",
                        label: "Street",
                        type: "text",
                        value: address.street,
                        error: dwellingErrors?.street
                    },
                    {
                        name: "city",
                        label: "City",
                        type: "text",
                        value: address.city,
                        error: dwellingErrors?.city
                    },
                    {
                        name: "district",
                        label: "District",
                        type: "text",
                        value: address.district,
                        error: dwellingErrors?.district
                    },
                    {
                        name: "country",
                        label: "Country",
                        type: "text",
                        value: address.country,
                        error: dwellingErrors?.country
                    }
                ]}
                onEdit={(_, values) => onEdit?.(index, values as Address)}
                onDelete={() => onDelete?.(index)}
                onCancel={() => onCancel?.(index)}
            />
        )
    }
}