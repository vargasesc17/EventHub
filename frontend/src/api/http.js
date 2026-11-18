export async function solicitar(ruta, opciones = {}) {
  const respuesta = await fetch(ruta, { ...opciones, headers: { Accept: 'application/json', ...(opciones.body ? { 'Content-Type': 'application/json' } : {}) } });
  const texto = await respuesta.text();
  let datos;
  try { datos = texto ? JSON.parse(texto) : null; } catch { datos = null; }
  if (!respuesta.ok) throw { ...datos, status: respuesta.status, detail: datos?.detail || datos?.title || 'No se pudo completar la solicitud (' + respuesta.status + ').' };
  if (texto && datos === null) throw { detail: 'El servidor no devolvió JSON. Revisa la conexión con la API.' };
  return datos;
}
