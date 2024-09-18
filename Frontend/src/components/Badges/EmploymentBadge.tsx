import { Badge } from "react-bootstrap";

export type EmploymentBadgeProps = {
  status: string;
};

export default function EmploymentBadge(props: EmploymentBadgeProps) {
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
