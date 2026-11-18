import { Link } from "react-router-dom";

import Pagina from "../../components/Pagina";
import { EVENTOS_PRUEBA } from "../../data/eventosPrueba";

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

      {tipo === "info" && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M12 11v6M12 7h.01" />
        </>
      )}

    </svg>

  );

}

// FORMATO DE FECHA

function formatearFecha(fecha) {

  return new Date(fecha).toLocaleString("es-CO", {
    day: "numeric",
    month: "long",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });

}

export default function CheckinSelector() {

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

      {/* AVISO DE DEMOSTRACIÓN */}

      <section className="
        mb-8 flex items-start gap-4
        rounded-[25px]
        border-2 border-[#d5c9e7]
        bg-[#f5f2fa] p-5
      ">

        <div className="
          flex size-[45px] shrink-0
          items-center justify-center
          rounded-full bg-[#e9e1f8]
          text-[#5524bf]
        ">

          <Icono tipo="info" />

        </div>

        <div>

          <h3 className="
            font-inter-bold text-[18px]
            text-[#260477]
          ">

            Modo de demostración

          </h3>

          <p className="
            mt-1 font-inter-semibold
            text-[15px] text-[#69529b]
          ">

            Estos eventos son de prueba. El listado de
            eventos asignados al Staff y la validación
            real de boletas están pendientes de la
            integración del backend (#24).

          </p>

        </div>

      </section>

      {/* LISTADO DE EVENTOS */}

      <section className="
        grid grid-cols-1
        gap-6
        sm:grid-cols-2
        xl:grid-cols-4
      ">

        {EVENTOS_PRUEBA.map((evento) => (

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
              ">

                {evento.nombre}

              </h2>

            </header>

            {/* INFORMACIÓN DEL EVENTO */}

            <div className="
              flex flex-1 flex-col
              gap-4 p-5
            ">

              {/* ESTADO */}

              <span className="
                w-fit rounded-full
                bg-[#e9e1f8] px-4 py-2
                font-inter-bold
                text-[13px] text-[#5524bf]
              ">

                Evento de prueba

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

                    {evento.lugar}

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

                <div>

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

                    {evento.aforoMaximo} personas

                  </p>

                </div>

              </div>

              {/* BOTÓN CHECK-IN */}

              <Link
                to={`/checkin/${evento.id}`}
                className={`${buttonStyle} mt-auto w-full gap-2`}
              >

                <Icono tipo="qr" size={21} />

                Abrir Check-in

              </Link>

            </div>

          </article>

        ))}

      </section>

    </Pagina>

  );

}