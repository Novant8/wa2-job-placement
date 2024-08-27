import React, {useEffect, useState} from "react";

import API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";
import {Accordion, Container,InputGroup,Form} from "react-bootstrap";
import * as Icon from 'react-bootstrap-icons';
import {Customer} from "../types/customer.ts";

export type ProfessionalAccordionProps = {
    cust: Customer

};

function CustomerAccordion(props: ProfessionalAccordionProps) {
    return (
        <div>
            <Accordion.Item eventKey={props.cust?.id?.toString()}>
                <Accordion.Header>
                    {props.cust.contactInfo?.name} {props.cust.contactInfo?.surname}
                </Accordion.Header>
                <Accordion.Body>
                    <div>
                        Category: {props.cust.contactInfo.category}
                    </div>
                    {props.cust.contactInfo.ssn?
                        <div>
                            SSN: {props.cust.contactInfo.ssn}
                        </div>
                        :""}

                    <div>
                        Notes: {props.cust.notes? props.cust.notes : "No notes"}
                    </div>

                </Accordion.Body>

            </Accordion.Item>
        </div>
    )
}

export  default  function CandidateManagement(){



    const {me} = useAuth()

    const [customers, setCustomers]=useState({});


    useEffect(() => {
        const token = me?.xsrfToken;



        API.getCustomers(token).then((customer => {
            setCustomers(customer);
        })).catch((err) => {
            console.log(err);
        });
    }, [location, /*skills, employmentState*/]);

    //TODO: remove this useEffect
    useEffect(() => {
        console.log(customers);
        //console.log(candidates.content);
    }, [customers]);

    return (
        <>
            <h1>Customers</h1>
            <Container>


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
