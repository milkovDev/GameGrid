
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    //local
    //url: 'http://localhost:8087', // Replace with your Keycloak URL
    //online
    url: 'https://keycloak.gamegrid.buzz',
    realm: 'myapp-realm',
    clientId: 'my-react-app' // Public client from Keycloak Admin Console
});

export default keycloak;