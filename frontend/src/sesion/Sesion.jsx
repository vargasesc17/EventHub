import { createContext, useContext, useState } from "react";

import { USUARIOS } from "./usuarios";

const SesionContext = createContext(null);

export function SesionProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    try {
      const guardado = JSON.parse(localStorage.getItem("usuario"));
      return USUARIOS.find(u => u.id === guardado?.id) || null;
    } catch {
      return null;
    }
  });

  const ingresar = (u) => {
    localStorage.setItem("usuario", JSON.stringify(u));
    setUsuario(u);
  };
  const salir = () => {
    localStorage.removeItem("usuario");
    setUsuario(null);
  };

  return (
    <SesionContext.Provider value={{ usuario, ingresar, salir }}>
      {children}
    </SesionContext.Provider>
  );
}

export const useSesion = () => useContext(SesionContext);