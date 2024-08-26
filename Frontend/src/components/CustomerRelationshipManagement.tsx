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
                    {/*TODO: mostrare gli addresses della contact info ???*/}
                    <div>
                        Notes: {props.cust.notes? props.cust.notes : "No notes"}
                    </div>

                </Accordion.Body>

            </Accordion.Item>
        </div>
    )
}

export  default  function CandidateManagement(){





    /*const[candidates,setCandidates] = useState( {"content":
            [
                {
                    "id":1,
                    "contactInfo":{"id":1,"name":"Luigi","surname":"Verdi","category":"PROFESSIONAL",
                    "addresses":[]
                    },
                    "location":"Torino",
                    "skills": ["Proficient in Kotlin","Can work well in a team"],
                    "dailyRate" : 20,
                    "employedState":"UNEMPLOYED"
                },
                {
                    "id":2,
                    "contactInfo":{"id":2,"name":"Mario","surname":"Rossi","category":"PROFESSIONAL",
                    "addresses":[]
                    },
                    "location":"Milano",
                    "skills": ["Proficient in Java","Can work well in a team","Agile"],
                    "dailyRate" : 20,
                    "employedState":"EMPLOYED"
                },
                {
                    "id":3,
                    "contactInfo":{"id":3,"name":"Giovanni","surname":"Mariani","category":"PROFESSIONAL",
                    "addresses":[]
                    },
                    "location":"Bologna",
                    "skills": ["Proficient in Python","Can work well in a team","Agile","Mobile Application"],
                    "dailyRate" : 20,
                    "employedState":"UNEMPLOYED"
                }

            ],
        "pageable":"INSTANCE","totalPages":1,
        "totalElements":3,
        "last":true,
        "size":3,
        "number":0,
        "sort":{"empty":true,"sorted":false,"unsorted":true},
        "numberOfElements":3,
        "first":true,
        "empty":false})*/

    const {me} = useAuth()

    const [customers, setCustomers]=useState({});


    //const [location, setLocation] = useState("");
    //const [skills, setSkills] = useState("");
    //const [skills, setSkills] = useState<string[]>([]); // Array di skill
    //const [employmentState, setEmploymentState] = useState("");




    /*useEffect(() => {
        const token = me?.xsrfToken
        API.getProfessionals(token).then((prof=>{
            //console.log(prof);
            setProfessional(prof);
        })).catch((err)=>{
            console.log(err)
        })
    }, []);*/


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
            <h1>CRM</h1>
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
