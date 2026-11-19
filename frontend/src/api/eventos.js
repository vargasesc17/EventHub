import { solicitar } from './http';
const ruta = id => '/api/eventos/' + encodeURIComponent(id);
export const obtenerCatalogo = () => solicitar('/api/eventos/catalogo');
export const obtenerEvento = id => solicitar(ruta(id));
export const obtenerEventosDelOrganizador = organizadorId =>
  solicitar('/api/eventos?organizadorId=' + encodeURIComponent(organizadorId));
export const crearEvento = datos => solicitar('/api/eventos', { method: 'POST', body: JSON.stringify(datos) });
export const editarEvento = (id, datos) => solicitar(ruta(id), { method: 'PUT', body: JSON.stringify(datos) });
export const reprogramarEvento = (id, datos) => solicitar(ruta(id), { method: 'PATCH', body: JSON.stringify(datos) });
export function cambiarEstado(id, accion) {
  if (!['publicar', 'iniciar', 'finalizar', 'cancelar'].includes(accion)) throw new Error('Transición desconocida');
  return solicitar(ruta(id) + '/' + accion, { method: 'POST' });
}
