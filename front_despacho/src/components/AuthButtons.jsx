import { useMsal, useIsAuthenticated } from "@azure/msal-react";
import { loginRequest } from "../authConfig.js";

const AuthButtons = () => {
  const { instance } = useMsal();
  const isAuthenticated = useIsAuthenticated();

  const login = () => {
    instance.loginRedirect(loginRequest);
  };

  const logout = () => {
    instance.logoutRedirect();
  };

  return (
    <div className="flex items-center gap-3">
      {isAuthenticated ? (
        <button
          type="button"
          onClick={logout}
          className="bg-red-600 hover:bg-red-700 text-white font-semibold px-4 py-2 rounded-lg shadow-md transition duration-200"
        >
          Cerrar sesión
        </button>
      ) : (
        <button
          type="button"
          onClick={login}
          className="bg-teal-600 hover:bg-teal-700 text-white font-semibold px-4 py-2 rounded-lg shadow-md transition duration-200"
        >
          Iniciar sesión
        </button>
      )}
    </div>
  );
};

export default AuthButtons;
