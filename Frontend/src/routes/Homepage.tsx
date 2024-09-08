import { Card } from "react-bootstrap";
import { CiUser } from "react-icons/ci";
import { useAuth } from "../contexts/auth.tsx";
import "./Homepage.css";
import HomePageNotLoggedIn from "../components/HomePageNotLoggedIn.tsx";
import HomePageLoggedIn from "../components/HomePageLoggedIn.tsx";
import TopNavbar from "../components/TopNavbar.tsx";
import Aside from "../components/Aside.tsx";

export default function Homepage() {
  const { me } = useAuth();

  return (
    <>
      {me?.principal == null ? <HomePageNotLoggedIn /> : <HomePageLoggedIn />}
    </>
  );
}
