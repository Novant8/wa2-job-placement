import  {useEffect, useState} from "react";

import * as API from "../../API.tsx";
//import {useAuth} from "../contexts/auth.tsx";
import {Accordion, Container, InputGroup, Form, Row, Col, Alert, Button} from "react-bootstrap";
import * as Icon from 'react-bootstrap-icons';
import {Customer} from "../types/customer.ts";
import { isEmailAddress, isPhoneAddress} from "../types/address.ts";
import {useNavigate} from "react-router-dom";

export type CustomerAccordionProps = {
    cust: Customer

};

function CustomerAccordion(props: CustomerAccordionProps) {
    const navigate = useNavigate();
    const [ formError, setFormError ] = useState("");
    const[customer, setCustomer] = useState<Customer>({
        id: 0,
        contactInfo: {
            id: 0 ,
            name: "",
            surname: "",
            ssn: "",
            category: "UNKNOWN",
            addresses: []
        }

    });

    useEffect(() => {
        API.getCustomerById(props.cust.id)
            .then(customer => setCustomer(customer))
            .catch(err => setFormError(err.message))
    }, []);

    if(formError) {
        return <Alert variant="danger"><strong>Error:</strong> {formError}</Alert>
    }
    return (

        <div>
            <Accordion.Item eventKey={props.cust?.id?.toString()}>
                <Accordion.Header>
                    {customer.contactInfo?.name} {customer.contactInfo?.surname}
                </Accordion.Header>
                <Accordion.Body>

                    {props.cust.contactInfo.ssn?
                        <div>
                            SSN: {customer.contactInfo.ssn}
                        </div>
                        :""}

                    <div>
                        Notes: {customer.notes? props.cust.notes : "No notes"}
                    </div>

                    {customer.contactInfo?.addresses.map(address => {
                        if (isEmailAddress(address)) {
                            return (
                                <div key={address.id}>
                                    Email: {address.email}
                                </div>
                            );
                        } else if (isPhoneAddress(address)){
                            return (
                                <div key={address.id}>
                                    Telephone: {address.phoneNumber}
                                </div>
                            );
                        }
                    })}

                    <Button className="primary mt-3" onClick={() => navigate(`customers/${customer.id}`)}> View Details </Button>
                </Accordion.Body>



            </Accordion.Item>
        </div>
    )
}

export  default  function CandidateManagement(){



    //const {me} = useAuth()

    const [customers, setCustomers]=useState({});
    const [fullName , setFullName ] = useState("");
    const [email , setEmail ] = useState("");
    const [telephone , setTelephone ] = useState("");
    const [address , setAddress ] = useState("");


    useEffect(() => {
        const filter = {
            fullName: fullName,
            email:email,
            telephone: telephone,
            address:address
        }
        API.getCustomers(filter).then((customer => {
            setCustomers(customer);
        })).catch((err) => {
            console.log(err);
        });
    }, [fullName,address,email,telephone]);

    //TODO: remove this useEffect
    useEffect(() => {
        console.log(customers);
        //console.log(candidates.content);
    }, [customers]);

    return (
        <>
            <h1 className="mb-5">Customers</h1>
            <Container>
                <Row>
                    <Col>
                        <InputGroup className="mb-3">
                            <InputGroup.Text id="basic-addon1"><Icon.PersonVcardFill className="mx-1"/> Full Name</InputGroup.Text>
                            <Form.Control
                                placeholder="Search Full Name"
                                aria-label="Search Customer"
                                aria-describedby="basic-addon1"
                                value={fullName}
                                name="location"
                                onChange={(e)=> setFullName(e.target.value)}
                            />
                        </InputGroup>
                    </Col>

                    <Col>
                        <InputGroup className="mb-3">
                            <InputGroup.Text id="basic-addon1"><Icon.House className="mx-1"/> Address</InputGroup.Text>
                            <Form.Control
                                placeholder="Search Address"
                                aria-label="Search Customer"
                                aria-describedby="basic-addon1"
                                value={address}
                                name="location"
                                onChange={(e)=> setAddress(e.target.value)}
                            />
                        </InputGroup>
                    </Col>

                </Row>

                <Row>
                    <Col>
                        <InputGroup className="mb-3">
                            <InputGroup.Text id="basic-addon1"><Icon.Envelope className="mx-1"/> Email</InputGroup.Text>
                            <Form.Control
                                placeholder="Search Email"
                                aria-label="Search Customer"
                                aria-describedby="basic-addon1"
                                value={email}
                                name="location"
                                onChange={(e)=> setEmail(e.target.value)}
                            />
                        </InputGroup>
                    </Col>

                    <Col>
                        <InputGroup className="mb-3">
                            <InputGroup.Text id="basic-addon1"><Icon.Telephone className="mx-1"/> Telephone</InputGroup.Text>
                            <Form.Control
                                placeholder="Search Telephone"
                                aria-label="Search Customer"
                                aria-describedby="basic-addon1"
                                value={telephone}
                                name="location"
                                onChange={(e)=> setTelephone(e.target.value)}
                            />
                        </InputGroup>
                    </Col>
                </Row>



                {customers?.content?.length>0 ?
                    <Accordion>
                        {/*candidates && candidates.content.map((e) =>
                            <ProfessionalAccordion key={e.id} prof={e}/>*/
                            customers && customers.content?.map((e)=>
                                    <CustomerAccordion key={e.id} cust={e}/>

                                /* candidates && filteredCandidates.map((e) =>
                                 <ProfessionalAccordion key={e.id} prof={e}/>*/
                            )}

                    </Accordion>
                    : <div>There no candidates matching the filters</div>}



            </Container>


        </>
    );

};
