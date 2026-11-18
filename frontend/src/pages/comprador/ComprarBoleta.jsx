import { useState } from "react";
import { Link, useParams } from "react-router-dom";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";

import { obtenerEvento } from "../../api/eventos";
import { obtenerAforo, comprarBoleta } from "../../api/boleteria";
import useConsulta from "../../api/useConsulta";
import { guardarMiBoleta } from "../../storage/misBoletas";

import { useSesion } from "../../sesion/Sesion";

// ESTILO DE LOS CAMPOS

const inputStyle =
  "w-full rounded-full! border-2! border-solid! " +
  "border-[#9682b8]! bg-white! px-5 py-3 " +
  "font-inter-semibold font-semibold! text-[16px] " +
  "text-[#0f0140] outline-none transition-all " +
  "hover:border-[#69529b]! focus:border-[#5524bf]! " +
  "focus:ring-2 focus:ring-[#5524bf]/20";

// ESTILO DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! px-8! py-3! border-0! " +
  "font-inter-bold font-bold! text-[18px] " +
  "text-white! no-underline! transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-md " +
  "disabled:opacity-50 disabled:cursor-not-allowed";

// FORMATO DE PRECIOS

const formatoPrecio = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0,
});

export default function ComprarBoleta() {

  const { eventoId } = useParams();
  const { usuario } = useSesion();

  const [categoria, setCategoria] = useState("");
  const [carnet, setCarnet] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [errorCompra, setErrorCompra] = useState("");
  const [boleta, setBoleta] = useState(null);

  // CONSULTAR EVENTO REAL DEL BACKEND

  const {
    datos: evento,
    cargando,
    error,
  } = useConsulta(obtenerEvento, eventoId);

  // CONSULTAR AFORO REAL DEL BACKEND

  const {
    datos: aforo,
    cargando: cargandoAforo,
    error: errorAforo,
  } = useConsulta(obtenerAforo, eventoId);

  const categoriaSeleccionada = evento?.categorias?.find(
    (c) => c.tipo === categoria
  );

  // CONFIRMAR COMPRA

  async function confirmarCompra(e) {

    e.preventDefault();

    if (ocupado || boleta) return;

    setErrorCompra("");

    // VALIDAR EL ESTADO DEL EVENTO

    if (!evento || evento.estado !== "PUBLICADO") {

      setErrorCompra("Este evento no está disponible.");
      return;

    }

    // VALIDAR LA CONSULTA DE AFORO

    if (cargandoAforo || errorAforo || !aforo) {

      setErrorCompra(
        "No se pudo consultar la disponibilidad de boletas."
      );

      return;

    }

    // VALIDAR LA CATEGORÍA

    if (!categoriaSeleccionada || categoria === "CORTESIA") {

      setErrorCompra("Selecciona una categoría válida.");
      return;

    }

    // VALIDAR EL CARNET ESTUDIANTIL

    if (categoria === "ESTUDIANTE" && !carnet.trim()) {

      setErrorCompra("Debes ingresar tu carnet estudiantil.");
      return;

    }

    setOcupado(true);

    try {

      // DATOS DE LA COMPRA

      const datosCompra = {
        eventoId: evento.id,
        categoria,
        compradorId: usuario.id,
        nombreComprador: usuario.nombre,
        correoComprador: usuario.correo,

        ...(categoria === "ESTUDIANTE"
          ? { carnetEstudiantil: carnet.trim() }
          : {}),
      };

      // ENVIAR COMPRA AL BACKEND

      const resultado = await comprarBoleta(datosCompra);

      setBoleta(resultado);

      // GUARDAR LA BOLETA EN EL NAVEGADOR
      // No existe un endpoint para listar las compras del usuario.

      guardarMiBoleta(usuario.id, {
        ...resultado,
        id: resultado.boletaId,
        nombreEvento: evento.nombre,
        nombreComprador: usuario.nombre,
      });

    } catch (err) {

      setErrorCompra(
        err?.detail ||
        err?.message ||
        "No se pudo completar la compra."
      );

    } finally {

      setOcupado(false);

    }

  }

  return (

    <Pagina titulo="Comprar boleta">

      <EstadoCarga
        cargando={cargando}
        error={error}
      />

      {/* REGRESAR AL EVENTO */}

      <Link
        to={`/eventos/${eventoId}`}
        className={`${buttonStyle}
          bg-[#69529b]! hover:bg-[#493778]! mb-6`}
      >

        ← Volver al evento

      </Link>

      {evento && (

        <div className="mx-auto max-w-[1000px]">

          {/* INFORMACIÓN DEL EVENTO */}

          <section className="eh-panel rounded-[40px]! p-8! mb-6">

            <h2 className="font-inter-bold
              text-[30px] text-[#260477]">

              {evento.nombre}

            </h2>

            <p className="mt-3 font-inter-semibold text-[18px]">

              Lugar: {evento.lugar}

            </p>

            <p className="mt-2 font-inter-semibold">

              Fecha:{" "}
              {new Date(evento.fechaInicio).toLocaleString("es-CO")}

            </p>

          </section>

          <form
            onSubmit={confirmarCompra}
            className="grid grid-cols-1
              lg:grid-cols-[1.3fr_1fr] gap-6"
          >

            {/* FORMULARIO DE COMPRA */}

            <section className="eh-panel rounded-[40px]!
              p-8! flex flex-col gap-5">

              <h2 className="font-inter-bold
                text-[26px] text-[#260477]">

                Información de compra

              </h2>

              {/* NOMBRE */}

              <label className="flex flex-col gap-2
                font-inter-semibold">

                Nombre del comprador

                <input
                  className={inputStyle}
                  value={usuario?.nombre || ""}
                  readOnly
                />

              </label>

              {/* CORREO */}

              <label className="flex flex-col gap-2
                font-inter-semibold">

                Correo electrónico

                <input
                  className={inputStyle}
                  value={usuario?.correo || ""}
                  readOnly
                />

              </label>

              {/* CATEGORÍA */}

              <label className="flex flex-col gap-2
                font-inter-semibold">

                Categoría de boleta

                <select
                  className={inputStyle}
                  value={categoria}
                  onChange={(e) => setCategoria(e.target.value)}
                  disabled={ocupado || Boolean(boleta)}
                  required
                >

                  <option value="">

                    Selecciona una categoría

                  </option>

                  {evento.categorias
                    ?.filter((c) => c.tipo !== "CORTESIA")
                    .map((c) => (

                      <option key={c.tipo} value={c.tipo}>

                        {c.tipo} — {formatoPrecio.format(c.precioBase)}

                      </option>

                    ))}

                </select>

              </label>

              {/* CARNET ESTUDIANTIL */}

              {categoria === "ESTUDIANTE" && (

                <label className="flex flex-col gap-2
                  font-inter-semibold">

                  Carnet estudiantil

                  <input
                    className={inputStyle}
                    placeholder="Número de carnet"
                    value={carnet}
                    onChange={(e) => setCarnet(e.target.value)}
                    disabled={ocupado || Boolean(boleta)}
                    required
                  />

                </label>

              )}

            </section>

            {/* RESUMEN DE COMPRA */}

            <section className="eh-panel rounded-[40px]!
              p-8! flex flex-col gap-5">

              <h2 className="font-inter-bold
                text-[26px] text-[#260477]">

                Resumen de compra

              </h2>

              <div className="rounded-[30px]
                border-2 border-[#d5c9e7]
                bg-[#f5f2fa] p-6">

                <p className="font-inter-semibold">

                  Evento

                </p>

                <p className="mb-4 font-inter-bold">

                  {evento.nombre}

                </p>

                <p className="font-inter-semibold">

                  Categoría

                </p>

                <p className="mb-4 font-inter-bold">

                  {categoria || "Sin seleccionar"}

                </p>

                <p className="font-inter-semibold">

                  Cantidad de boletas

                </p>

                <p className="font-inter-bold">

                  1

                </p>

                <hr className="my-5 border-[#d5c9e7]" />

                <p className="font-inter-semibold text-[18px]">

                  Precio base

                </p>

                <p className="font-inter-bold
                  text-[34px] text-[#5524bf]">

                  {formatoPrecio.format(
                    categoriaSeleccionada?.precioBase || 0
                  )}

                </p>

              </div>

              {/* DISPONIBILIDAD REAL */}

              <EstadoCarga
                cargando={cargandoAforo}
                error={errorAforo}
              />

              {aforo && !cargandoAforo && !errorAforo && (

                <p className="font-inter-semibold text-[#206b79]">

                  Consulta de aforo realizada.
                  La disponibilidad final se valida al comprar.

                </p>

              )}

              {/* MENSAJE DE ERROR */}

              {errorCompra && (

                <p
                  role="alert"
                  className="rounded-[20px] bg-red-100
                    p-4 font-semibold text-red-700"
                >

                  {errorCompra}

                </p>

              )}

              {/* CONFIRMAR COMPRA */}

              <button
                type="submit"
                disabled={
                  ocupado ||
                  !categoriaSeleccionada ||
                  cargandoAforo ||
                  Boolean(errorAforo) ||
                  !aforo ||
                  Boolean(boleta) ||
                  evento.estado !== "PUBLICADO"
                }
                className={`${buttonStyle}
                  w-full bg-[#5524bf]!
                  hover:bg-[#260477]!`}
              >

                {ocupado
                  ? "Procesando..."
                  : boleta
                    ? "Compra realizada"
                    : "Confirmar compra"}

              </button>

              {/* COMPRA EXITOSA */}

              {boleta && (

                <div className="rounded-[25px]
                  bg-green-100 p-5 text-green-800">

                  <h3 className="font-inter-bold text-[20px]">

                    ¡Compra exitosa!

                  </h3>

                  <p className="mt-2 font-inter-semibold break-all">

                    ID de la boleta: {boleta.boletaId}

                  </p>

                  <p className="mt-2">

                    Precio pagado:{" "}
                    {formatoPrecio.format(boleta.precioPagado)}

                  </p>

                </div>

              )}

            </section>

          </form>

        </div>

      )}

    </Pagina>

  );
}