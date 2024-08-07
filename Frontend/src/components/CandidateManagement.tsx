import /*React,*/ {useEffect, useState} from "react";
import API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";
import {Accordion, Container,InputGroup,Form} from "react-bootstrap";
import * as Icon from 'react-bootstrap-icons';
import {Professional} from "../types/professional.ts";

//TODO: modify the filter system in order to use the API and not the filtering on the frontend

export type ProfessionalAccordionProps = {
    prof: Professional/*{

        "id":number,
        "contactInfo":
            {"id":number,"name":string,"surname":string,"category":string},
        "location":string,
        "skills": string[],
        "employmentState":string,
        notes? :string
    }*/

};

function ProfessionalAccordion(props: ProfessionalAccordionProps) {
    return (
        <div>
            <Accordion.Item eventKey={props.prof?.id?.toString()}>
                <Accordion.Header>
                    {props.prof.contactInfo?.name} {props.prof.contactInfo?.surname}
                </Accordion.Header>
                <Accordion.Body>
                    <div>
                        Category: {props.prof.contactInfo.category}
                    </div>
                    {props.prof.contactInfo.ssn?
                        <div>
                            SSN: {props.prof.contactInfo.ssn}
                        </div>
                    :""}
                    {/*TODO: mostrare gli addresses della contact info*/}
                    <div>
                        Location: {props.prof.location}
                    </div>
                    <div>
                        Skills: {props.prof.skills.map((e, index) =>
                        index == props.prof.skills.length-1 ?
                            props.prof.skills.length>2?
                                <span key={index}>,{e}</span>
                            :
                                <span key={index}>{e}</span>
                        :
                            <span key={index}>{e}, </span>
                    )
                    }
                    </div>
                    <div>
                        Employment State:{props.prof.employedState}
                    </div>
                    {props.prof.notes?
                    <div>Notes: {props.prof.notes}</div>
                    :""
                    }
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
    //console.log(me);
    const [professional, setProfessional]=useState({});
    const [searchTerm, setSearchTerm] = useState("");

    const handleSearchChange = (event:any) => {
        setSearchTerm(event.target.value);
    };


    /*const filteredCandidates = candidates.content.filter(candidate => {
        const nameMatch = candidate.contactInfo.name.toLowerCase().includes(searchTerm.toLowerCase());
        const surnameMatch = candidate.contactInfo.surname.toLowerCase().includes(searchTerm.toLowerCase());
        const locationMatch = candidate.location.toLowerCase().includes(searchTerm.toLowerCase());
        const skillsMatch = candidate.skills.some(skill => skill.toLowerCase().includes(searchTerm.toLowerCase()));
        return nameMatch || surnameMatch || locationMatch || skillsMatch;
    });*/

    useEffect(() => {
        const token = me?.xsrfToken
        API.getProfessionals(token).then((prof=>{
            //console.log(prof);
            setProfessional(prof);
        })).catch((err)=>{
            console.log(err)
        })
    }, []);

    //TODO: remove this useEffect
    useEffect(() => {
        console.log(professional);
        //console.log(candidates.content);
    }, [professional]);
    return (
        <>
            <h1>Candidate Management</h1>
            <Container>
                <InputGroup className="mb-3">
                    <InputGroup.Text id="basic-addon1"><Icon.Search/></InputGroup.Text>
                    <Form.Control
                        placeholder="Search Professional"
                        aria-label="Search Professional"
                        aria-describedby="basic-addon1"
                        value={searchTerm}
                        onChange={handleSearchChange}
                    />
                </InputGroup>
                <Accordion>
                    {/*candidates && candidates.content.map((e) =>
                            <ProfessionalAccordion key={e.id} prof={e}/>*/
                        professional && professional.content?.map((e)=>
                            <ProfessionalAccordion key={e.id} prof={e}/>

                       /* candidates && filteredCandidates.map((e) =>
                        <ProfessionalAccordion key={e.id} prof={e}/>*/
                    )}

                </Accordion>
            </Container>

        </>
    );
};