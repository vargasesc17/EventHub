export function leer(clave) {
  const valor = localStorage.getItem(clave);
  if (!valor) return [];
  try { const datos = JSON.parse(valor); if (Array.isArray(datos)) return datos; } catch { /* Conservar los datos existentes. */ }
  throw new Error('No se pudieron leer los datos guardados en este navegador.');
}
export function guardar(clave, datos) {
  try { localStorage.setItem(clave, JSON.stringify(datos)); }
  catch { throw new Error('No se pudieron guardar los datos en este navegador.'); }
}
