import {ChangeEvent, FormEvent, useEffect, useState} from "react";
import {Form, FormControlProps, InputGroup, Spinner} from "react-bootstrap";
import {MdCheck, MdClose, MdDelete, MdEdit} from "react-icons/md";

export interface EditableFieldProps<T extends FormControlProps["value"]> {
    name: string;
    type?: string;
    label?: string;
    initValue: T;
    initEdit?: boolean;
    showDelete?: boolean;
    loading: boolean;
    error?: string;
    validate?: (value: T) => boolean;
    onEdit?: (name: string, value: T) => void;
    onDelete?: (name: string) => void;
    onCancel?: (name: string) => void;
}

export default function EditableField<T extends FormControlProps["value"]>({ name, type, label, initValue, initEdit, showDelete, loading, error: initError, validate, onEdit, onDelete, onCancel }: EditableFieldProps<T>) {
    const [ value, setValue ] = useState(initValue)
    const [ editing, setEditing ]   = useState(!!initEdit)
    const [ error, setError ] = useState(initError || "")

    useEffect(() => {
        setValue(initValue);
    }, [ initValue ])

    useEffect(() => {
        setError(initError || "");
    }, [ initError ])

    function handleChange(e: ChangeEvent<HTMLInputElement>) {
        setError("");
        setValue(e.target.value as T)
    }

    function cancelEdit() {
        setEditing(false)
        setValue(initValue)
        onCancel?.(name);
    }

    function handleEdit(e?: FormEvent<HTMLFormElement>) {
        e?.preventDefault()
        if(!validate || validate(value)) {
            setEditing(false)
            onEdit?.(name, value)
        } else {
            setError("Invalid field");
        }
    }

    return (
        <Form onSubmit={handleEdit}>
            <Form.Group controlId="register-user-ssn" className="my-2">
                {
                    label &&
                    <Form.Label>{label}</Form.Label>
                }
                <InputGroup>
                    <Form.Control
                        type={type}
                        name="ssn"
                        value={value}
                        isInvalid={!!error}
                        onChange={handleChange}
                        disabled={!editing || loading}
                    />
                    <InputGroup.Text>
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
                                            <MdDelete size={24} role="button" onClick={() => onDelete?.(name)} />
                                        }
                                        <MdEdit size={24} role="button" onClick={() => setEditing(true)} />
                                    </>
                        }
                    </InputGroup.Text>
                </InputGroup>
                <Form.Control.Feedback type="invalid">{error}</Form.Control.Feedback>
            </Form.Group>
        </Form>
    )
}