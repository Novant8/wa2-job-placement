import { Badge } from "react-bootstrap";

export default function EmploymentBadge(props) {
  let status = props.status;
  //"UNEMPLOYED" | "EMPLOYED"
  return (
    <>
      {status == "EMPLOYED" ? (
        <Badge pill bg="success">
          Employed
        </Badge>
      ) : status == "UNEMPLOYED" ? (
        <Badge pill bg="info">
          Unemployed
        </Badge>
      ) : (
        <>Error </>
      )}
    </>
  );
}
