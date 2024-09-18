import { Accordion, Pagination } from "react-bootstrap";

import AddressBookRow from "./AddressBookRow.tsx";

export default function AddressBook({
  header,
  emails,
  setPage,
  page,
  totalPage,
  handleAddEmailAddrWithAddress,
}) {
  return (
    <Accordion>
      <Accordion.Header>{header}</Accordion.Header>
      <Accordion.Body>
        {emails.length > 0 ? (
          emails.map((item) => (
            <AddressBookRow
              item={item}
              handleAddEmailAddrWithAddress={handleAddEmailAddrWithAddress}
            />
          ))
        ) : (
          <p>No items to display</p>
        )}
        <Pagination
          className={" d-flex justify-content-center align-items-center"}
        >
          <Pagination.First onClick={() => setPage(1)} />
          <Pagination.Prev
            onClick={() => {
              if (page - 1 >= 1) {
                setPage(page - 1);
              }
            }}
          />
          <Pagination.Item>{`Page ${page} of ${totalPage}`}</Pagination.Item>
          <Pagination.Next
            onClick={() => {
              if (page + 1 <= totalPage) {
                setPage(page + 1);
              }
            }}
          />
          <Pagination.Last onClick={() => setPage(totalPage)} />
        </Pagination>
      </Accordion.Body>
    </Accordion>
  );
}
