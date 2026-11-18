// Preparado para PR #65; las pantallas permanecen deshabilitadas hasta su integración.
import { solicitar } from './http';
export const obtenerAforo = eventoId => solicitar('/api/boleteria/eventos/' + encodeURIComponent(eventoId) + '/aforo');
export const comprarBoleta = datos => solicitar('/api/boleteria/boletas', { method: 'POST', body: JSON.stringify(datos) });
