import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";

import { obtenerEvento, cambiarEstado } from "../../api/eventos";
import { obtenerMisEventos, olvidarMisEventos } from "../../storage/misEventos";
import { useSesion } from "../../sesion/Sesion";

const acciones = {
  BORRADOR: ["publicar", "cancelar"],
  PUBLICADO: ["iniciar", "cancelar"],
  EN_CURSO: ["finalizar"],
};

// ESTILO GENERAL DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! px-6! py-2.5! " +
  "font-inter-semibold font-semibold! text-[16px] " +
  "text-white! no-underline! border-0! " +
  "cursor-pointer transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-md " +
  "active:scale-95 disabled:opacity-50 " +
  "disabled:cursor-not-allowed";

// COLORES SEGÚN LA ACCIÓN

const colores = {
  editar: "bg-[#5524bf]! hover:bg-[#260477]!",
  reprogramar: "bg-[#5524bf]! hover:bg-[#260477]!",
  publicar: "bg-[#298496]! hover:bg-[#206b79]!",
  iniciar: "bg-[#298496]! hover:bg-[#206b79]!",
  cancelar: "bg-[#b94363]! hover:bg-[#922f4b]!",
  finalizar: "bg-[#373d4d]! hover:bg-[#242936]!",
  aforo: "bg-[#69529b]! hover:bg-[#493778]!",
  consultar: "bg-[#5524bf]! hover:bg-[#260477]!",
  cerrar: "bg-[#373d4d]! hover:bg-[#242936]!",
};

export default function GestionEventos() {
  const { usuario } = useSesion();

  const [eventos, setEventos] = useState([]);
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [ocupado, setOcupado] = useState(false);
  const [detalle, setDetalle] = useState(null);
  const [aviso, setAviso] = useState("");

  // CARGAR LOS EVENTOS DEL ORGANIZADOR

  useEffect(() => {
    let activo = true;

    async function cargar() {
      try {
        const ids = obtenerMisEventos(usuario.id);
        const resultados = await Promise.allSettled(ids.map(obtenerEvento));

        if (!activo) return;

        setEventos(
          resultados
            .filter(
              (r) =>
                r.status === "fulfilled" &&
                r.value.organizadorId === usuario.id
            )
            .map((r) => r.value)
        );

        // Un 404 significa que el evento ya no existe (por ejemplo, se borró la base de datos):
        // se olvida su id. Cualquier otra falla (Eventos caído, red) sí se muestra.

        const inexistentes = ids.filter(
          (_, i) =>
            resultados[i].status === "rejected" &&
            resultados[i].reason?.status === 404
        );
        if (inexistentes.length) {
          olvidarMisEventos(usuario.id, inexistentes);
        }

        if (
          resultados.some(
            (r) => r.status === "rejected" && r.reason?.status !== 404
          )
        ) {
          setError({
            detail:
              "Algunos eventos no pudieron consultarse. Sus identificadores siguen guardados.",
          });
        }
      } catch (e) {
        if (activo) setError(e);
      } finally {
        if (activo) setCargando(false);
      }
    }

    cargar();

    return () => {
      activo = false;
    };
  }, [usuario.id]);

  // CAMBIAR ESTADO DEL EVENTO

  async function transicion(evento, accion) {
    if (
      accion === "cancelar" &&
      !window.confirm(
        evento.estado === "PUBLICADO"
          ? "¿Cancelar este evento? Se avisará por correo a todos los compradores."
          : "¿Cancelar este evento?"
      )
    ) {
      return;
    }

    setOcupado(true);
    setError(null);
    setAviso("");

    try {
      const actualizado = await cambiarEstado(evento.id, accion);

      setEventos((actual) =>
        actual.map((e) =>
          e.id === actualizado.id ? actualizado : e
        )
      );

      // CONFIRMACIÓN DE LA CANCELACIÓN
      // Eventos publica evento.actualizado: Boletería cancela las boletas y Notificaciones
      // envía el correo a cada comprador. Un borrador no tiene compradores a quienes avisar.

      if (accion === "cancelar") {
        setAviso(
          evento.estado === "PUBLICADO"
            ? `"${evento.nombre}" fue cancelado. Se notificó a los asistentes: cada comprador recibe un correo y sus boletas quedan canceladas.`
            : `"${evento.nombre}" fue cancelado. Estaba en borrador, así que no había compradores a quienes avisar.`
        );
      }
    } catch (e) {
      setError(e);
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pagina titulo="Mis eventos">

      <EstadoCarga cargando={cargando} error={error} />

      {/* CONFIRMACIÓN DE ACCIONES */}

      {aviso && (
        <p
          role="status"
          className="mb-6 rounded-[25px] bg-[#e3f7e3] p-5
          font-inter-semibold text-[#1d6b1d]"
        >
          {aviso}
        </p>
      )}

      {!cargando && !error && !eventos.length && (
        <p className="font-inter-semibold">
          No tienes eventos guardados en este navegador.
        </p>
      )}

      {/* LISTADO DE EVENTOS */}

      <section className="eh-grid">

        {eventos.map((evento) => (

          <article
            className="eh-panel rounded-[35px]!"
            key={evento.id}
          >

            {/* NOMBRE DEL EVENTO */}

            <h2 className="font-inter-bold font-bold! text-[26px]">
              {evento.nombre}
            </h2>

            {/* LUGAR */}

            <p className="font-inter-semibold font-semibold!">
              {evento.lugar}
            </p>

            {/* ESTADO */}

            <p>
              Estado:{" "}
              <span className="font-inter-semibold font-semibold!">
                {evento.estado}
              </span>
            </p>

            {/* BOTONES DE ACCIONES */}

            <div className="eh-actions flex! flex-wrap! items-center! gap-3! mt-4">

              {/* EDITAR EVENTO */}

              {evento.estado === "BORRADOR" && (
                <Link
                  to={"/organizador/" + evento.id + "/editar"}
                  className={`${buttonStyle} ${colores.editar}`}
                >
                  Editar
                </Link>
              )}

              {/* REPROGRAMAR EVENTO */}

              {evento.estado === "PUBLICADO" && (
                <Link
                  to={
                    "/organizador/" +
                    evento.id +
                    "/reprogramar"
                  }
                  className={`${buttonStyle} ${colores.reprogramar}`}
                >
                  Reprogramar
                </Link>
              )}

              {/* PUBLICAR, INICIAR, CANCELAR Y FINALIZAR */}

              {(acciones[evento.estado] || []).map((accion) => (

                <button
                  key={accion}
                  type="button"
                  disabled={ocupado}
                  onClick={() => transicion(evento, accion)}
                  className={`${buttonStyle} ${colores[accion]}`}
                >
                  {accion[0].toUpperCase() + accion.slice(1)}
                </button>

              ))}

              {/* VER AFORO */}

              {["PUBLICADO", "EN_CURSO"].includes(
                evento.estado
              ) && (
                <Link
                  to={"/organizador/" + evento.id + "/aforo"}
                  className={`${buttonStyle} ${colores.aforo}`}
                >
                  Ver aforo
                </Link>
              )}

              {/* CONSULTAR INFORMACIÓN */}

              {["FINALIZADO", "CANCELADO"].includes(
                evento.estado
              ) && (
                <button
                  type="button"
                  onClick={() => setDetalle(evento)}
                  className={`${buttonStyle} ${colores.consultar}`}
                >
                  Consultar información
                </button>
              )}

            </div>

          </article>

        ))}

      </section>

      {/* DETALLES DEL EVENTO */}

      {detalle && (

        <section className="eh-panel rounded-[35px]! mt-6">

          <h2 className="font-inter-bold font-bold! text-[26px]">
            {detalle.nombre}
          </h2>

          <p className="font-inter-semibold">
            {detalle.lugar} ·{" "}
            {new Date(detalle.fechaInicio).toLocaleString("es-CO")}
          </p>

          <p className="font-inter-semibold">
            Aforo: {detalle.aforoMaximo}
          </p>

          {detalle.categorias.map((c) => (

            <p key={c.tipo}>
              {c.tipo} · {c.cupos} cupos · {c.precioBase} COP
            </p>

          ))}

          <button
            type="button"
            onClick={() => setDetalle(null)}
            className={`${buttonStyle} ${colores.cerrar} mt-4`}
          >
            Cerrar información
          </button>

        </section>

      )}

    </Pagina>
  );
}