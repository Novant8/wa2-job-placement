import /*React,*/ {useEffect, useState} from "react";
import API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";
import {Accordion, Container,InputGroup,Form} from "react-bootstrap";
import * as Icon from 'react-bootstrap-icons';
import {Professional} from "../types/professional.ts";
import TagsInput from 'react-tagsinput';
import 'react-tagsinput/react-tagsinput.css';
import "../styles/CandidateManagement.css"


export type ProfessionalAccordionProps = {
    prof: Professional

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
                    {/*TODO: mostrare gli addresses della contact info ???*/}
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

    const [professional, setProfessional]=useState({});


    const [location, setLocation] = useState("");
    //const [skills, setSkills] = useState("");
    const [skills, setSkills] = useState<string[]>([]); // Array di skill
    const [employmentState, setEmploymentState] = useState("");




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
        const token = me?.xsrfToken;

        const filterDTO = {
            location: location,
            skills: skills,
            employmentState: employmentState
        };

        API.getProfessionals(token, filterDTO).then((prof => {
            setProfessional(prof);
        })).catch((err) => {
            console.log(err);
        });
    }, [location, skills, employmentState]);

    //TODO: remove this useEffect
    useEffect(() => {
        console.log(professional);
        console.log(me);
        //console.log(candidates.content);
    }, [professional]);

    return (
        <>
            <h1>Candidate Management</h1>
            <Container>
                <InputGroup className="mb-3">
                    <InputGroup.Text id="basic-addon1"><Icon.Geo/> Location</InputGroup.Text>
                    <Form.Control
                        placeholder="Search Location"
                        aria-label="Search Professional"
                        aria-describedby="basic-addon1"
                        value={location}
                        name="location"
                        onChange={(e)=> setLocation(e.target.value)}
                    />
                </InputGroup>


                <InputGroup className="mb-3">
                    <InputGroup.Text id="basic-addon1"><Icon.ListColumnsReverse/> Skills</InputGroup.Text>

                    <div style={{flex: 1}}>
                        <TagsInput
                            value={skills}
                            onChange={(tags: any) => setSkills(tags)}
                            inputProps={{placeholder: "Add a skill"}}
                            className="tags-input"
                        />
                    </div>
                </InputGroup>

                <InputGroup className="mb-3">
                    <Form.Select
                        aria-label="Search Employment State"
                        value={employmentState}
                        name="employment"
                        onChange={(e) => setEmploymentState(e.target.value)}
                    >
                        <option value="">Select Employment State</option> {/* Opzione di default */}
                        <option value="UNEMPLOYED">UNEMPLOYED</option>
                        <option value="EMPLOYED">EMPLOYED</option>
                    </Form.Select>
        </InputGroup>

                {professional?.content?.length>0 ?
                    <Accordion>
                        {/*candidates && candidates.content.map((e) =>
                            <ProfessionalAccordion key={e.id} prof={e}/>*/
                            professional && professional.content?.map((e)=>
                                    <ProfessionalAccordion key={e.id} prof={e}/>

                                /* candidates && filteredCandidates.map((e) =>
                                 <ProfessionalAccordion key={e.id} prof={e}/>*/
                            )}

                    </Accordion>
                    : <div>There no candidates matching the filters</div>}

            </Container>

        </>
    );
};