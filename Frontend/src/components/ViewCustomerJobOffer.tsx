import { useEffect, useState } from 'react';
import { Card, Container, Row, Spinner } from 'react-bootstrap';
import * as API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";
import {Customer} from "../types/customer.ts";
import {Contact, ContactCategory} from "../types/contact.ts";
import {Pageable} from "../types/Pageable.ts";
import {JobOffer} from "../types/JobOffer.ts";


export default function ViewCustomerJobOffer (){
    const [jobOffers, setJobOffers] = useState<JobOffer[]>([]);
    const [pageable, setPageable] = useState<Pageable | null>(null);
    const [totalPages, setTotalPages] = useState <number | null >(null)
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const { me } = useAuth();

    const [ userInfo, setUserInfo ] = useState<Customer>({
        id: 0,
        contactInfo: {
            id: 0 ,
            name: "",
            surname: "",
            ssn: "",
            category: "UNKNOWN",
            addresses: []
        }

    });
    function updateInfoField<K extends keyof Contact>(field: K, value: Contact[K]) {
        setUserInfo({
            ...userInfo,
            contactInfo: {
                ...userInfo.contactInfo,
                [field]: value
            }
        });
    }

    useEffect(() => {
        if(!me || userInfo.id > 0) return;

        const registeredRole = me.roles.find(role => ["customer", "professional"].includes(role));
        if(registeredRole)
            updateInfoField("category", registeredRole.toUpperCase() as ContactCategory);

        setLoading(true);
        API.getCustomerFromCurrentUser()
            .then((customer)=>{
                setUserInfo(customer)
                API.getCustomerJobOffer(customer.id).
                then((data)=>{
                    setJobOffers(data.content);
                    setPageable(data.pageable);
                    setTotalPages(data.totalPages);
                }).catch(()=>{
                    setError('Failed to fetch job offers');
                })
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [ me, userInfo.id ])

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


