import {
  createContext,
  PropsWithChildren,
  useContext,
  useEffect,
  useState,
} from "react";

export type UserRole =
  | "customer"
  | "professional"
  | "guest"
  | "manager"
  | "operator"
  | "user";

export interface User {
  userId: string;
  name: string;
  surname: string;
  loginUrl: string;
  logoutUrl: string;
  principal: any | null;
  xsrfToken: string;
  roles: UserRole[];
  email: string;
}

interface AuthContextOutput {
  me: User | null;
  refreshToken: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextOutput>({
  me: null,
  refreshToken: () => Promise.resolve(),
});

export function AuthContextProvider({ children }: PropsWithChildren) {
  const [me, setMe] = useState<User | null>(null);

  async function fetchMe() {
    try {
      const res = await fetch("/me");
      const me = (await res.json()) as User;
      setMe(me);
    } catch (error) {
      setMe(null);
    }
  }

  async function refreshToken() {
    if (me === null) return;
    try {
      await fetch(me.loginUrl, { mode: "no-cors" });
      await fetchMe();
    } catch (error) {
      setMe(null);
    }
  }

  useEffect(() => {
    fetchMe().then();
  }, []);

  return (
    <AuthContext.Provider value={{ me, refreshToken }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
