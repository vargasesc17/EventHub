import { solicitar } from "./http";

// CONSULTAR AFORO

export const obtenerAforo = eventoId =>
  solicitar(
    "/api/boleteria/eventos/" +
    encodeURIComponent(eventoId) +
    "/aforo"
  );

// COMPRAR BOLETA

export const comprarBoleta = datos =>
  solicitar("/api/boleteria/boletas", {
    method: "POST",
    body: JSON.stringify(datos),
  });

// REGISTRAR CHECK-IN

export const registrarCheckIn = datos =>
  solicitar("/api/boleteria/checkin", {
    method: "POST",
    body: JSON.stringify(datos),
  });

// BOLETAS DEL COMPRADOR ("MIS BOLETAS")

export const obtenerBoletasDelComprador = compradorId =>
  solicitar(
    "/api/boleteria/boletas?compradorId=" +
    encodeURIComponent(compradorId)
  );
