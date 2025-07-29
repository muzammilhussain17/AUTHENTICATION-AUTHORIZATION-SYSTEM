import {createContext, useState} from "react";
import {Constants} from "../util/Constants";

export const Context = createContext();

const backendURL = Constants.SERVER_URL;
const [isLoggedIn, setIsLoggedIn]=useState(false)
const [userData , setUserData]= useState(false)

export const AppContextProvider = (props) => {
    return (
        <AppContext.Provider value={{}}>
            {props.children}
        </AppContext.Provider>
    )
}