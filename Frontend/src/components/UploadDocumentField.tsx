import { ChangeEvent, useEffect, useState } from "react";
import {
  DocumentHistory,
  ReducedDocumentMetadata,
} from "../types/documents.ts";
import * as API from "../../API.tsx";
import { ApiError } from "../../API.tsx";
import {
  Accordion,
  Button,
  ButtonGroup,
  Form,
  InputGroup,
  Spinner,
} from "react-bootstrap";
import ConfirmModal from "./ConfirmModal.tsx";

interface UploadFileFieldProps {
  documentId: number | undefined | null;
  loading?: boolean;
  error?: string;
  onUpload?: (file: File) => void;
  onDelete?: (documentId: number) => void;
  customerView: boolean;
  customerConfirm: boolean;
  professionalView: boolean;
  professionalConfirm: boolean;
}

export function UploadDocumentField({
  documentId,
  error,
  loading,
  onUpload,
  onDelete,
  customerView,
  customerConfirm,
  professionalView,
  professionalConfirm,
}: UploadFileFieldProps) {
  const [file, setFile] = useState<File | undefined>();
  const [documentHistory, setDocumentHistory] = useState<
    DocumentHistory | undefined
  >();
  const [documentError, setDocumentError] = useState("");
  const [versionDeleteLoading, setVersionDeleteLoading] = useState<boolean[]>(
    [],
  );

  useEffect(() => {
    if (!documentId) return;
    setDocumentError("");
    API.getDocumentHistory(documentId)
      .then((history) => {
        setDocumentHistory(history);
        setVersionDeleteLoading(Array(history.versions.length).fill(false));
      })
      .catch((err: ApiError) => setDocumentError(err.message));
  }, [documentId]);

  function handleFileUpload() {
    setDocumentHistory(undefined);
    setFile(undefined);
    onUpload?.(file!);
  }

  function handleHistoryDelete() {
    setDocumentHistory(undefined);
    setFile(undefined);
    onDelete?.(documentId!);
  }

  function handleVersionDelete(versionIndex: number) {
    versionDeleteLoading[versionIndex] = true;
    setVersionDeleteLoading([...versionDeleteLoading]);
    const historyId = documentHistory!.versions[versionIndex].historyId;
    const versionId = documentHistory!.versions[versionIndex].versionId;
    API.deleteDocumentVersion(historyId, versionId)
      .then(() => {
        documentHistory!.versions.splice(versionIndex, 1);
      })
      .catch((err: ApiError) => setDocumentError(err.message))
      .finally(() => {
        versionDeleteLoading[versionIndex] = false;
        setVersionDeleteLoading([...versionDeleteLoading]);
      });
  }

  const latestDoc = documentHistory?.versions?.[0];

  const showSpinner = loading || (documentId && !documentHistory);
  if (showSpinner) return <Spinner />;

  return (
    <>
      {documentId && latestDoc && (
        <>
          {!customerView && !professionalView && !customerConfirm && (
            <p>Currently uploaded: </p>
          )}

          <FileField
            document={latestDoc}
            isHistory
            onDelete={handleHistoryDelete}
            customerConfirm={customerConfirm}
            professionalConfirm={professionalConfirm}
          />
        </>
      )}
      <Form.Group controlId="register-user-upload-document" className="my-3">
        <InputGroup hasValidation>
          {((customerView && !customerConfirm) ||
            (professionalView && !professionalConfirm)) && (
            <>
              <Form.Control
                type="file"
                isInvalid={!!documentError || !!error}
                onChange={(e: ChangeEvent<HTMLInputElement>) =>
                  setFile(e.target.files![0])
                }
              />
              <Button
                variant={documentHistory ? "warning" : "primary"}
                disabled={!file}
                onClick={handleFileUpload}
              >
                {documentHistory ? "Update" : "Upload"}
              </Button>
            </>
          )}

          <Form.Control.Feedback type="invalid">
            {documentError || error}
          </Form.Control.Feedback>
        </InputGroup>
      </Form.Group>
      {documentHistory &&
        (professionalView || customerView) &&
        documentHistory.versions.length > 1 && (
          <Accordion>
            <Accordion.Item eventKey="0">
              <Accordion.Header>Show all versions</Accordion.Header>
              <Accordion.Body>
                {documentHistory.versions.map((document, index) =>
                  versionDeleteLoading[index] ? (
                    <Spinner key={`version-loading-${index}`} />
                  ) : (
                    <FileField
                      key={`version-${index}`}
                      document={document}
                      onDelete={() => handleVersionDelete(index)}
                      customerConfirm={customerConfirm}
                      professionalConfirm={professionalConfirm}
                    />
                  ),
                )}
              </Accordion.Body>
            </Accordion.Item>
          </Accordion>
        )}
    </>
  );
}

interface FileFieldProps {
  document: ReducedDocumentMetadata;
  isHistory?: boolean;
  onDelete?: (documentId: number) => void;
  customerConfirm: boolean;
  professionalConfirm: boolean;
}

function FileField({
  document,
  isHistory,
  onDelete,
  customerConfirm,
  professionalConfirm,
}: FileFieldProps) {
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  return (
    <div className="my-2">
      <span className="mx-3 my-auto">
        <strong>{document.name}</strong>
      </span>
      <ButtonGroup>
        <Button
          variant="primary"
          as="a"
          href={`/document-store/API/documents/${document.historyId}/version/${document.versionId}/data`}
          target="_blank"
        >
          View
        </Button>
        {!customerConfirm && !professionalConfirm && (
          <>
            <Button variant="danger" onClick={() => setShowConfirmModal(true)}>
              Delete
            </Button>
            <ConfirmModal
              title={`Confirm file${isHistory ? " history" : ""} deletion`}
              show={showConfirmModal}
              onConfirm={() => onDelete?.(document.historyId)}
              onCancel={() => setShowConfirmModal(false)}
            >
              Are you sure you want to delete the file {document.name}
              {isHistory && " and all of its history"}? This action cannot be
              undone.
            </ConfirmModal>
          </>
        )}
      </ButtonGroup>
    </div>
  );
}
