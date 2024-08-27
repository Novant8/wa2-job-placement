import { useEffect, useState } from 'react';
import {Button, Card, Container, Form, InputGroup, Row, Spinner} from 'react-bootstrap';
import * as API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";
import {Customer} from "../types/customer.ts";
import {Contact, ContactCategory} from "../types/contact.ts";
import {Pageable} from "../types/Pageable.ts";
import {ReducedJobOffer} from "../types/JobOffer.ts";
import {useNavigate} from "react-router-dom";
import * as Icon from "react-bootstrap-icons";



export default function ViewRecruiterJobOffer () {
    const [jobOffers, setJobOffers] = useState<ReducedJobOffer[]>([]);
    const [pageable, setPageable] = useState<Pageable | null>(null);
    const [totalPages, setTotalPages] = useState<number | null>(null)
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const {me} = useAuth();
    const [professionalId, setProfessionalId] = useState("")
    const [status, setStatus] = useState("")
    const [customerId, setCustomerId] = useState("")
    const navigate = useNavigate();



    useEffect(() => {
        const token = me?.xsrfToken;

        const filterDTO = {
            professionalId: professionalId,
            status: status,
            customerId: customerId
        };
        console.log(filterDTO);

        setLoading(true);
        API.getJobOfferRecruiter(token, filterDTO).then((data => {
            console.log(data);
            setJobOffers(data.content);
            setPageable(data.pageable);
            setTotalPages(data.totalPages);
        })).catch((err) => {
            console.log(err);
            setError('Failed to fetch job offers');
        }).finally(() => setLoading(false));
    }, [professionalId, status, customerId]);

    if (loading) {
        return (
            <Container className="text-center mt-5">
            <Spinner animation="border" />
                </Container>
        );
    }

    if (error) {
        return (
            <Container className="text-center mt-5">
                <p>{error}</p>
                </Container>
        );
    }

    return (
        <Container className="mt-5">
            <InputGroup className="mb-3">
                <InputGroup.Text id="basic-addon1"><Icon.Briefcase/>  Professional </InputGroup.Text>
                <Form.Control
                    placeholder="Search Professional Id"
                    aria-label="Search Professional Id"
                    aria-describedby="basic-addon1"
                    value={professionalId}
                    name="professionalId"
                    onChange={(e)=> setProfessionalId(e.target.value)}
                />
            </InputGroup>

            <InputGroup className="mb-3">
                <InputGroup.Text id="basic-addon1"><Icon.Buildings/> Customer</InputGroup.Text>
                <Form.Control
                    placeholder="Search Customer Id"
                    aria-label="Search Customer Id"
                    aria-describedby="basic-addon1"
                    value={customerId}
                    name="customerId"
                    onChange={(e)=> setCustomerId(e.target.value)}
                />
            </InputGroup>




            <InputGroup className="mb-3">
                <Form.Select
                    aria-label="Search Offer Status"
                    value={status}
                    name="status"
                    onChange={(e) => setStatus(e.target.value)}
                >
                    <option value="">Select Offer Status</option>
                    {/* Opzione di default */}
                    <option value="CREATED">CREATED</option>
                    <option value="SELECTION_PHASE">SELECTION PHASE</option>
                    <option value="CANDIDATE_PROPOSAL">CANDIDATE PROPOSAL</option>
                    <option value="CONSOLIDATED">CONSOLIDATED</option>
                    <option value="DONE">DONE</option>
                    <option value="ABORTED">ABORTED</option>
                </Form.Select>
            </InputGroup>

            {jobOffers.map(offer => (
                    <Row key={offer.id} xs={12} className="mb-4">
                <Card>
                    <Card.Body>
                        <Card.Title>Job Offer ID: {offer.id}</Card.Title>
    <Card.Text>
    <strong>Description:</strong> {offer.description} &nbsp;
    <strong>Status:</strong> {offer.offerStatus}&nbsp;
    <strong>Professional:</strong> {offer.professional ? offer.professional : 'N/A'}
    </Card.Text>

    <Button variant="primary" onClick={()=>navigate(`jobOffer/${offer.id}`)}>
    View
    </Button>
    </Card.Body>
    </Card>
    </Row>
))}

    {pageable && (
        <div className="mt-4">
            <p>Page {pageable.pageNumber + 1} of {totalPages}</p>
    </div>
    )}
    </Container>
);
};