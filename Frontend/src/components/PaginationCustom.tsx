import { Pagination } from "react-bootstrap";

export default function PaginationCustom(props) {
  return (
    <>
      {props.totalPage < props.page ? (
        <></>
      ) : (
        <Pagination
          className={" d-flex justify-content-center align-items-center"}
        >
          <Pagination.First onClick={() => props.setPage(1)} />
          <Pagination.Prev
            onClick={() => {
              if (props.page - 1 >= 1) {
                props.setPage(props.page - 1);
              }
            }}
          />
          <Pagination.Item>{`Page ${props.page} of ${props.totalPage}`}</Pagination.Item>
          <Pagination.Next
            onClick={() => {
              if (props.page + 1 <= props.totalPage) {
                props.setPage(props.page + 1);
              }
            }}
          />
          <Pagination.Last onClick={() => props.setPage(props.totalPage)} />
        </Pagination>
      )}
    </>
  );
}
