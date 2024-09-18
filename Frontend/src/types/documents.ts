export interface DocumentMetadata {
  historyId: number;
  versionId: number;
  size: number;
  contentType: string;
  name: string;
  creationTimestamp: string;
}

export type ReducedDocumentMetadata = Pick<
  DocumentMetadata,
  "historyId" | "versionId" | "name"
>;

export interface DocumentHistory {
  historyId: number;
  versions: ReducedDocumentMetadata[];
}
