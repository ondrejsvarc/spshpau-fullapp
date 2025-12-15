import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './keycloak.js';
import { UserProvider } from './contexts/UserContext.jsx';

const root = ReactDOM.createRoot(document.getElementById('root'));

const initOptions = {
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    pkceMethod: 'S256',
};

const handleTokens = (tokens) => {
    if (tokens.token) {
        console.log('Keycloak Access token received/refreshed');
    }
};

root.render(
    // <React.StrictMode>
    <ReactKeycloakProvider
        authClient={keycloak}
        initOptions={initOptions}
        onTokens={handleTokens}
        LoadingComponent={<div style={{textAlign: 'center', marginTop: '50px'}}>Initializing Keycloak Authentication...</div>}
    >
        <UserProvider> {}
            <App />
        </UserProvider>
    </ReactKeycloakProvider>
    // </React.StrictMode>
);