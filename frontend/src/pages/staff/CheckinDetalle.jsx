
import { useState } from "react";
import { Link, useParams } from "react-router-dom";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";

import { registrarCheckIn } from "../../api/boleteria";
import { obtenerEvento } from "../../api/eventos";
import { useSesion } from "../../sesion/Sesion";
import useConsulta from "../../api/useConsulta";

// ESTILO GENERAL DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! border-0! px-7! py-3! " +
  "font-inter-bold font-bold! text-[18px] " +
  "text-white! no-underline! cursor-pointer " +
  "transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-md " +
  "active:scale-95 disabled:opacity-50 " +
  "disabled:cursor-not-allowed";

// COLORES DE LOS ESTADOS

const estados = {
  pendiente: {
    texto: "Pendiente de validación",
    color: "bg-[#260477]",
  },

  correcto: {
    texto: "¡Acceso correcto!",
    color: "bg-[#36ED32]",
  },

  usada: {
    texto: "¡Boleta ya usada!",
    color: "bg-[#EF2424]",
  },

  incorrecto: {
    texto: "¡Acceso incorrecto!",
    color: "bg-[#EF2424]",
  },
};

export default function CheckinDetalle() {

  const { eventoId } = useParams();
  const { usuario } = useSesion();

  // CONSULTAR EL EVENTO REAL

  const {
    datos: evento,
    cargando,
    error,
  } = useConsulta(obtenerEvento, eventoId);

  // ESTADOS DEL CHECK-IN

  const [estado, setEstado] = useState("pendiente");
  const [horaIngreso, setHoraIngreso] = useState("");
  const [codigoQr, setCodigoQr] = useState("");

  const [motivo, setMotivo] = useState("");
  const [categoria, setCategoria] = useState("");
  const [ocupado, setOcupado] = useState(false);

  const bloqueado =
    ocupado || cargando || !!error || !evento || !usuario;

  // VALIDAR BOLETA CON EL BACKEND

  async function validarBoleta(e) {

    e.preventDefault();

    if (bloqueado) return;

    // LIMPIAR RESULTADOS ANTERIORES

    setCategoria("");
    setHoraIngreso("");
    setMotivo("");
    setEstado("pendiente");

    // COMPROBAR EL CÓDIGO QR

    if (!codigoQr.trim()) {

      setMotivo("Escanea o pega el código QR.");
      setEstado("incorrecto");

      return;
    }

    setOcupado(true);

    try {

      // ENVIAR EL QR Y EL ID DEL STAFF

      const r = await registrarCheckIn({
        codigoQr: codigoQr.trim(),
        staffId: usuario.id,
      });

      // ACCESO CORRECTO

      setCategoria(r.categoria || "");

      setHoraIngreso(
        new Date().toLocaleTimeString("es-CO", {
          hour: "2-digit",
          minute: "2-digit",
        })
      );

      setMotivo("");
      setEstado("correcto");

    } catch (err) {

      // BOLETA RECHAZADA

      if (err.status === 409) {

        const mensaje =
          err.motivo ||
          err.detail ||
          "Boleta rechazada.";

        setMotivo(mensaje);

        setEstado(
          mensaje.toLowerCase().includes("ya fue usada")
            ? "usada"
            : "incorrecto"
        );

      } else {

        setMotivo(
          err.detail ||
          "No se pudo validar. Revisa la conexión."
        );

        setEstado("incorrecto");
      }

    } finally {

      setOcupado(false);

    }

  }

  // REINICIAR LA VALIDACIÓN

  function reiniciar() {

    if (ocupado) return;

    setCodigoQr("");
    setHoraIngreso("");
    setEstado("pendiente");
    setMotivo("");
    setCategoria("");

  }

  return (

    <Pagina titulo="Check-in">

      <EstadoCarga
        cargando={cargando}
        error={error}
      />

      {/* CONTENIDO PRINCIPAL */}

      <div className="mx-auto max-w-[1050px]">

        {/* IDENTIFICACIÓN DEL EVENTO */}

        <p className="
          mb-8 break-words
          font-inter-semibold font-semibold!
          text-[20px] text-[#69529b]
        ">

          Evento: {evento?.nombre || eventoId}

        </p>

        {/* CONTENEDOR DEL CHECK-IN */}

        <section className="
          grid grid-cols-1
          items-start gap-8
          md:grid-cols-[1fr_1.3fr]
        ">

          {/* APARTADO DEL QR */}

          <div className="flex flex-col gap-5">

            <div className="
              flex flex-col items-center
              justify-center gap-5
              rounded-[40px]
              border-2 border-[#9682b8]
              bg-white p-8 shadow-sm
            ">

              <h2 className="
                font-inter-bold font-bold!
                text-[25px] text-[#260477]
              ">

                Código QR

              </h2>

              {/* ESPACIO REPRESENTATIVO DEL ESCÁNER */}

              <div className="
                relative flex aspect-square
                w-full max-w-[280px]
                items-center justify-center
                rounded-[30px]
                bg-[#f5f2fa]
              ">

                {/* ESQUINAS DEL ESCÁNER */}

                <div className="
                  absolute left-4 top-4
                  h-12 w-12 rounded-tl-[18px]
                  border-l-4 border-t-4
                  border-[#5524bf]
                " />

                <div className="
                  absolute right-4 top-4
                  h-12 w-12 rounded-tr-[18px]
                  border-r-4 border-t-4
                  border-[#5524bf]
                " />

                <div className="
                  absolute bottom-4 left-4
                  h-12 w-12 rounded-bl-[18px]
                  border-b-4 border-l-4
                  border-[#5524bf]
                " />

                <div className="
                  absolute bottom-4 right-4
                  h-12 w-12 rounded-br-[18px]
                  border-b-4 border-r-4
                  border-[#5524bf]
                " />

                {/* ICONO REPRESENTATIVO DEL QR */}

                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="120"
                  height="120"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="#5524bf"
                  strokeWidth="1.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >

                  <rect x="3" y="3" width="7" height="7" rx="1" />
                  <rect x="14" y="3" width="7" height="7" rx="1" />
                  <rect x="3" y="14" width="7" height="7" rx="1" />

                  <path d="M6 6h1v1H6z" />
                  <path d="M17 6h1v1h-1z" />
                  <path d="M6 17h1v1H6z" />
                  <path d="M14 14h3v3h-3z" />
                  <path d="M21 14v3M14 21h3M21 21v-1" />

                </svg>

              </div>

              <p className="
                text-center font-inter-semibold
                text-[15px] text-[#69529b]
              ">

                Ingresa el código QR de la boleta
                para validar el acceso al evento.

              </p>

            </div>

            {/* INGRESO MANUAL DEL CÓDIGO */}

            <form
              onSubmit={validarBoleta}
              className="flex flex-col gap-3"
            >

              <label
                htmlFor="codigoQr"
                className="font-inter-bold text-[17px]"
              >

                Código de la boleta

              </label>

              <input
                id="codigoQr"
                type="text"
                value={codigoQr}
                disabled={bloqueado}
                autoComplete="off"
                spellCheck={false}
                onChange={(e) => {

                  setCodigoQr(e.target.value);
                  setEstado("pendiente");
                  setHoraIngreso("");
                  setMotivo("");
                  setCategoria("");

                }}
                placeholder="Pega el código QR de la boleta"
                className="
                  w-full rounded-full!
                  border-2! border-solid!
                  border-[#9682b8]!
                  bg-white! px-5! py-3!
                  font-inter-semibold font-semibold!
                  text-[16px] text-[#0f0140]
                  outline-none transition-all
                  hover:border-[#69529b]!
                  focus:border-[#5524bf]!
                  focus:ring-2 focus:ring-[#5524bf]/20
                  disabled:opacity-50
                  disabled:cursor-not-allowed
                "
              />

              <button
                type="submit"
                disabled={bloqueado}
                className={`${buttonStyle}
                  bg-[#5524bf]!
                  hover:bg-[#260477]!
                `}
              >

                {ocupado
                  ? "Validando boleta..."
                  : "Validar boleta"}

              </button>

            </form>

          </div>

          {/* INFORMACIÓN DE LA BOLETA */}

          <article className="
            flex flex-col gap-5
            rounded-[45px]
            bg-[#69529b]
            p-6 sm:p-8
            text-white shadow-lg
          ">

            <h2 className="
              text-center
              font-inter-bold font-bold!
              text-[28px]
            ">

              Información de la boleta

            </h2>

            <hr className="border-white/30" />

            {/* NOMBRE DEL EVENTO */}

            <div className="rounded-[25px] bg-white/15 p-4">

              <p className="
                font-inter-bold
                text-[15px] text-white/80
              ">

                Evento seleccionado

              </p>

              <p className="
                mt-1 font-inter-bold
                text-[22px] text-white
              ">

                {evento?.nombre || "—"}

              </p>

            </div>

            {/* INFORMACIÓN GENERAL */}

            <dl className="
              grid grid-cols-1
              gap-3 sm:grid-cols-2
            ">

              {/* LUGAR */}

              <div className="rounded-[25px] bg-white/15 p-4">

                <dt className="
                  font-inter-semibold
                  text-[14px] text-white/80
                ">

                  Lugar

                </dt>

                <dd className="
                  mt-1 font-inter-bold text-[18px]
                  break-words
                ">

                  {evento?.lugar || "—"}

                </dd>

              </div>

              {/* FECHA */}

              <div className="rounded-[25px] bg-white/15 p-4">

                <dt className="
                  font-inter-semibold
                  text-[14px] text-white/80
                ">

                  Fecha del evento

                </dt>

                <dd className="
                  mt-1 font-inter-bold text-[18px]
                ">

                  {evento?.fechaInicio
                    ? new Date(evento.fechaInicio)
                        .toLocaleString("es-CO")
                    : "—"}

                </dd>

              </div>

              {/* TIPO DE BOLETA */}

              <div className="rounded-[25px] bg-white/15 p-4">

                <dt className="
                  font-inter-semibold
                  text-[14px] text-white/80
                ">

                  Tipo de boleta

                </dt>

                <dd className="
                  mt-1 font-inter-bold text-[18px]
                ">

                  {estado === "correcto"
                    ? categoria || "—"
                    : "—"}

                </dd>

              </div>

              {/* HORA DE INGRESO */}

              <div className="rounded-[25px] bg-white/15 p-4">

                <dt className="
                  font-inter-semibold
                  text-[14px] text-white/80
                ">

                  Hora de ingreso

                </dt>

                <dd className="
                  mt-1 font-inter-bold text-[18px]
                ">

                  {horaIngreso || "—"}

                </dd>

              </div>

            </dl>

            {/* ESTADO DE LA BOLETA */}

            <div className="mt-2 flex flex-col gap-3">

              <h3 className="
                text-center font-inter-bold
                text-[20px]
              ">

                Estado de validación

              </h3>

              <div
                role="status"
                className={`
                  w-full rounded-full
                  px-5 py-4 text-center
                  font-inter-bold font-bold!
                  text-[20px]
                  transition-colors duration-300
                  ${estados[estado].color}
                  ${estado === "correcto"
                    ? "text-[#0f4010]"
                    : "text-white"}
                `}
              >

                {ocupado
                  ? "Validando..."
                  : estados[estado].texto}

              </div>

              {/* MOTIVO DEL RECHAZO */}

              {(estado === "incorrecto" ||
                estado === "usada") &&
                motivo && (

                <div
                  role="alert"
                  className="
                    rounded-[25px]
                    border-2 border-red-200
                    bg-red-50 p-4
                    text-center
                    font-inter-semibold
                    font-semibold!
                    text-[16px]
                    text-red-700
                  "
                >

                  {motivo}

                </div>

              )}

            </div>

            {/* REINICIAR VALIDACIÓN */}

            <button
              type="button"
              onClick={reiniciar}
              disabled={ocupado}
              className={`${buttonStyle}
                mt-2 w-full
                bg-[#260477]!
                hover:bg-[#070024]!
              `}
            >

              Nueva validación

            </button>

          </article>

        </section>

        {/* BOTÓN VOLVER */}

        <div className="mt-8">

          <Link
            to="/checkin"
            className={`${buttonStyle}
              bg-[#260477]!
              hover:bg-[#070024]!
            `}
          >

            ← Volver a selección

          </Link>

        </div>

      </div>

    </Pagina>

  );

}
