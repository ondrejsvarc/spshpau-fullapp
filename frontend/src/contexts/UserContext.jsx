import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { syncUserWithBackend, getCurrentUserFromBackend, getMyIncomingRequests } from '../services/api';

const UserContext = createContext(null);

export const useUser = () => useContext(UserContext);

export const UserProvider = ({ children }) => {
    const { keycloak, initialized } = useKeycloak();
    const [appUser, setAppUser] = useState(null);
    const [loadingUser, setLoadingUser] = useState(false);
    const [userError, setUserError] = useState(null);
    const [hasPendingRequests, setHasPendingRequests] = useState(false);

    const fetchCoreUserData = useCallback(async (forceSync = false) => {
        if (initialized && keycloak.authenticated && (!appUser || forceSync) && !loadingUser) {
            setLoadingUser(true);
            setUserError(null);
            console.log('UserContext: Keycloak authenticated, attempting to sync and fetch user data...');
            try {
                if (forceSync || !appUser) {
                    await syncUserWithBackend();
                    console.log('UserContext: User sync successful.');
                }
                const backendUser = await getCurrentUserFromBackend();
                console.log('UserContext: Backend user data fetched:', backendUser);
                setAppUser(backendUser);
                return backendUser;
            } catch (error) {
                console.error('UserContext: Failed to sync or fetch user data from backend:', error);
                setUserError(error.message || 'Failed to load user data.');
                return null; // Return null on error
            } finally {
                setLoadingUser(false);
            }
        } else if (initialized && !keycloak.authenticated) {
            setAppUser(null);
            setHasPendingRequests(false);
        }
        return appUser;
    }, [initialized, keycloak, appUser, loadingUser]);

    const checkIncomingRequests = useCallback(async () => {
        if (initialized && keycloak.authenticated) {
            console.log('UserContext: Checking for incoming connection requests...');
            try {
                const requestsData = await getMyIncomingRequests(0, 1);
                if (requestsData && requestsData.totalElements > 0) {
                    setHasPendingRequests(true);
                    console.log('UserContext: Pending incoming requests found.');
                } else {
                    setHasPendingRequests(false);
                    console.log('UserContext: No pending incoming requests.');
                }
            } catch (error) {
                console.error('UserContext: Failed to check incoming requests:', error);
            }
        }
    }, [initialized, keycloak.authenticated]);

    useEffect(() => {
        fetchCoreUserData().then(user => {
            if (user) {
                checkIncomingRequests();
            }
        });
    }, [fetchCoreUserData, checkIncomingRequests]);

    const refreshAppUserAndRequests = useCallback(async () => {
        console.log("UserContext: Explicitly refreshing appUser data and requests...");
        const user = await fetchCoreUserData(true);
        if (user) {
            await checkIncomingRequests();
        }
    }, [fetchCoreUserData, checkIncomingRequests]);

    useEffect(() => {
      if (initialized && keycloak.authenticated) {
        const intervalId = setInterval(checkIncomingRequests, 60000);
        return () => clearInterval(intervalId);
      }
    }, [initialized, keycloak.authenticated, checkIncomingRequests]);


    return (
        <UserContext.Provider value={{
            appUser,
            loadingUser,
            userError,
            keycloakInitialized: initialized,
            keycloakAuthenticated: keycloak.authenticated,
            hasPendingRequests,
            refreshAppUser: refreshAppUserAndRequests,
            refreshRequests: checkIncomingRequests
        }}>
            {children}
        </UserContext.Provider>
    );
};