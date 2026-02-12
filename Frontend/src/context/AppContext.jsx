// AppContext.jsx
import axios from 'axios';
import React, { createContext, useState } from "react";
import { AppConstants } from "../util/Constants";
import { toast } from 'react-toastify';

// ✅ Create context
// eslint-disable-next-line react-refresh/only-export-components
export const AppContext = createContext();

// ✅ Provider component
export const AppContextProvider = ({ children }) => {
  const backendURL = AppConstants.SERVER_URL;
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userData, setUserData] = useState(null);
  const [userRole, setUserRole] = useState(null);

  const getUserdata = async () => {
    try {
      const response = await axios.get(`${backendURL}/profile/profilee`);
      if (response.status === 200) {
        setUserData(response.data);
        setIsLoggedIn(true);
      } else {
        toast.error("Error fetching user data:");
      }
    } catch (error) {
      // If access token expired, try refreshing
      if (error.response?.status === 401) {
        const refreshed = await refreshAccessToken();
        if (refreshed) {
          // Retry the original request
          try {
            const retryResponse = await axios.get(`${backendURL}/profile/profilee`);
            if (retryResponse.status === 200) {
              setUserData(retryResponse.data);
              setIsLoggedIn(true);
              return;
            }
          } catch (retryError) {
            console.error("Retry failed:", retryError);
          }
        }
        // Refresh failed — user is logged out
        setIsLoggedIn(false);
        setUserData(null);
        setUserRole(null);
      } else {
        toast.error(error.message);
      }
    }
  };

  /**
   * Calls the /refresh endpoint to get a new access token
   * using the refresh token stored in HttpOnly cookie.
   */
  const refreshAccessToken = async () => {
    try {
      const response = await axios.post(`${backendURL}/refresh`, {}, { withCredentials: true });
      if (response.status === 200) {
        setUserRole(response.data.role);
        return true;
      }
      return false;
    } catch (error) {
      console.error("Token refresh failed:", error);
      return false;
    }
  };

  const contextValue = {
    backendURL,
    isLoggedIn,
    setIsLoggedIn,
    userData,
    setUserData,
    getUserdata,
    userRole,
    setUserRole,
    refreshAccessToken,
  };

  return (
    <AppContext.Provider value={contextValue}>
      {children}
    </AppContext.Provider>
  );
};

export default AppContextProvider;