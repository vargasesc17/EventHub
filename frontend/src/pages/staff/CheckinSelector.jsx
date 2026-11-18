
import { Link } from "react-router-dom";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";

import useConsulta from "../../api/useConsulta";
import { obtenerCatalogo } from "../../api/eventos";

// ESTILO DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! border-0! px-6! py-3! " +
  "font-inter-bold font-bold! text-[17px] " +
  "text-white! no-underline! cursor-pointer " +
  "bg-[#5524bf]! hover:bg-[#260477]! " +
  "transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-md " +
  "active:scale-95";

// ICONOS

function Icono({ tipo, size = 24 }) {

  return (

    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >

      {tipo === "calendario" && (

        <>
          <rect x="3" y="5" width="18" height="16" rx="3" />
          <path d="M16 3v4M8 3v4M3 10h18" />
          <path d="M8 15h2m4 0h2" />
        </>

      )}

      {tipo === "ubicacion" && (

        <>
          <path d="M20 10c0 6-8 11-8 11S4 16 4 10a8 8 0 1 1 16 0Z" />
          <circle cx="12" cy="10" r="3" />
        </>

      )}

      {tipo === "personas" && (

        <>
          <circle cx="9" cy="7" r="4" />
          <path d="M2 21v-3a7 7 0 0 1 14 0v3" />
          <path d="M17 4a4 4 0 0 1 0 8M18 15a5 5 0 0 1 4 5" />
        </>

      )}

      {tipo === "qr" && (

        <>
          <rect x="3" y="3" width="7" height="7" rx="1" />
          <rect x="14" y="3" width="7" height="7" rx="1" />
          <rect x="3" y="14" width="7" height="7" rx="1" />

          <path d="M14 14h3v3h-3z" />
          <path d="M21 14v3M14 21h3M21 21v-1" />
        </>

      )}

    </svg>

  );

}

// FORMATO DE FECHA

function formatearFecha(fecha) {

  if (!fecha) return "No disponible";

  return new Date(fecha).toLocaleString("es-CO", {
    day: "numeric",
    month: "long",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });

}

// ESTILOS SEGÚN EL ESTADO DEL EVENTO

const coloresEstado = {

  PUBLICADO: "bg-[#e9e1f8] text-[#5524bf]",

  EN_CURSO: "bg-[#d9f5e5] text-[#187442]",

  FINALIZADO: "bg-gray-200 text-[#373d4d]",

  CANCELADO: "bg-red-100 text-red-700",

};

export default function CheckinSelector() {

  // CONSULTAR EVENTOS REALES DEL BACKEND

  const {
    datos: eventos,
    cargando,
    error,
  } = useConsulta(obtenerCatalogo);

  return (

    <Pagina titulo="Check-in">

      {/* INTRODUCCIÓN */}

      <div className="mb-8">

        <h2 className="
          font-inter-bold font-bold!
          text-[26px] sm:text-[30px]
          text-[#260477]
        ">

          Selecciona un evento

        </h2>

        <p className="
          mt-2 font-inter-semibold
          text-[17px] leading-relaxed
          text-[#69529b]
        ">

          Selecciona el evento en el que deseas realizar
          la validación de boletas y el control de ingreso.

        </p>

      </div>

      {/* ESTADO DE CARGA Y ERRORES */}

      <EstadoCarga
        cargando={cargando}
        error={error}
      />

      {/* NO HAY EVENTOS DISPONIBLES */}

      {!cargando && !error && !eventos?.length && (

        <section className="
          mx-auto flex max-w-[650px]
          flex-col items-center gap-5
          rounded-[40px]
          bg-white p-8 sm:p-10
          text-center shadow-sm
        ">

          <div className="
            flex size-[100px]
            items-center justify-center
            rounded-full
            bg-[#e9e1f8] text-[#69529b]
          ">

            <Icono tipo="qr" size={48} />

          </div>

          <h2 className="
            font-inter-bold font-bold!
            text-[26px] text-[#260477]
          ">

            No hay eventos disponibles

          </h2>

          <p className="
            font-inter-semibold
            text-[17px] leading-relaxed
            text-[#69529b]
          ">

            Actualmente no hay eventos disponibles
            en el catálogo para realizar el Check-in.
            Vuelve a consultar más tarde.

          </p>

        </section>

      )}

      {/* LISTADO DE EVENTOS */}

      {!cargando && !error && (

        <section
          className="
            grid grid-cols-1
            gap-6
            sm:grid-cols-2
            xl:grid-cols-4
          "
          aria-label="Eventos disponibles para check-in"
        >

          {eventos?.map((evento) => (

            <article
              key={evento.id}
              className="
                flex min-w-0 flex-col
                overflow-hidden
                rounded-[35px]
                border-2 border-[#9682b8]
                bg-white shadow-sm
                transition-all duration-200
                hover:-translate-y-1
                hover:shadow-lg
              "
            >

              {/* ENCABEZADO MORADO */}

              <header className="
                flex min-h-[190px]
                flex-col items-center
                justify-center gap-4
                bg-[#69529b] p-6
                text-center text-white
              ">

                <div className="
                  flex size-[70px]
                  items-center justify-center
                  rounded-[25px]
                  bg-white/20
                ">

                  <Icono tipo="qr" size={38} />

                </div>

                <h2 className="
                  font-inter-bold font-bold!
                  text-[24px] leading-snug
                  break-words
                ">

                  {evento.nombre}

                </h2>

              </header>

              {/* INFORMACIÓN DEL EVENTO */}

              <div className="
                flex flex-1 flex-col
                gap-4 p-5
              ">

                {/* ESTADO DEL EVENTO */}

                <span className={`
                  w-fit rounded-full
                  px-4 py-2
                  font-inter-bold
                  text-[13px]
                  ${coloresEstado[evento.estado]
                    || "bg-[#e9e1f8] text-[#5524bf]"}
                `}>

                  {evento.estado?.replaceAll("_", " ")
                    || "Sin estado"}

                </span>

                {/* UBICACIÓN */}

                <div className="
                  flex items-start gap-3
                  rounded-[25px]
                  bg-[#f5f2fa] p-4
                ">

                  <div className="
                    shrink-0 text-[#69529b]
                  ">

                    <Icono tipo="ubicacion" />

                  </div>

                  <div className="min-w-0">

                    <p className="
                      font-inter-semibold
                      text-[13px] text-[#69529b]
                    ">

                      Lugar

                    </p>

                    <p className="
                      mt-1 break-words
                      font-inter-bold
                      text-[16px] text-[#0f0140]
                    ">

                      {evento.lugar || "No disponible"}

                    </p>

                  </div>

                </div>

                {/* FECHA */}

                <div className="
                  flex items-start gap-3
                  rounded-[25px]
                  bg-[#f5f2fa] p-4
                ">

                  <div className="
                    shrink-0 text-[#69529b]
                  ">

                    <Icono tipo="calendario" />

                  </div>

                  <div className="min-w-0">

                    <p className="
                      font-inter-semibold
                      text-[13px] text-[#69529b]
                    ">

                      Fecha del evento

                    </p>

                    <p className="
                      mt-1 font-inter-bold
                      text-[15px] text-[#0f0140]
                    ">

                      {formatearFecha(evento.fechaInicio)}

                    </p>

                  </div>

                </div>

                {/* AFORO */}

                <div className="
                  flex items-start gap-3
                  rounded-[25px]
                  bg-[#f5f2fa] p-4
                ">

                  <div className="
                    shrink-0 text-[#69529b]
                  ">

                    <Icono tipo="personas" />

                  </div>

                  <div>

                    <p className="
                      font-inter-semibold
                      text-[13px] text-[#69529b]
                    ">

                      Aforo máximo

                    </p>

                    <p className="
                      mt-1 font-inter-bold
                      text-[17px] text-[#0f0140]
                    ">

                      {evento.aforoMaximo != null
                        ? `${evento.aforoMaximo} personas`
                        : "No disponible"}

                    </p>

                  </div>

                </div>

                {/* BOTÓN CHECK-IN */}

                <Link
                  to={`/checkin/${evento.id}`}
                  className={`${buttonStyle}
                    mt-auto w-full gap-2
                  `}
                >

                  <Icono tipo="qr" size={21} />

                  Abrir Check-in

                </Link>

              </div>

            </article>

          ))}

        </section>

      )}

    </Pagina>

  );

}
