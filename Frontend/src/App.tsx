import {useEffect, useState} from 'react'

import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import {Button,Navbar,Nav,Container,Card,Row,Col}from "react-bootstrap";

import { CiLogin,CiUser ,CiLogout} from "react-icons/ci";
export interface MeInterface{
    name:string,
    surname:string,
    loginUrl : string,
    logoutUrl: string,
    principal : any |null,
    xsrfToken:string,
    role: any | null,
    email : string
}
function App() {

    const[me,setMe]= useState<MeInterface| null>(null)

    useEffect(()=>{
        const fetchMe = async ()=>{
            try {
                const res = await  fetch("/me")
                const me = await res.json() as MeInterface
                setMe(me)
            }catch(error){
                setMe(null)
            }


        }
        fetchMe().then()
    },[])
    return (
        <>
            <Navbar expand="lg" bg="primary" fixed="top">

                <Navbar.Brand href="#home" className="text-white">TEMPORARY JOB PLACEMENT - 07</Navbar.Brand>
                <Nav className="me-auto">
                    <Nav.Link href="#home">CRM</Nav.Link>
                    <Nav.Link href="#features">Document Store</Nav.Link>
                    <Nav.Link href="#pricing">Send Email</Nav.Link >
                </Nav>
                {me && me.principal &&
                    <Nav>
                        <form method={"post"} action={me.logoutUrl} style={{ display: "flex",flexDirection:"row"}}>
                            <Nav.Item className="text-white"  style={{marginRight:"0.5em", marginTop: "10px"}}> <CiUser  size={30}/> Welcome <b> {me?.name}</b> </Nav.Item>
                            <input type={"hidden"} name={"_csrf"} value={me.xsrfToken}/>
                            <Nav.Item> <Button className="mx-1" type={"submit"} variant={"info"}><CiLogout size={30}/> Logout </Button> </Nav.Item>

                        </form>
                    </Nav>
                }

                {me && me.principal==null && me.loginUrl &&
                    <Nav>
                         <Nav.Item> <Button className="mx-1"  variant={"info"} onClick={()=> window.location.href=me?.loginUrl}><CiLogin size={30}/> Login </Button> </Nav.Item>
                    </Nav>
                }

            </Navbar>


            <Card style={{ width: '23rem' }}>
                <Card.Body>
                    {me && me.principal != null ? <Card.Title><CiUser  size={30}/>{me?.name} {me?.surname}</Card.Title>
                    : <Card.Title><CiUser  size={30}/>Anonymous User</Card.Title>}
                    {me && me.principal != null ?
                        <>
                        <Card.Text>
                            Role: {me?.role.crmclient.roles}
                        </Card.Text>
                        <Card.Text>
                            Email: {me?.email}
                        </Card.Text>
                        </>
                        :
                    <Card.Text>
                      Login to display User's information
                    </Card.Text>
                    }


                </Card.Body>

            </Card>







        </>
    )
}

export default App
