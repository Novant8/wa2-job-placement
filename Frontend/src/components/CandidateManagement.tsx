import /*React,*/ {useEffect, useState} from "react";
import API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";
import {Accordion, Container} from "react-bootstrap";


export type ProfessionalAccordionProps = {
    prof: {

        "id":number,
        "contactInfo":
            {"id":number,"name":string,"surname":string,"category":string},
        "location":string,
        "skills": string[],
        "employmentState":string}

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
                        Employment State:{props.prof.employmentState}
                    </div>
                </Accordion.Body>

            </Accordion.Item>
        </div>
    )
}
export  default  function CandidateManagement(){




    const[candidates,setCandidates] = useState( {"content":
            [
                {
                    "id":1,
                    "contactInfo":{"id":1,"name":"Luigi","surname":"Verdi","category":"PROFESSIONAL"},
                    "location":"Torino",
                    "skills": ["Proficient in Kotlin","Can work well in a team"],
                    "employmentState":"UNEMPLOYED"
                },
                {
                    "id":2,
                    "contactInfo":{"id":2,"name":"Mario","surname":"Rossi","category":"PROFESSIONAL"},
                    "location":"Milano",
                    "skills": ["Proficient in Java","Can work well in a team","Agile"],
                    "employmentState":"EMPLOYED"
                }
            ],
        "pageable":"INSTANCE","totalPages":1,
        "totalElements":2,
        "last":true,
        "size":2,
        "number":0,
        "sort":{"empty":true,"sorted":false,"unsorted":true},
        "numberOfElements":2,
        "first":true,
        "empty":false})

    const {me} = useAuth()
    //console.log(me);
    const [professional, setProfessional]=useState({});
    useEffect(() => {
        const token = me?.xsrfToken
        API.getProfessionals(token).then((prof=>{
            //console.log(prof);
            setProfessional(prof);
        })).catch((err)=>{
            console.log(err)
        })
    }, []);

    useEffect(() => {
        console.log(professional);
        console.log(candidates.content);
    }, [professional]);
    return (
        <>
            <h1>Candidate Management</h1>
            <Container>
                <Accordion>
                    {candidates && candidates.content.map((e)=>
                    <ProfessionalAccordion key={e.id} prof={e}/>

                    )}

                </Accordion>
            </Container>

        </>
    );
};