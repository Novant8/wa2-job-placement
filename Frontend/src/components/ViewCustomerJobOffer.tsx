/*import { useEffect, useState } from 'react';
import { Card, Container, Row, Spinner } from 'react-bootstrap';
import API,{Pageable,CustomerJobOffer,CustomerJobOfferResponse} from "../../API.tsx";

export default function ViewCustomerJobOffer (){
    const [jobOffers, setJobOffers] = useState<CustomerJobOffer[]>([]);
    const [pageable, setPageable] = useState<Pageable | null>(null);
    const [totalPages, setTotalPages] = useState <number | null >(null)
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchJobOffers = async () => {
            try {
                const data: CustomerJobOfferResponse = await API.getCustomerJobOffer();
                setJobOffers(data.content);
                setPageable(data.pageable);
                setTotalPages(data.totalPages);
            } catch (err) {
                setError('Failed to fetch job offers');
            } finally {
                setLoading(false);
            }
        };

        fetchJobOffers();
    }, []);

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
                                    <strong>Status:</strong> {offer.status}&nbsp;
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
};*/


