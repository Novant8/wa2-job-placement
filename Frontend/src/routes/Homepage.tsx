import {Card} from "react-bootstrap";
import {CiUser} from "react-icons/ci";
import {useAuth} from "../contexts/auth.tsx";
import './Homepage.css'

export default function Homepage() {
    const { me } = useAuth()

    return (
        <Card style={{ width: '23rem' }}>
            <Card.Body>
                {me?.principal != null ?
                    <Card.Title><CiUser  size={30}/>{me?.name} {me?.surname}</Card.Title>
                :
                    <Card.Title><CiUser  size={30}/>Anonymous User</Card.Title>}
                {me?.principal != null ?
                    <>
                        <Card.Text>
                            Roles: {me?.roles?.join(", ")}
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
    )
}