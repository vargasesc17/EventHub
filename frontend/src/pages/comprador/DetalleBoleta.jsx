import { Link, useParams } from "react-router-dom";
import { QRCodeSVG } from "qrcode.react";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";
import { useSesion } from "../../sesion/Sesion";
import { obtenerBoletasDelComprador } from "../../api/boleteria";
import useConsulta from "../../api/useConsulta";

// ESTILO DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! border-0! px-8! py-3! " +
  "font-inter-bold font-bold! text-[18px] " +
  "text-white! no-underline! " +
  "transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-lg " +
  "active:scale-95";

// COLORES DE LAS CATEGORÍAS

const coloresCategorias = {
  GENERAL: "bg-[#69529b]",
  VIP: "bg-[#260477]",
  ESTUDIANTE: "bg-[#298496]",
  CORTESIA: "bg-[#373d4d]",
};

// FORMATO DE PRECIOS

const formatoPrecio = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0,
});

export default function DetalleBoleta() {

  const { boletaId } = useParams();
  const { usuario } = useSesion();

  // BUSCAR LA BOLETA ENTRE LAS DEL COMPRADOR (BOLETERÍA)
  // Solo se encuentra si es de quien inició sesión.

  const { datos, cargando, error } = useConsulta(
    obtenerBoletasDelComprador,
    usuario.id
  );

  const boleta = datos?.find((b) => b.boletaId === boletaId);

  // INFORMACIÓN DEL EVENTO

  const nombreEvento =
    boleta?.nombreEvento || "Información del evento";

  return (

    <Pagina titulo="Detalle de boleta">

      {/* BOTÓN REGRESAR */}

      <Link
        to="/boletas"
        className={`${buttonStyle}
          bg-[#69529b]! hover:bg-[#493778]! mb-6`}
      >
        ← Volver a mis boletas
      </Link>

      {/* CARGANDO O ERROR */}

      <EstadoCarga cargando={cargando} error={error} />

      {cargando || error ? null : !boleta ? (

        <section className="eh-panel rounded-[35px]! p-8!">

          <h2 className="font-inter-bold text-[26px]">
            Boleta no encontrada
          </h2>

          <p className="mt-3 font-inter-semibold">
            No se encontró esta boleta entre tus compras.
          </p>

        </section>

      ) : (

        <article
          className="mx-auto max-w-[650px]
          overflow-hidden rounded-[40px]
          border-2 border-[#9682b8]
          bg-white shadow-lg"
        >

          {/* ENCABEZADO DE LA BOLETA */}

          <header
            className={`${coloresCategorias[boleta.categoria]
              || "bg-[#260477]"}
              px-8 py-8 text-center text-white`}
          >

            <p className="font-inter-semibold
              text-[16px] tracking-widest uppercase">

              EventHub · Boleta digital

            </p>

            <h2 className="mt-3 font-inter-bold
              font-bold! text-[30px]">

              {nombreEvento}

            </h2>

            {(boleta.fechaEvento || boleta.lugar) && (

              <p className="mt-2 font-inter-semibold text-[16px]">

                {[
                  boleta.fechaEvento &&
                    new Date(boleta.fechaEvento).toLocaleString("es-CO"),
                  boleta.lugar,
                ].filter(Boolean).join(" · ")}

              </p>

            )}

            <span className="mt-4 inline-block
              rounded-full border-2 border-white/50
              bg-white/20 px-6 py-2
              font-inter-bold text-[18px]">

              {boleta.categoria}

            </span>

          </header>

          {/* INFORMACIÓN DE LA BOLETA */}

          <section className="flex flex-col gap-5 p-8">

            {/* COMPRADOR */}

            <div className="rounded-[25px]
              border-2 border-[#d5c9e7]
              bg-[#f5f2fa] p-5">

              <p className="font-inter-semibold
                text-[#69529b] text-[14px]">
                Comprador
              </p>

              <p className="font-inter-bold
                text-[20px] text-[#0f0140]">

                {usuario.nombre}

              </p>

            </div>

            {/* INFORMACIÓN GENERAL */}

            <div className="grid grid-cols-1
              sm:grid-cols-2 gap-4">

              {/* CATEGORÍA */}

              <div className="rounded-[25px]
                border-2 border-[#d5c9e7]
                bg-[#f5f2fa] p-5">

                <p className="font-inter-semibold
                  text-[#69529b] text-[14px]">
                  Categoría
                </p>

                <p className="font-inter-bold text-[18px]">
                  {boleta.categoria}
                </p>

              </div>

              {/* PRECIO PAGADO */}

              <div className="rounded-[25px]
                border-2 border-[#d5c9e7]
                bg-[#f5f2fa] p-5">

                <p className="font-inter-semibold
                  text-[#69529b] text-[14px]">
                  Precio pagado
                </p>

                <p className="font-inter-bold text-[18px]">

                  {boleta.precioPagado != null
                    ? formatoPrecio.format(boleta.precioPagado)
                    : "No disponible"}

                </p>

              </div>

              {/* ESTADO */}

              <div className="rounded-[25px]
                border-2 border-[#d5c9e7]
                bg-[#f5f2fa] p-5">

                <p className="font-inter-semibold
                  text-[#69529b] text-[14px]">
                  Estado de la boleta
                </p>

                <p className="font-inter-bold text-[18px]">

                  {boleta.estado || "No disponible"}

                </p>

              </div>

              {/* FECHA DE COMPRA */}

              <div className="rounded-[25px]
                border-2 border-[#d5c9e7]
                bg-[#f5f2fa] p-5">

                <p className="font-inter-semibold
                  text-[#69529b] text-[14px]">
                  Fecha de compra
                </p>

                <p className="font-inter-bold text-[16px]">

                  {boleta.fechaCompra
                    ? new Date(boleta.fechaCompra)
                        .toLocaleString("es-CO")
                    : "No disponible"}

                </p>

              </div>

            </div>

          </section>

          {/* SEPARADOR ESTILO BOLETA */}

          <div className="relative mx-6
            border-t-2 border-dashed border-[#9682b8]">

            <div className="absolute -left-10 -top-4
              h-8 w-8 rounded-full bg-[#e5e5e7]" />

            <div className="absolute -right-10 -top-4
              h-8 w-8 rounded-full bg-[#e5e5e7]" />

          </div>

          {/* CÓDIGO QR */}

          <section className="flex flex-col
            items-center gap-5 p-8 text-center">

            <h2 className="font-inter-bold font-bold!
              text-[26px] text-[#260477]">

              Código de acceso

            </h2>

            {boleta.codigoQr ? (

              <>

                <div className="rounded-[30px]
                  border-2 border-[#9682b8]
                  bg-white p-6 shadow-md">

                  <QRCodeSVG
                    value={boleta.codigoQr}
                    size={256}
                    level="M"
                    marginSize={4}
                    title="Código QR de tu boleta"
                    className="h-auto max-w-full"
                  />

                </div>

                <p className="font-inter-semibold
                  text-[#69529b]">

                  Presenta este código QR al personal
                  de ingreso del evento.
                  {/* El staff no tiene cámara en la demo: el código se copia y se pega en el check-in */}
                  <div className="mt-4 w-full max-w-[420px] rounded-[20px] bg-[#f5f2fa] p-4">
                    <p className="text-[13px] font-semibold text-[#69529b]">
                      Código de la boleta
                    </p>
                    <p className="mt-1 break-all font-mono text-[12px] text-[#0f0140]">
                      {boleta.codigoQr}
                    </p>
                    <button
                        type="button"
                        onClick={() => navigator.clipboard.writeText(boleta.codigoQr)}
                        className="mt-3 rounded-full! bg-[#5524bf]! px-5! py-2!
                      text-[14px] font-bold! text-white! border-0!"
                    >
                      Copiar código
                    </button>
                  </div>

                </p>

              </>

            ) : (

              <p className="rounded-[25px]
                bg-[#e9e1f8] p-5
                font-inter-semibold text-[#69529b]">

                {boleta.estado === "CANCELADA"
                  ? "Esta boleta fue cancelada: ya no sirve para ingresar."
                  : boleta.estado === "USADA"
                  ? "Esta boleta ya se usó para ingresar al evento."
                  : "Esta boleta no tiene un código QR disponible."}

              </p>

            )}

            {/* IDENTIFICADOR DE LA BOLETA */}

            <div className="w-full rounded-[25px]
              bg-[#f5f2fa] p-4">

              <p className="font-inter-semibold
                text-[14px] text-[#69529b]">

                Identificador de la boleta

              </p>

              <p className="mt-2 break-all
                font-inter-semibold text-[13px]">

                {boleta.boletaId}

              </p>

            </div>

            <p className="w-full break-all text-[13px]
              text-[#69529b]">

              ID del evento: {boleta.eventoId}

            </p>

          </section>

        </article>

      )}

    </Pagina>

  );
}