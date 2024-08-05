import {Button, Nav, Navbar} from "react-bootstrap";
import {CiLogin, CiLogout, CiUser} from "react-icons/ci";
import {useAuth} from "../contexts/auth.tsx";



export default function TopNavbar() {
    const { me } = useAuth();

    return (
        <Navbar expand="lg" bg="primary" fixed="top">
            <Navbar.Brand href="#home" className="text-white">TEMPORARY JOB PLACEMENT - 07</Navbar.Brand>
            <Nav className="me-auto">
                <Nav.Link href="#home">CRM</Nav.Link>
                <Nav.Link href="#features">Document Store</Nav.Link>
                <Nav.Link href="#pricing">Send Email</Nav.Link >
                <Nav.Link href="/grafana">Dashboard</Nav.Link >
            </Nav>
            {me?.principal &&
                <Nav>
                    <form method={"post"} action={me.logoutUrl} style={{ display: "flex",flexDirection:"row"}}>
                        <Nav.Item className="text-white"  style={{marginRight:"0.5em", marginTop: "10px"}}> <CiUser  size={24}/> Welcome <b> {me?.name}</b> </Nav.Item>
                        <input type={"hidden"} name={"_csrf"} value={me.xsrfToken}/>
                        <Nav.Item> <Button className="mx-1" type={"submit"} variant={"info"}><CiLogout size={24}/> Logout </Button> </Nav.Item>
                    </form>
                </Nav>
            }

            {me?.principal==null && me?.loginUrl &&
                <Nav>
                    <Nav.Item> <Button className="mx-1"  variant={"info"} onClick={()=> window.location.href=me?.loginUrl}><CiLogin size={24}/> Login </Button> </Nav.Item>
                </Nav>
            }
        </Navbar>
    )
}