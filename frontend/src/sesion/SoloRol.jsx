import { Navigate } from "react-router-dom";
import { useSesion } from "./Sesion";

export function SoloRol({ roles, children }) {
  const { usuario } = useSesion();
  if (!usuario) return <Navigate to="/login" replace />;
  if (!roles.includes(usuario.rol)) return <Navigate to="/" replace />;
  return children;
}
