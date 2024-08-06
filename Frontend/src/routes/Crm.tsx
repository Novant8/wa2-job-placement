import { Container, Row, Col } from 'react-bootstrap';
import {useState} from "react";
import Aside from "../components/Aside.tsx";
import AsideContent from "../components/AsideContent.tsx";
enum SelectedItem {
    ViewJobOffers = 'ViewJobOffers',
    CreateJobOffer = 'CreateJobOffer',
    CandidateManagement = 'CandidateManagement'
}

export default function Crm(){
    const [selectedItem, setSelectedItem] = useState<SelectedItem | null>(null);

    const handleSelect = (item: SelectedItem) => {
        setSelectedItem(item);
    };

    return (
        <Container fluid>
            <Row >
                <Col xs={3} className="bg-light p-3 fixed-left" style={{width: "250px", position: "fixed", top : 0 , bottom: 0 , left : 0  }}>
                    <Aside onSelect={handleSelect} />
                </Col>
                <Col xs >
                    <AsideContent selectedItem={selectedItem} />
                </Col>
            </Row>
        </Container>
    );


}