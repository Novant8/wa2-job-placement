import Cookies from "js-cookie";

export function getCSRFCookie(): string | undefined {
  return Cookies.get("XSRF-TOKEN");
}
