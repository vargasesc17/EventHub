import { Link } from "react-router-dom";

import Pagina from "../../components/Pagina";
import { useSesion } from "../../sesion/Sesion";
import { obtenerMisBoletas } from "../../storage/misBoletas";
import { EVENTOS_PRUEBA } from "../../data/eventosPrueba";

// ESTILO GENERAL DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! border-0! px-6! py-3! " +
  "font-inter-bold font-bold! text-[17px] " +
  "text-white! no-underline! " +
  "transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-lg " +
  "active:scale-95";

// COLORES SEGÚN LA CATEGORÍA

const coloresCategorias = {
  GENERAL: "bg-[#69529b]",
  VIP: "bg-[#260477]",
  ESTUDIANTE: "bg-[#298496]",
  CORTESIA: "bg-[#373d4d]",
};

// FORMATO DE MONEDA

const formatoPrecio = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0,
});

export default function Boletas() {

  const { usuario } = useSesion();

  let boletas = [];
  let error;

  // CONSULTAR LAS BOLETAS DEL COMPRADOR

  try {

    boletas = obtenerMisBoletas(usuario.id);

  } catch (e) {

    error = e.message;

  }

  return (

    <Pagina titulo="Mis boletas">

      {/* MENSAJE DE ERROR */}

      {error && (

        <p
          role="alert"
          className="rounded-[25px] bg-red-100 p-5
          font-inter-semibold text-red-700"
        >
          {error}
        </p>

      )}

      {/* CUANDO NO HAY BOLETAS */}

      {!error && !boletas.length && (

        <section className="eh-panel rounded-[40px]!
          mx-auto max-w-[650px] p-10!
          flex flex-col items-center gap-5 text-center">

          {/* ICONO DE BOLETA */}

          <div className="flex h-[100px] w-[100px]
            items-center justify-center
            rounded-full bg-[#e9e1f8]">

            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="52"
              height="52"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#69529b"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M4 4h16a2 2 0 0 1 2 2v3a3 3 0 0 0 0 6v3a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-3a3 3 0 0 0 0-6V6a2 2 0 0 1 2-2Z" />
              <path d="M13 5v2m0 4v2m0 4v2" />
            </svg>

          </div>

          <h2 className="font-inter-bold font-bold!
            text-[28px] text-[#260477]">

            Aún no tienes boletas

          </h2>

          <p className="font-inter-semibold
            text-[17px] text-[#69529b]">

            No tienes compras guardadas en este navegador.
            Explora los eventos disponibles y encuentra
            tu próximo evento.

          </p>

          <Link
            to="/eventos"
            className={`${buttonStyle}
              bg-[#5524bf]! hover:bg-[#260477]!`}
          >
            Explorar eventos
          </Link>

        </section>

      )}

      {/* RESUMEN DE BOLETAS */}

      {boletas.length > 0 && (

        <div className="mb-6 flex flex-wrap
          items-center justify-between gap-4">

          <p className="font-inter-semibold
            font-semibold! text-[20px]">

            Tus boletas:{" "}
            <span className="text-[#5524bf]
              font-inter-bold">

              {boletas.length}

            </span>

          </p>

          <Link
            to="/eventos"
            className={`${buttonStyle}
              bg-[#69529b]! hover:bg-[#493778]!`}
          >

            Explorar más eventos

          </Link>

        </div>

      )}

      {/* TARJETAS DE BOLETAS */}

      <section className="grid grid-cols-1
        gap-6 sm:grid-cols-2 lg:grid-cols-3">

        {boletas.map((boleta) => {

          const id = boleta.boletaId || boleta.id;

          const eventoPrueba = EVENTOS_PRUEBA.find(
            (e) => e.id === boleta.eventoId
          );

          const nombreEvento =
            boleta.nombreEvento ||
            eventoPrueba?.nombre ||
            "Boleta de evento";

          return (

            <article
              key={id}
              className="flex flex-col overflow-hidden
              rounded-[35px] border-2 border-[#9682b8]
              bg-white shadow-sm transition-all duration-200
              hover:-translate-y-1 hover:shadow-lg"
            >

              {/* ENCABEZADO */}

              <header
                className={`${
                  coloresCategorias[boleta.categoria] ||
                  "bg-[#260477]"
                } p-6 text-white`}
              >

                <p className="font-inter-semibold
                  text-[13px] tracking-widest uppercase">

                  EventHub · Boleta digital

                </p>

                <h2 className="mt-3 font-inter-bold
                  font-bold! text-[25px]">

                  {nombreEvento}

                </h2>

                <span className="mt-4 inline-block
                  rounded-full border-2 border-white/50
                  bg-white/20 px-5 py-2
                  font-inter-semibold text-[14px]">

                  {boleta.categoria}

                </span>

              </header>

              {/* INFORMACIÓN */}

              <div className="flex flex-1 flex-col
                gap-4 p-6">

                {/* PRECIO PAGADO */}

                <div className="rounded-[25px]
                  border-2 border-[#d5c9e7]
                  bg-[#f5f2fa] p-4">

                  <p className="font-inter-semibold
                    text-[14px] text-[#69529b]">

                    Precio pagado

                  </p>

                  <p className="font-inter-bold
                    font-bold! text-[26px] text-[#260477]">

                    {boleta.precioPagado != null
                      ? formatoPrecio.format(
                          boleta.precioPagado
                        )
                      : "No disponible"}

                  </p>

                </div>

                {/* ESTADO */}

                <div className="rounded-[25px]
                  border-2 border-[#d5c9e7]
                  bg-[#f5f2fa] p-4">

                  <p className="font-inter-semibold
                    text-[14px] text-[#69529b]">

                    Estado de la boleta

                  </p>

                  <p className="font-inter-bold
                    text-[18px] text-[#0f0140]">

                    {boleta.estado || "No disponible"}

                  </p>

                </div>

                {/* FECHA DE COMPRA */}

                <div className="rounded-[25px]
                  border-2 border-[#d5c9e7]
                  bg-[#f5f2fa] p-4">

                  <p className="font-inter-semibold
                    text-[14px] text-[#69529b]">

                    Fecha de compra

                  </p>

                  <p className="font-inter-semibold
                    text-[16px]">

                    {boleta.fechaCompra
                      ? new Date(
                          boleta.fechaCompra
                        ).toLocaleString("es-CO")
                      : "No disponible"}

                  </p>

                </div>

              </div>

              {/* SEPARADOR ESTILO BOLETA */}

              <div className="relative mx-6
                border-t-2 border-dashed border-[#9682b8]">

                <div className="absolute -left-10 -top-4
                  h-8 w-8 rounded-full bg-[#e5e5e7]" />

                <div className="absolute -right-10 -top-4
                  h-8 w-8 rounded-full bg-[#e5e5e7]" />

              </div>

              {/* BOTÓN VER BOLETA */}

              <div className="flex flex-col
                items-center gap-4 p-6">

                <p className="w-full break-all text-center
                  text-[12px] text-[#69529b]">

                  ID: {id}

                </p>

                <Link
                  to={`/boletas/${id}`}
                  className={`${buttonStyle}
                    w-full bg-[#5524bf]!
                    hover:bg-[#260477]!`}
                >

                  Ver mi boleta

                </Link>

                <p className="text-center
                  font-inter-semibold
                  text-[13px] text-[#69529b]">

                  {boleta.codigoQr
                    ? "Consulta los detalles y tu código QR"
                    : "Consulta los detalles de tu boleta"}

                </p>

              </div>

            </article>

          );

        })}

      </section>

    </Pagina>

  );
}