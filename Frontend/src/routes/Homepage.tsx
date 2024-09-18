import { useAuth } from "../contexts/auth.tsx";
import "./Homepage.css";
import HomePageNotLoggedIn from "../components/HomePageNotLoggedIn.tsx";
import HomePageLoggedIn from "../components/HomePageLoggedIn.tsx";

export default function Homepage() {
  const { me } = useAuth();

  return (
    <>
      {me?.principal == null ? <HomePageNotLoggedIn /> : <HomePageLoggedIn />}
    </>
  );
}
