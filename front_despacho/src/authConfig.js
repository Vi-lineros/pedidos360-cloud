export const msalConfig = {
  auth: {
    clientId: import.meta.env.VITE_AZURE_CLIENT_ID,
    authority: `https://login.microsoftonline.com/${import.meta.env.VITE_AZURE_TENANT_ID}`,
    redirectUri: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: 'sessionStorage',
  },
};

export const loginRequest = {
  scopes: ['openid', 'profile'],
};

export const protectedResources = {
  api: {
    endpoint: import.meta.env.VITE_API_URL_VENTAS,
    scopes: ['api://0ca4fc8d-b22c-46b7-8786-cfa1acfa1e76/access_as_user'],
  }
};
