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
    scopes: ['api://bd5a9a79-78e1-4d51-9c7e-4f8c8c6331b0/access_as_user'],
  }
};
