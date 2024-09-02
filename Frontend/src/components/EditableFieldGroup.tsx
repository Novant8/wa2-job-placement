import {Card, Form, FormControlProps, Spinner} from "react-bootstrap";
import {ChangeEvent, FormEvent, useEffect, useState} from "react";
import {MdCheck, MdClose, MdDelete, MdEdit} from "react-icons/md";

export interface FormFields {
    id?: number;
    name: string;
    label: string;
    type: FormControlProps["type"];
    value: FormControlProps["value"];
    error?: string;
}

export type FieldGroupValues = {
    [name: string]: FormControlProps["value"]
}

export interface EditableFieldGroupProps {
    title: string;
    groupName: string;
    showDelete?: boolean;
    initEdit?: boolean;
    loading: boolean;
    fields: FormFields[];
    onEdit?: (groupName: string, fields: FieldGroupValues) => void;
    onDelete?: (groupName: string) => void;
    onCancel?: (groupName: string) => void;
}

export default function EditableFieldGroup({ title, groupName, showDelete, initEdit, fields, loading, onEdit, onDelete, onCancel }: EditableFieldGroupProps) {
    const [ editing, setEditing ] = useState(initEdit || false);
    const [ values, setValues ] = useState(fields.map(field => field.value));

    useEffect(() => {
        setValues(fields.map(field => field.value));
    }, [ fields ]);

    function handleChange(e: ChangeEvent<HTMLInputElement>) {
        values[fields.findIndex(field => field.name == e.target.name)] = e.target.value;
        setValues([ ...values ]);
    }

    function cancelEdit() {
        setEditing(false);
        setValues(fields.map(field => field.value));
        onCancel?.(groupName);
    }

    function handleEdit(e?: FormEvent<HTMLFormElement>) {
        e?.preventDefault();
        setEditing(false);
        onEdit?.(groupName, Object.fromEntries(fields.map((field, index) => [ field.name, values[index] ])));
    }

    function handleDelete() {
        onDelete?.(groupName);
    }

    return(
        <Card className="my-2">
            <Card.Header className="d-flex justify-content-between">
                <div>
                    <strong>{title}</strong>
                </div>
                <div>
                    {
                        loading ? <Spinner size="sm" /> :
                        editing ?
                            <>
                                <MdCheck color="green" size={24} role="button" onClick={() => handleEdit()} />
                                <MdClose color="red" size={24} role="button" onClick={cancelEdit} />
                            </>
                            :
                            <>
                                {
                                    showDelete &&
                                        <MdDelete size={24} role="button" onClick={handleDelete} />
                                }
                                <MdEdit size={24} role="button" onClick={() => setEditing(true)} />
                            </>
                    }
                </div>
            </Card.Header>
            <Card.Body>
                <Form onSubmit={handleEdit}>
                    {
                        fields
                            .map((field, index) => ({ ...field, value: values[index] }))
                            .map(({ name, label, type, value, error }, index) => (
                                <Form.Group key={`field-group-${index}`} controlId={`register-user-address-${index}-country`} className="my-2">
                                    <Form.Label>{ label }</Form.Label>
                                    <Form.Control
                                        name={name}
                                        type={type}
                                        value={value}
                                        isInvalid={!!error}
                                        onChange={handleChange}
                                        disabled={loading || !editing}
                                    />
                                    <Form.Control.Feedback type="invalid">{error}</Form.Control.Feedback>
                                </Form.Group>
                            ))
                    }
                </Form>
            </Card.Body>
        </Card>
    )
}