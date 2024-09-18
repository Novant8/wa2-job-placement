import {ChangeEvent, FormEvent, ReactElement, useEffect, useState} from "react";
import {Form, FormControlProps, InputGroup, Spinner} from "react-bootstrap";
import {MdCheck, MdClose, MdDelete, MdEdit} from "react-icons/md";

export interface EditableFieldProps<N extends string, V extends FormControlProps["value"]> {
    name: N;
    type?: string;
    label?: string | ReactElement;
    initValue: V;
    initEdit?: boolean;
    showDelete?: boolean;
    loading: boolean;
    error?: string;
    validate?: (value: V) => boolean;
    onEdit?: (name: N, value: V) => void;
    onDelete?: (name: string) => void;
    onCancel?: (name: string) => void;
}

export default function EditableField<N extends string, T extends FormControlProps["value"]>({ name, type, label, initValue, initEdit, showDelete, loading, error: initError, validate, onEdit, onDelete, onCancel }: EditableFieldProps<N,T>) {
    const [ value, setValue ] = useState(initValue)
    const [ editing, setEditing ]   = useState(!!initEdit)
    const [ error, setError ] = useState(initError || "")

    useEffect(() => {
        setValue(initValue);
    }, [ initValue ])

    useEffect(() => {
        setError(initError || "");
        if(initError && initError.length > 0)
            setEditing(true);
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
                <InputGroup hasValidation>
                    {
                        label &&
                        <InputGroup.Text>{label}</InputGroup.Text>
                    }
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
                    <Form.Control.Feedback type="invalid">{error}</Form.Control.Feedback>
                </InputGroup>
            </Form.Group>
        </Form>
    )
}