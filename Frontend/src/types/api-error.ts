export interface ErrorResponseBody {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
}

export type UnprocessableEntityResponseBody = ErrorResponseBody & {
  fieldErrors: {
    [field: string]: string;
  };
};
