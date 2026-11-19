import { useState } from "react";

const tipos = ["GENERAL", "VIP", "ESTUDIANTE", "CORTESIA"];

// ESTILO DE LOS CAMPOS
const inputStyle =
  "w-full rounded-full! border-2! border-solid! border-[#9682B8]! " +
  "bg-white! px-5 py-3 " +
  "text-[16px] font-inter-semibold font-semibold! " +
  "text-[#0f0140] placeholder:text-[#9588ae] " +
  "placeholder:font-semibold shadow-sm outline-none " +
  "transition-all duration-200 " +
  "hover:border-[#69529b]! " +
  "focus:border-[#5524bf]! focus:ring-2 focus:ring-[#5524bf]/20";

// ESTILO DE LAS ETIQUETAS
const labelStyle =
  "flex flex-col gap-2 font-inter-semibold font-semibold! text-[16px]";

// ESTILO DE LOS BOTONES
const buttonStyle =
  "rounded-full! bg-[#5524bf]! px-6 py-2 " +
  "font-inter-bold font-bold! text-white " +
  "transition-colors hover:bg-[#260477]! " +
  "disabled:opacity-50 disabled:cursor-not-allowed";

function fechaLocal(fecha) {
  const d = new Date(fecha);

  return new Date(
    d.getTime() - d.getTimezoneOffset() * 60000
  ).toISOString().slice(0, 16);
}

export default function FormularioEvento({
  evento,
  reprogramar = false,
  onGuardar,
  ocupado,
}) {

  const [nombre, setNombre] = useState(evento?.nombre || "");
  const [lugar, setLugar] = useState(evento?.lugar || "");

  const [fecha, setFecha] = useState(
    evento ? fechaLocal(evento.fechaInicio) : ""
  );

  const [aforo, setAforo] = useState(evento?.aforoMaximo || 100);

  const [categorias, setCategorias] = useState(
    evento?.categorias || [
      {
        tipo: "GENERAL",
        cupos: 100,
        precioBase: 0,
      },
    ]
  );

  const [error, setError] = useState("");

  // MODIFICAR CATEGORÍA
  const cambiar = (indice, campo, valor) =>
      setCategorias((actual) =>
          actual.map((c, i) => {
            if (i !== indice) return c;
            const nueva = { ...c, [campo]: campo === "tipo" ? valor : Number(valor) };
            // Una cortesía no tiene precio: al elegirla, el precio queda en 0 (lo exige Eventos).
            return nueva.tipo === "CORTESIA" ? { ...nueva, precioBase: 0 } : nueva;
          })
      );

  // GUARDAR EVENTO
  function enviar(e) {
    e.preventDefault();

    if (
      !lugar.trim() ||
      !Number.isFinite(new Date(fecha).getTime())
    ) {
      setError("Completa un lugar y una fecha válidos.");
      return;
    }

    if (
      !reprogramar &&
      (
        !nombre.trim() ||
        !Number.isSafeInteger(Number(aforo)) ||
        categorias.some(
          (c) =>
            !Number.isSafeInteger(c.cupos) ||
            !Number.isSafeInteger(c.precioBase)
        ) ||
        new Set(categorias.map((c) => c.tipo)).size !==
          categorias.length ||
        categorias.reduce((s, c) => s + c.cupos, 0) >
          Number(aforo)
      )
    ) {
      setError(
        "Revisa el nombre, las categorías únicas, los valores enteros y los cupos dentro del aforo."
      );
      return;
    }

    setError("");

    onGuardar(
      reprogramar
        ? {
            lugar: lugar.trim(),
            fechaInicio: new Date(fecha).toISOString(),
          }
        : {
            nombre: nombre.trim(),
            lugar: lugar.trim(),
            fechaInicio: new Date(fecha).toISOString(),
            aforoMaximo: Number(aforo),
            categorias,
          }
    );
  }

  return (
    <form
      className="eh-panel eh-form rounded-[45px]!"
      onSubmit={enviar}
    >
      <fieldset
        disabled={ocupado}
        className="flex min-w-0 flex-col gap-5 border-0"
      >

        {/* NOMBRE DEL EVENTO */}

        {!reprogramar && (
          <label className={labelStyle}>
            Nombre

            <input
              className={inputStyle}
              required
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
            />
          </label>
        )}

        {/* LUGAR */}

        <label className={labelStyle}>
          Lugar

          <input
            className={inputStyle}
            required
            value={lugar}
            onChange={(e) => setLugar(e.target.value)}
          />
        </label>

        {/* FECHA Y HORA */}

        <label className={labelStyle}>
          Fecha y hora local

          <input
            className={inputStyle}
            required
            type="datetime-local"
            min={fechaLocal(new Date().toISOString())}
            value={fecha}
            onChange={(e) => setFecha(e.target.value)}
          />
        </label>

        {!reprogramar && (
          <>

            {/* AFORO MÁXIMO */}

            <label className={labelStyle}>
              Aforo máximo

              <input
                className={inputStyle}
                required
                type="number"
                min="1"
                step="1"
                value={aforo}
                onChange={(e) => setAforo(e.target.value)}
              />
            </label>

            {/* TÍTULO DE CATEGORÍAS */}

            <h2 className="font-inter-bold font-bold text-[24px]">
              Categorías de boleta
            </h2>

            {/* CATEGORÍAS */}

            {categorias.map((c, i) => (

              <div
                className="eh-category rounded-[35px]! p-5"
                key={i}
              >

                {/* TIPO */}

                <label className={labelStyle}>
                  Tipo

                  <select
                    className={inputStyle}
                    value={c.tipo}
                    onChange={(e) =>
                      cambiar(i, "tipo", e.target.value)
                    }
                  >
                    {tipos.map((t) => (
                      <option key={t}>{t}</option>
                    ))}
                  </select>
                </label>

                {/* CUPOS */}

                <label className={labelStyle}>
                  Cupos

                  <input
                    className={inputStyle}
                    type="number"
                    required
                    min="1"
                    step="1"
                    value={c.cupos}
                    onChange={(e) =>
                      cambiar(i, "cupos", e.target.value)
                    }
                  />
                </label>

                {/* PRECIO */}

                <label className={labelStyle}>
                  Precio (COP)

                  <input
                      className={inputStyle}
                      type="number"
                      required
                      min="0"
                      step="1"
                      value={c.precioBase}
                      disabled={c.tipo === "CORTESIA"}
                      title={c.tipo === "CORTESIA" ? "Las cortesías no tienen precio" : undefined}
                      onChange={(e) =>
                          cambiar(i, "precioBase", e.target.value)
                      }
                  />
                </label>

                {/* QUITAR CATEGORÍA */}

                <button
                  className={`${buttonStyle} mt-4`}
                  type="button"
                  disabled={categorias.length === 1}
                  onClick={() =>
                    setCategorias((actual) =>
                      actual.filter((_, j) => i !== j)
                    )
                  }
                >
                  Quitar categoría
                </button>

              </div>
            ))}

            {/* AGREGAR CATEGORÍA */}

            <button
              className={buttonStyle}
              type="button"
              disabled={categorias.length === tipos.length}
              onClick={() =>
                setCategorias((actual) => [
                  ...actual,
                  {
                    tipo: tipos.find(
                      (t) => !actual.some((c) => c.tipo === t)
                    ),
                    cupos: 1,
                    precioBase: 0,
                  },
                ])
              }
            >
              + Agregar categoría
            </button>

          </>
        )}

        {/* MENSAJE DE ERROR */}

        {error && (
          <p
            role="alert"
            className="rounded-[25px] bg-red-100 p-4 font-inter-semibold text-sm text-red-700"
          >
            {error}
          </p>
        )}

        {/* BOTÓN GUARDAR */}

        <button
          type="submit"
          className={`${buttonStyle} mt-3 min-w-[180px] self-center text-[20px]`}
        >
          {ocupado ? "Guardando..." : "Guardar evento"}
        </button>

      </fieldset>
    </form>
  );
}