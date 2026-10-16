import { BrowserRouter } from "react-router-dom";
import { AuthenticatedTemplate, UnauthenticatedTemplate } from "@azure/msal-react";
import AuthButtons from "./components/AuthButtons.jsx";
import AppRoutes from "./Routes/AppRoutes.jsx";

const App = () => {
  return (
    <BrowserRouter>
      <UnauthenticatedTemplate>
        <div className="min-h-screen flex flex-col items-center justify-center bg-slate-100 p-6 text-center">
          <div className="bg-white rounded-xl shadow-lg p-8 max-w-md w-full">
            <h1 className="text-2xl font-bold text-slate-800 mb-4">Acceso requerido</h1>
            <p className="text-slate-600 mb-6">
              Debes iniciar sesión para acceder a la aplicación.
            </p>
            <AuthButtons />
          </div>
        </div>
      </UnauthenticatedTemplate>

      <AuthenticatedTemplate>
        <div className="min-h-screen bg-slate-100">
          <header className="bg-white shadow-sm border-b border-slate-200 px-6 py-4 flex justify-end">
            <AuthButtons />
          </header>
          <main>
            <AppRoutes />
          </main>
        </div>
      </AuthenticatedTemplate>
    </BrowserRouter>
  );
};

export default App;
