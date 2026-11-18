import { leer, guardar } from './local';
export const obtenerMisBoletas = usuarioId => leer('misBoletas:' + usuarioId);
export function guardarMiBoleta(usuarioId, boleta) {
  guardar('misBoletas:' + usuarioId, [...obtenerMisBoletas(usuarioId).filter(item => item.id !== boleta.id), boleta]);
}
