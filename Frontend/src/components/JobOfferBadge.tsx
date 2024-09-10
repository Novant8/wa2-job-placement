import { Badge } from "react-bootstrap";

export default function JobOfferBadge(props) {
  let status = props.status;
  //"CREATED" | "SELECTION_PHASE" | "CANDIDATE_PROPOSAL" | "CONSOLIDATED" | "DONE" | "ABORTED"
  return (
    <>
      {status == "CREATED" ? (
        <Badge pill bg="primary">
          CREATED
        </Badge>
      ) : status == "CANDIDATE_PROPOSAL" ? (
        <Badge pill bg="warning">
          CANDIDATE_PROPOSAL
        </Badge>
      ) : status == "CONSOLIDATED" ? (
        <Badge pill bg="info">
          CONSOLIDATED
        </Badge>
      ) : status == "DONE" ? (
        <Badge pill bg="success">
          DONE
        </Badge>
      ) : status == "ABORTED" ? (
        <Badge pill bg="dark">
          ABORTED
        </Badge>
      ) : (
        <div>Error status</div>
      )}
    </>
  );
}
