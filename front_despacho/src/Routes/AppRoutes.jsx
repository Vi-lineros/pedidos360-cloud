import { Routes, Route } from "react-router-dom";
import { CrudAdmin } from "../componentes/CrudAdmin.jsx";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<CrudAdmin />} />
    </Routes>
  );
};

export default AppRoutes;
