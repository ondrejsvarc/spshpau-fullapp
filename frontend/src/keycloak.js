import Keycloak from 'keycloak-js';

// Use environment variables for Keycloak configuration for flexibility
const keycloakConfig = {
    url: import.meta.env.VITE_KEYCLOAK_URL || 'http://192.168.1.112:8080',
    realm: import.meta.env.VITE_KEYCLOAK_REALM || 'SPSHPAU',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'spshpau-rest-api',
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;