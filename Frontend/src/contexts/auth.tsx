import {createContext, PropsWithChildren, useContext, useEffect, useState} from "react";

export interface User {
    name:string,
    surname:string,
    loginUrl : string,
    logoutUrl: string,
    principal : any | null,
    xsrfToken: string,
    role: any | null,
    email : string
}

interface AuthContextOutput {
    me: User | null;
}

export const AuthContext = createContext<AuthContextOutput>({ me: null })

export function AuthContextProvider({ children }: PropsWithChildren) {
    const [ me, setMe ] = useState<User | null>(null)

    useEffect(() => {
        const fetchMe = async () => {
            try {
                const res = await  fetch("/me")
                const me = await res.json() as User
                setMe(me)
            } catch(error) {
                setMe(null)
            }
        }
        fetchMe().then()
    }, [])

    return (
        <AuthContext.Provider value={{ me }}>
            { children }
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)