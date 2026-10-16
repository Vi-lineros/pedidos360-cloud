import axios from "axios";
import { msalInstance } from "../msal.js";
import { protectedResources } from "../authConfig.js";

axios.interceptors.request.use(async (config) => {
  try {
    const account = msalInstance.getAllAccounts()[0];

    if (!account) {
      return config;
    }

    const tokenResponse = await msalInstance.acquireTokenSilent({
      scopes: protectedResources.api.scopes,
      account,
    });

    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${tokenResponse.accessToken}`,
    };

    return config;
  } catch (error) {
    console.warn("No se pudo obtener token silencioso:", error);
    return config;
  }
});
