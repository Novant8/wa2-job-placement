import { useState } from 'react'

import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import {Button,Navbar,Nav,Container,Card,Row,Col}from "react-bootstrap";

import { CiLogin,CiUser } from "react-icons/ci";
function App() {


    return (
        <>
            <Navbar expand="lg" bg="primary" fixed="top">

                <Navbar.Brand href="#home" className="text-white">TEMPORARY JOB PLACEMENT - 07</Navbar.Brand>
                <Nav className="me-auto">
                    <Nav.Link href="#home">CRM</Nav.Link>
                    <Nav.Link href="#features">Document Store</Nav.Link>
                    <Nav.Link href="#pricing">Send Email</Nav.Link>
                </Nav>

                <Nav.Item className="text-white" > <CiUser  size={30}/> Welcome <b> User</b> </Nav.Item>
                <Nav.Item> <Button className="mx-1" variant={"info"}><CiLogin size={30}/> Login </Button> </Nav.Item>

            </Navbar>


            <Card style={{ width: '18rem' }}>
                <Card.Body>
                    <Card.Title><CiUser  size={30}/>User</Card.Title>
                    <Card.Text>
                        Role: Anonymus User
                    </Card.Text>
                    <Card.Text>
                        Some quick example text to build on the card title and make up the
                        bulk of the card's content.
                    </Card.Text>

                </Card.Body>

            </Card>







        </>
    )
}

export default App
