import { Link, useParams } from "react-router-dom";

import Pagina from "../../components/Pagina";
import EstadoCarga from "../../components/EstadoCarga";

import { obtenerEvento } from "../../api/eventos";
import { obtenerAforo } from "../../api/boleteria";
import useConsulta from "../../api/useConsulta";

// ESTILO GENERAL DE LOS BOTONES

const buttonStyle =
  "inline-flex! items-center! justify-center! " +
  "rounded-full! px-8! py-3! " +
  "font-inter-semibold font-semibold! " +
  "text-[17px] no-underline! border-0! " +
  "transition-all duration-200 " +
  "hover:-translate-y-0.5 hover:shadow-md " +
  "active:scale-95";

// FORMATO DE MONEDA COLOMBIANA

const formatoPrecio = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0,
});

// COLORES DE LAS CATEGORÍAS

const coloresCategorias = {
  GENERAL: "bg-[#69529b]",
  VIP: "bg-[#260477]",
  ESTUDIANTE: "bg-[#298496]",
  CORTESIA: "bg-[#373d4d]",
};

export default function DetalleEvento() {

  const { eventoId } = useParams();

  // CONSULTAR EVENTO REAL DEL BACKEND

  const {
    datos: evento,
    cargando,
    error,
  } = useConsulta(obtenerEvento, eventoId);

  // DISPONIBILIDAD REAL POR CATEGORÍA (BOLETERÍA)
  // Solo existe cuando el evento ya se publicó; si no, se muestran los cupos asignados.

  const { datos: aforo } = useConsulta(obtenerAforo, eventoId);

  const disponibles = Object.fromEntries(
    (aforo?.categorias || []).map((c) => [c.categoria, c.disponibles])
  );

  const agotado =
    !!aforo?.categorias?.length &&
    aforo.categorias.every((c) => c.disponibles === 0);

  return (

    <Pagina titulo={evento?.nombre || "Detalle del evento"}>

      <EstadoCarga
        cargando={cargando}
        error={error}
      />

      {/* BOTÓN REGRESAR */}

      <Link
        to="/eventos"
        className={`${buttonStyle}
          bg-[#69529b]! text-white!
          hover:bg-[#493778]! mb-6`}
      >
        ← Volver a eventos
      </Link>

      {evento && (

        <article className="eh-panel rounded-[40px]! p-6! sm:p-10!">

          {/* INFORMACIÓN GENERAL */}

          <div className="flex flex-wrap items-center
            justify-between gap-4 mb-8">

            <h2 className="font-inter-bold font-bold!
              text-[28px] text-[#0f0140]">

              Información del evento

            </h2>

            {/* ESTADO DEL EVENTO */}

            <span className="rounded-full bg-[#e9e1f8]
              px-5 py-2 font-inter-semibold
              text-[#5524bf] text-[14px]">

              {evento.estado}

            </span>

          </div>

          {/* TARJETAS DE INFORMACIÓN */}

          <section className="grid grid-cols-1
            sm:grid-cols-2 lg:grid-cols-3 gap-5">

            {/* LUGAR */}

            <div className="rounded-[30px]
              border-2 border-[#d5c9e7]
              bg-[#f5f2fa] p-6">

              <h3 className="font-inter-bold
                text-[20px] text-[#260477]">

                Lugar

              </h3>

              <p className="mt-2 font-inter-semibold
                text-[17px] text-[#0f0140]">

                {evento.lugar}

              </p>

            </div>

            {/* FECHA Y HORA */}

            <div className="rounded-[30px]
              border-2 border-[#d5c9e7]
              bg-[#f5f2fa] p-6">

              <h3 className="font-inter-bold
                text-[20px] text-[#260477]">

                Fecha y hora

              </h3>

              <p className="mt-2 font-inter-semibold
                text-[17px] text-[#0f0140]">

                {new Date(evento.fechaInicio).toLocaleString(
                  "es-CO",
                  {
                    dateStyle: "long",
                    timeStyle: "short",
                  }
                )}

              </p>

            </div>

            {/* AFORO */}

            <div className="rounded-[30px]
              border-2 border-[#d5c9e7]
              bg-[#f5f2fa] p-6">

              <h3 className="font-inter-bold
                text-[20px] text-[#260477]">

                Aforo máximo

              </h3>

              <p className="mt-2 font-inter-semibold
                text-[17px] text-[#0f0140]">

                {evento.aforoMaximo} personas

              </p>

            </div>

          </section>

          {/* SEPARADOR */}

          <hr className="my-10 border-[#d5c9e7]" />

          {/* CATEGORÍAS DE BOLETAS */}

          <h2 className="font-inter-bold font-bold!
            text-[28px] text-[#0f0140] mb-6">

            Categorías de boletas

          </h2>

          <section className="grid grid-cols-1
            sm:grid-cols-2 lg:grid-cols-4 gap-5">

            {evento.categorias?.map((categoria) => (

              <div
                key={categoria.tipo}
                className="overflow-hidden rounded-[30px]
                border-2 border-[#d5c9e7] bg-white
                shadow-sm transition-all duration-200
                hover:-translate-y-1 hover:shadow-lg"
              >

                {/* ENCABEZADO DE LA CATEGORÍA */}

                <div className={`
                  ${coloresCategorias[categoria.tipo]
                    || "bg-[#69529b]"}
                  px-4 py-4 text-center text-white
                  font-inter-bold text-[18px]
                `}>

                  {categoria.tipo}

                </div>

                {/* INFORMACIÓN DE LA BOLETA */}

                <div className="p-6 text-center">

                  <p className="font-inter-bold
                    font-bold! text-[26px]
                    text-[#260477]">

                    {formatoPrecio.format(categoria.precioBase)}

                  </p>

                  {disponibles[categoria.tipo] === undefined ? (

                    <p className="mt-2 font-inter-semibold
                      text-[#69529b] text-[15px]">

                      {categoria.cupos} cupos asignados

                    </p>

                  ) : disponibles[categoria.tipo] === 0 ? (

                    <p className="mt-2 inline-block rounded-full
                      bg-red-100 px-4 py-1
                      font-inter-bold text-[15px] text-red-700">

                      Agotado

                    </p>

                  ) : (

                    <p className="mt-2 font-inter-semibold
                      text-[#69529b] text-[15px]">

                      {disponibles[categoria.tipo]} de {categoria.cupos} disponibles

                    </p>

                  )}

                  <p className="mt-2 text-[14px]
                    text-gray-500">

                    COP

                  </p>

                </div>

              </div>

            ))}

          </section>

          {/* BOTÓN COMPRAR BOLETA */}

          <div className="mt-10 flex flex-col
            items-center justify-center">

            {evento.estado === "PUBLICADO" && agotado ? (

              <p className="rounded-full bg-red-100
                px-6 py-3 font-inter-semibold
                text-red-700 text-center">

                Boletas agotadas para este evento.

              </p>

            ) : evento.estado === "PUBLICADO" ? (

              <Link
                to={`/eventos/${evento.id}/comprar`}
                className={`${buttonStyle}
                  bg-[#5524bf]! text-white!
                  hover:bg-[#260477]!
                  min-w-[230px]`}
              >

                Comprar boleta

              </Link>

            ) : (

              <p className="rounded-full bg-[#e9e1f8]
                px-6 py-3 font-inter-semibold
                text-[#69529b] text-center">

                Este evento no está disponible para comprar boletas.

              </p>

            )}

          </div>

        </article>

      )}

    </Pagina>

  );
}