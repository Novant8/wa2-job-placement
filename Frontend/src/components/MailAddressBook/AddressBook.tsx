import { Accordion, Pagination } from "react-bootstrap";

import AddressBookRow from "./AddressBookRow.tsx";
import { Professional } from "../../types/professional.ts";
import { JobOffer } from "../../types/JobOffer.ts";

import { emailContacts } from "../../types/emailContacts.ts";

export type AddressBookProps = {
  header: string;
  emails: emailContacts[];
  prof: Professional;
  jobOffer: JobOffer;
  page: number;
  totalPage: number;
  setPage: (page: number) => void;
  handleAddEmailAddrWithAddress: (email: string) => void;
};

export default function AddressBook(props: AddressBookProps) {
  return (
    <Accordion>
      <Accordion.Header>{props.header}</Accordion.Header>
      <Accordion.Body>
        {props.emails.length > 0 ? (
          props.emails.map((item) => (
            <AddressBookRow
              item={item}
              handleAddEmailAddrWithAddress={
                props.handleAddEmailAddrWithAddress
              }
            />
          ))
        ) : (
          <p>No items to display</p>
        )}
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
      </Accordion.Body>
    </Accordion>
  );
}
