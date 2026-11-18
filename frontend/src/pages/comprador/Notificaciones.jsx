import { Link } from "react-router-dom";

import Pagina from "../../components/Pagina";
import { useSesion } from "../../sesion/Sesion";

// TIPOS DE NOTIFICACIONES INFORMATIVAS

const notificaciones = [
  {
    titulo: "Confirmación de compra",
    descripcion:
      "Información relacionada con la compra de tus boletas.",
    icono: "boleta",
  },
  {
    titulo: "Reprogramación de eventos",
    descripcion:
      "Información sobre cambios en la fecha o el lugar de un evento.",
    icono: "calendario",
  },
  {
    titulo: "Cancelación de eventos",
    descripcion:
      "Avisos relacionados con la cancelación de eventos.",
    icono: "alerta",
  },
];

// ICONOS

function Icono({ tipo }) {

  return (

    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="32"
      height="32"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >

      {tipo === "correo" && (
        <>
          <rect x="2" y="4" width="20" height="16" rx="3" />
          <path d="m2 6 10 7L22 6" />
        </>
      )}

      {tipo === "boleta" && (
        <>
          <path d="M4 4h16a2 2 0 0 1 2 2v3a3 3 0 0 0 0 6v3a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-3a3 3 0 0 0 0-6V6a2 2 0 0 1 2-2Z" />
          <path d="M13 5v2m0 4v2m0 4v2" />
        </>
      )}

      {tipo === "calendario" && (
        <>
          <rect x="3" y="5" width="18" height="16" rx="2" />
          <path d="M16 3v4M8 3v4M3 10h18" />
          <path d="M8 15h2m4 0h2" />
        </>
      )}

      {tipo === "alerta" && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M12 7v6M12 17h.01" />
        </>
      )}

    </svg>

  );
}

export default function Notificaciones() {

  const { usuario } = useSesion();

  return (

    <Pagina titulo="Notificaciones">

      <div className="mx-auto max-w-[1000px]">

        {/* INFORMACIÓN PRINCIPAL */}

        <section className="eh-panel rounded-[40px]!
          border-2! border-[#9682b8]!
          bg-white! p-6! sm:p-10!
          flex flex-col items-center
          gap-5 text-center">

          {/* ICONO DE CORREO */}

          <div className="flex size-[100px]
            items-center justify-center
            rounded-full bg-[#5524bf] text-white">

            <Icono tipo="correo" />

          </div>

          <h2 className="font-inter-bold font-bold!
            text-[28px] sm:text-[34px]
            text-[#260477]">

            Mantente al tanto de tus eventos

          </h2>

          <p className="max-w-[650px]
            font-inter-semibold text-[18px]
            leading-relaxed text-[#69529b]">

            Las notificaciones de EventHub se envían
            directamente a tu correo electrónico.
            Allí podrás consultar los avisos importantes
            relacionados con tus eventos y boletas.

          </p>

          {/* CORREO DEL USUARIO */}

          <div className="w-full max-w-[550px]
            rounded-[25px] border-2 border-[#d5c9e7]
            bg-[#f5f2fa] p-5">

            <p className="font-inter-semibold
              text-[14px] text-[#69529b]">

              Correo asociado a tu cuenta

            </p>

            <p className="mt-2 break-all
              font-inter-bold text-[20px]
              text-[#260477]">

              {usuario?.correo || "Correo no disponible"}

            </p>

          </div>

        </section>

        {/* TIPOS DE NOTIFICACIONES */}

        <section className="mt-8">

          <h2 className="mb-5 font-inter-bold
            font-bold! text-[28px] text-[#0f0140]">

            Avisos relacionados con tus eventos

          </h2>

          <div className="grid grid-cols-1
            gap-5 sm:grid-cols-2 lg:grid-cols-3">

            {notificaciones.map((notificacion) => (

              <article
                key={notificacion.titulo}
                className="flex flex-col gap-4
                  rounded-[30px]
                  border-2 border-[#9682b8]
                  bg-white p-6 shadow-sm
                  transition-all duration-200
                  hover:-translate-y-1 hover:shadow-lg"
              >

                <div className="flex size-[65px]
                  items-center justify-center
                  rounded-full bg-[#e9e1f8]
                  text-[#5524bf]">

                  <Icono tipo={notificacion.icono} />

                </div>

                <h3 className="font-inter-bold
                  font-bold! text-[21px]
                  text-[#260477]">

                  {notificacion.titulo}

                </h3>

                <p className="font-inter-semibold
                  text-[16px] leading-relaxed
                  text-[#69529b]">

                  {notificacion.descripcion}

                </p>

              </article>

            ))}

          </div>

        </section>

        {/* RECOMENDACIONES */}

        <section className="mt-8
          rounded-[30px] border-2 border-[#d5c9e7]
          bg-[#f5f2fa] p-7">

          <h2 className="font-inter-bold
            text-[23px] text-[#260477]">

            Recuerda

          </h2>

          <p className="mt-3 font-inter-semibold
            text-[16px] leading-relaxed
            text-[#69529b]">

            Revisa periódicamente tu bandeja de entrada.
            Si no encuentras un correo esperado,
            consulta también la carpeta de spam o
            correo no deseado.

          </p>

          <p className="mt-3 font-inter-semibold
            text-[16px] text-[#69529b]">

            EventHub no almacena un historial de
            notificaciones en esta pantalla.

          </p>

        </section>

        {/* BOTÓN REGRESAR */}

        <div className="mt-8 flex justify-center">

          <Link
            to="/eventos"
            className="
              inline-flex! items-center! justify-center!
              rounded-full! border-0!
              bg-[#5524bf]! px-8! py-3!
              font-inter-bold font-bold!
              text-[18px] text-white!
              no-underline!
              transition-all duration-200
              hover:bg-[#260477]!
              hover:-translate-y-0.5
              hover:shadow-lg
              active:scale-95
            "
          >

            Volver a eventos

          </Link>

        </div>

      </div>

    </Pagina>

  );
}