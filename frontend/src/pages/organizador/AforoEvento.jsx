import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";

import { obtenerEvento } from "../../api/eventos";
import { obtenerAforo } from "../../api/boleteria";

import useConsulta from "../../api/useConsulta";
import { useSesion } from "../../sesion/Sesion";

// ESTILO DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! border-0! px-7! py-3! " +
  "font-inter-bold font-bold! text-[17px] " +
  "text-white! no-underline! cursor-pointer " +
  "transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-md " +
  "active:scale-95 disabled:opacity-50 " +
  "disabled:cursor-not-allowed";

// COLORES POR CATEGORÍA

const coloresCategorias = {
  GENERAL: "bg-[#69529b]",
  VIP: "bg-[#260477]",
  ESTUDIANTE: "bg-[#298496]",
  CORTESIA: "bg-[#373d4d]",
};

// TARJETA DE ESTADÍSTICAS

function TarjetaEstadistica({
  titulo,
  valor,
  descripcion,
  color,
}) {

  return (

    <article className="
      flex flex-col gap-3
      rounded-[35px]
      border-2 border-[#9682b8]
      bg-white p-6 shadow-sm
      transition-all duration-200
      hover:-translate-y-1
      hover:shadow-md
    ">

      <h3 className="
        font-inter-semibold font-semibold!
        text-[18px] text-[#69529b]
      ">

        {titulo}

      </h3>

      <p className={`
        font-inter-bold font-bold!
        text-[46px] leading-tight
        ${color}
      `}>

        {valor ?? "—"}

      </p>

      <p className="
        font-inter-semibold
        text-[14px] text-[#69529b]
      ">

        {descripcion}

      </p>

    </article>

  );

}

// BARRA DE PROGRESO

function BarraAforo({ porcentaje }) {

  const disponible = Number.isFinite(porcentaje);

  const progreso = disponible
    ? Math.min(100, Math.max(0, porcentaje))
    : 0;

  const color =
    progreso >= 100
      ? "bg-[#ef2424]"
      : progreso >= 80
        ? "bg-[#e5a13b]"
        : "bg-[#298496]";

  return (

    <div className="flex flex-col gap-3">

      <div className="
        flex items-center justify-between gap-3
        font-inter-bold text-[17px]
      ">

        <span>Ocupación</span>

        <span>

          {disponible ? `${progreso.toFixed(1)}%` : "—"}

        </span>

      </div>

      <div
        className="
          h-[25px] w-full
          overflow-hidden rounded-full
          bg-[#e5e5e7]
        "
        role="progressbar"
        aria-label="Porcentaje de ocupación"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={disponible ? progreso : undefined}
      >

        <div
          className={`
            h-full rounded-full
            transition-all duration-500
            ${color}
          `}
          style={{ width: `${progreso}%` }}
        />

      </div>

    </div>

  );

}

export default function AforoEvento() {

  const { eventoId } = useParams();
  const { usuario } = useSesion();

  const [aforo, setAforo] = useState(null);
  const [errorAforo, setErrorAforo] = useState(null);
  const [cargandoAforo, setCargandoAforo] = useState(false);
  const [actualizacion, setActualizacion] = useState(0);
  const [ultimaConsulta, setUltimaConsulta] = useState(null);

  // CONSULTAR INFORMACIÓN DEL EVENTO

  const {
    datos: evento,
    cargando,
    error,
  } = useConsulta(obtenerEvento, eventoId);

  // COMPROBAR QUE EL EVENTO PERTENECE AL ORGANIZADOR

  const permitido =
    usuario?.rol === "ORGANIZADOR" &&
    evento?.organizadorId === usuario?.id;

  // CONSULTAR AFORO

  useEffect(() => {

    if (!permitido) return;

    let activo = true;

    async function cargarAforo() {

      setCargandoAforo(true);
      setErrorAforo(null);
      setAforo(null);
      setUltimaConsulta(null);

      try {

        const resultado = await obtenerAforo(eventoId);

        if (!activo) return;

        setAforo(resultado);
        setUltimaConsulta(new Date());

      } catch (err) {

        if (activo) setErrorAforo(err);

      } finally {

        if (activo) setCargandoAforo(false);

      }

    }

    cargarAforo();

    return () => {
      activo = false;
    };

  }, [eventoId, permitido, actualizacion]);

  // DATOS DEL AFORO
  // Ajustar estos nombres al contrato definitivo de Boletería.

  const aforoMaximo = evento?.aforoMaximo;

  const cuposOcupados =
    aforo?.cuposOcupados ??
    aforo?.boletasVendidas ??
    null;

  const cuposDisponibles =
    aforo?.cuposDisponibles ??
    null;

  const porcentaje =
    aforoMaximo > 0 && cuposOcupados !== null
      ? (cuposOcupados / aforoMaximo) * 100
      : null;

  return (

    <Pagina titulo="Aforo del evento">

      <EstadoCarga
        cargando={cargando}
        error={error}
      />

      {/* BOTÓN REGRESAR */}

      <Link
        to="/organizador"
        className={`${buttonStyle}
          bg-[#69529b]! hover:bg-[#493778]! mb-6`}
      >

        ← Volver a mis eventos

      </Link>

      {evento && !permitido && (

        <p
          role="alert"
          className="
            rounded-[25px] bg-red-100
            p-5 font-inter-semibold text-red-700
          "
        >

          No puedes consultar el aforo de este evento.

        </p>

      )}

      {evento && permitido && (

        <div className="mx-auto max-w-[1150px]">

          {/* INFORMACIÓN DEL EVENTO */}

          <section className="
            rounded-[40px]
            bg-[#69529b] p-7 sm:p-9
            text-white shadow-md
          ">

            <div className="
              flex flex-wrap items-center
              justify-between gap-5
            ">

              <div>

                <p className="
                  font-inter-semibold
                  text-[15px] text-white/80
                ">

                  Control de aforo

                </p>

                <h2 className="
                  mt-2 font-inter-bold font-bold!
                  text-[30px] sm:text-[36px]
                ">

                  {evento.nombre}

                </h2>

              </div>

              <span className="
                rounded-full
                border-2 border-white/40
                bg-white/20 px-5 py-2
                font-inter-bold text-[15px]
              ">

                {evento.estado}

              </span>

            </div>

            <hr className="my-5 border-white/30" />

            <div className="
              grid grid-cols-1 gap-4
              sm:grid-cols-2
            ">

              <div className="
                rounded-[25px] bg-white/15 p-5
              ">

                <p className="
                  font-inter-semibold
                  text-[14px] text-white/80
                ">

                  Lugar del evento

                </p>

                <p className="
                  mt-1 font-inter-bold text-[19px]
                ">

                  {evento.lugar}

                </p>

              </div>

              <div className="
                rounded-[25px] bg-white/15 p-5
              ">

                <p className="
                  font-inter-semibold
                  text-[14px] text-white/80
                ">

                  Fecha y hora

                </p>

                <p className="
                  mt-1 font-inter-bold text-[19px]
                ">

                  {new Date(evento.fechaInicio)
                    .toLocaleString("es-CO")}

                </p>

              </div>

            </div>

          </section>

          {/* ESTADÍSTICAS GENERALES */}

          <section className="mt-8">

            <h2 className="
              mb-5 font-inter-bold
              font-bold! text-[28px]
            ">

              Resumen de aforo

            </h2>

            <div className="
              grid grid-cols-1 gap-5
              sm:grid-cols-3
            ">

              <TarjetaEstadistica
                titulo="Aforo máximo"
                valor={aforoMaximo}
                descripcion="Capacidad total del evento"
                color="text-[#260477]"
              />

              <TarjetaEstadistica
                titulo="Cupos ocupados"
                valor={cuposOcupados}
                descripcion="Cupos reportados por Boletería"
                color="text-[#5524bf]"
              />

              <TarjetaEstadistica
                titulo="Cupos disponibles"
                valor={cuposDisponibles}
                descripcion="Disponibilidad reportada"
                color="text-[#298496]"
              />

            </div>

          </section>

          {/* PORCENTAJE DE OCUPACIÓN */}

          <section className="
            mt-6 rounded-[35px]
            border-2 border-[#9682b8]
            bg-white p-7 shadow-sm
          ">

            <h2 className="
              mb-6 font-inter-bold
              text-[25px] text-[#260477]
            ">

              Ocupación general del evento

            </h2>

            <BarraAforo porcentaje={porcentaje} />

            {porcentaje === null && (

              <p className="
                mt-4 font-inter-semibold
                text-[14px] text-[#69529b]
              ">

                El porcentaje estará disponible cuando
                se reciban los datos de Boletería.

              </p>

            )}

          </section>

          {/* AFORO POR CATEGORÍA */}

          <section className="mt-8">

            <h2 className="
              mb-5 font-inter-bold
              font-bold! text-[28px]
            ">

              Aforo por categoría

            </h2>

            <div className="
              grid grid-cols-1 gap-5
              sm:grid-cols-2
              xl:grid-cols-4
            ">

              {(evento.categorias || []).map((categoria) => {

                const datosCategoria =
                  aforo?.categorias?.find(
                    (c) => c.categoria === categoria.tipo
                  );

                const ocupados =
                  datosCategoria?.cuposOcupados ??
                  datosCategoria?.boletasVendidas ??
                  null;

                const disponibles =
                  datosCategoria?.cuposDisponibles ??
                  null;

                const porcentajeCategoria =
                  categoria.cupos > 0 && ocupados !== null
                    ? (ocupados / categoria.cupos) * 100
                    : null;

                return (

                  <article
                    key={categoria.tipo}
                    className="
                      overflow-hidden rounded-[35px]
                      border-2 border-[#9682b8]
                      bg-white shadow-sm
                      transition-all duration-200
                      hover:-translate-y-1
                      hover:shadow-md
                    "
                  >

                    {/* ENCABEZADO */}

                    <header className={`
                      p-5 text-center text-white
                      ${coloresCategorias[categoria.tipo]
                        || "bg-[#69529b]"}
                    `}>

                      <h3 className="
                        font-inter-bold font-bold!
                        text-[23px]
                      ">

                        {categoria.tipo}

                      </h3>

                    </header>

                    {/* INFORMACIÓN DE LA CATEGORÍA */}

                    <div className="flex flex-col gap-4 p-5">

                      <div className="
                        rounded-[25px] bg-[#f5f2fa] p-4
                      ">

                        <p className="
                          font-inter-semibold
                          text-[14px] text-[#69529b]
                        ">

                          Cupos asignados

                        </p>

                        <p className="
                          font-inter-bold
                          text-[30px] text-[#260477]
                        ">

                          {categoria.cupos}

                        </p>

                      </div>

                      <div className="
                        rounded-[25px] bg-[#f5f2fa] p-4
                      ">

                        <p className="
                          font-inter-semibold
                          text-[14px] text-[#69529b]
                        ">

                          Cupos ocupados

                        </p>

                        <p className="
                          font-inter-bold
                          text-[26px] text-[#5524bf]
                        ">

                          {ocupados ?? "—"}

                        </p>

                      </div>

                      <div className="
                        rounded-[25px] bg-[#f5f2fa] p-4
                      ">

                        <p className="
                          font-inter-semibold
                          text-[14px] text-[#69529b]
                        ">

                          Cupos disponibles

                        </p>

                        <p className="
                          font-inter-bold
                          text-[26px] text-[#298496]
                        ">

                          {disponibles ?? "—"}

                        </p>

                      </div>

                      <BarraAforo
                        porcentaje={porcentajeCategoria}
                      />

                    </div>

                  </article>

                );

              })}

            </div>

          </section>

          {/* ACTUALIZAR INFORMACIÓN */}

          <section className="
            mt-8 rounded-[30px]
            border-2 border-[#d5c9e7]
            bg-white p-6
          ">

            <div className="
              flex flex-wrap items-center
              justify-between gap-5
            ">

              <div>

                <h3 className="
                  font-inter-bold text-[21px]
                  text-[#260477]
                ">

                  Información de disponibilidad

                </h3>

                <p className="
                  mt-2 font-inter-semibold
                  text-[14px] text-[#69529b]
                ">

                  {ultimaConsulta
                    ? `Última consulta: ${
                        ultimaConsulta.toLocaleString("es-CO")
                      }`
                    : "Sin consultas exitosas todavía."}

                </p>

              </div>

              <button
                type="button"
                disabled={cargandoAforo}
                onClick={() =>
                  setActualizacion((anterior) => anterior + 1)
                }
                className={`${buttonStyle}
                  bg-[#5524bf]! hover:bg-[#260477]!`}
              >

                {cargandoAforo
                  ? "Consultando..."
                  : "Actualizar información"}

              </button>

            </div>

          </section>

          {/* AVISO DE INTEGRACIÓN */}

          {errorAforo && (

            <div
              role="alert"
              className="
                mt-6 rounded-[25px]
                border-2 border-[#d5c9e7]
                bg-[#f5f2fa] p-6
              "
            >

              <h3 className="
                font-inter-bold text-[20px]
                text-[#260477]
              ">

                Disponibilidad no accesible

              </h3>

              <p className="
                mt-2 font-inter-semibold
                text-[16px] text-[#69529b]
              ">

                No fue posible consultar el aforo.
                La información de disponibilidad depende
                del servicio de Boletería (PR #65).

              </p>

              <p className="
                mt-3 font-inter-semibold
                text-[14px] text-[#69529b]
              ">

                {errorAforo.detail || errorAforo.message}

              </p>

            </div>

          )}

        </div>

      )}

    </Pagina>

  );

}