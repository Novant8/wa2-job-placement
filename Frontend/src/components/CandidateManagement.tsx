import /*React,*/ {useEffect, useState} from "react";
import API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";

export  default  function CandidateManagement(){

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
    }, [professional]);
    return (
        <>
            <h1>Candidate Management</h1>
        </>
    );
};