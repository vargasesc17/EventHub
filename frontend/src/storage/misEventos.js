import { leer, guardar } from './local';
export const obtenerMisEventos = usuarioId => leer('misEventos:' + usuarioId);
export function guardarMiEvento(usuarioId, eventoId) {
  guardar('misEventos:' + usuarioId, [...new Set([...obtenerMisEventos(usuarioId), eventoId])]);
}
