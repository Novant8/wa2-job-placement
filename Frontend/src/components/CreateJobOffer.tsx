import React, { useState } from 'react';
import {Form, Button, Row, Col} from 'react-bootstrap';
import API from "../../API.tsx";
import {useAuth} from "../contexts/auth.tsx";

export default function CreateJobOffer(){
    const [description, setDescription] = useState('');
    const [requiredSkills, setRequiredSkills] = useState<string[]>([]);
    const [duration, setDuration] = useState<number | ''>('');
    const [notes, setNotes] = useState('');
    const { me } = useAuth()
    const handleSkillChange = (index: number, value: string) => {
        const newSkills = [...requiredSkills];
        newSkills[index] = value;
        setRequiredSkills(newSkills);
    };

    const handleAddSkill = () => {
        setRequiredSkills([...requiredSkills, '']);
    };

    const handleRemoveSkill = (index: number) => {
        const newSkills = requiredSkills.filter((_, i) => i !== index);
        setRequiredSkills(newSkills);
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const jobOffer = {
            description,
            requiredSkills,
            duration: typeof duration === 'number' ? duration : parseInt(duration),
            notes: notes || null,
        };
        const token = me?.xsrfToken
        API.addJobOffer(jobOffer,token).then((id =>{
            console.log('Job Offer Created:', id);
        })).catch((err)=>{
            console.log(err)
        })

    };

    return (
        <div className="mx-auto">
            <h1>Create Job Offer</h1>
            <Form onSubmit={handleSubmit}>
                <Form.Group controlId="jobDescription" className="mb-1">
                    <Form.Label>Job Description</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Enter job description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        required
                    />
                </Form.Group>
                <Form.Group controlId="requiredSkills" className="mb-3 mt-2">
                    <Form.Label className="me-3">Required Skills</Form.Label>
                    {requiredSkills.map((skill, index) => (
                        <Row key={index} className="mb-">
                            <Col>
                                <Form.Control
                                    type="text"
                                    placeholder="Enter a required skill"
                                    value={skill}
                                    onChange={(e) => handleSkillChange(index, e.target.value)}
                                    required
                                />
                            </Col>
                            <Col xs="auto">
                                <Button variant="danger" onClick={() => handleRemoveSkill(index)}>
                                    Remove
                                </Button>
                            </Col>
                        </Row>
                    ))}
                    <Button variant="outline-info" className="mt-2" onClick={handleAddSkill}>
                        Add Skill
                    </Button>
                </Form.Group>
                <Form.Group controlId="jobDuration"  className="mb-3">
                    <Form.Label>Duration (in month)</Form.Label>
                    <Form.Control
                        type="number"
                        placeholder="Enter job duration"
                        value={duration}
                        onChange={(e) => setDuration(e.target.value === '' ? '' : parseInt(e.target.value))}
                        required
                        min={1}
                    />
                </Form.Group>
                <Form.Group controlId="jobNotes" className="mb-3">
                    <Form.Label>Notes</Form.Label>
                    <Form.Control
                        as="textarea"
                        rows={3}
                        placeholder="Enter any additional notes"
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                    />
                </Form.Group>
                <Button variant="primary" type="submit">
                    Create
                </Button>
            </Form>
        </div>
    );
};


