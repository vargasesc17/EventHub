import { useEffect, useState } from 'react';
export default function useConsulta(consultar, parametro) {
  const [estado, setEstado] = useState({ datos: null, cargando: true, error: null });
  useEffect(() => {
    let activo = true;
    consultar(parametro).then(datos => { if (activo) setEstado({ datos, cargando: false, error: null }); })
      .catch(error => { if (activo) setEstado({ datos: null, cargando: false, error }); });
    return () => { activo = false; };
  }, [consultar, parametro]);
  return estado;
}
