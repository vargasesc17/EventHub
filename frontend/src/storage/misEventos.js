import { leer, guardar } from './local';
export const obtenerMisEventos = usuarioId => leer('misEventos:' + usuarioId);
export function guardarMiEvento(usuarioId, eventoId) {
  guardar('misEventos:' + usuarioId, [...new Set([...obtenerMisEventos(usuarioId), eventoId])]);
}
/** Olvida eventos que ya no existen en el servidor (por ejemplo, después de borrar la base). */
export function olvidarMisEventos(usuarioId, eventoIds) {
  const quitar = new Set(eventoIds);
  guardar('misEventos:' + usuarioId, obtenerMisEventos(usuarioId).filter(id => !quitar.has(id)));
}
