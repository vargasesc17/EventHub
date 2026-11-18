import { useState } from "react";
import { useNavigate } from "react-router-dom";

import Pagina from "../../components/Pagina";
import { useSesion } from "../../sesion/Sesion";

const TIPOS_CATEGORIA = [
  "GENERAL",
  "VIP",
  "ESTUDIANTE",
  "CORTESIA",
];

const inputStyle =
  "w-full rounded-full! border-0! bg-white! px-5 py-3 text-[16px] " +
  "font-inter-semibold font-semibold text-[#0f0140] placeholder:text-[#9588ae] placeholder:font-semibold " +
  "shadow-md outline-none focus:ring-2 focus:ring-[#5524bf]";

export default function CrearEvento() {

  const { usuario } = useSesion();
  const navigate = useNavigate();

  // Información general del evento
  const [datosEvento, setDatosEvento] = useState({
    nombre: "",
    lugar: "",
    fechaInicio: "",
    aforoMaximo: "",
  });

  // Categorías de boletas
  const [categorias, setCategorias] = useState([
    {
      tipo: "GENERAL",
      cupos: "",
      precioBase: "",
    },
  ]);

  const [error, setError] = useState("");
  const [erroresCampos, setErroresCampos] = useState({});
  const [cargando, setCargando] = useState(false);

  // Actualizar información general
  const handleChange = (e) => {
    const { name, value } = e.target;

    setDatosEvento((anterior) => ({
      ...anterior,
      [name]: value,
    }));
  };

  // Actualizar una categoría
  const cambiarCategoria = (index, campo, valor) => {
    setCategorias((anteriores) =>
      anteriores.map((categoria, i) =>
        i === index
          ? { ...categoria, [campo]: valor }
          : categoria
      )
    );
  };

  // Agregar categoría
  const agregarCategoria = () => {
    const tipoDisponible = TIPOS_CATEGORIA.find(
      (tipo) => !categorias.some((c) => c.tipo === tipo)
    );

    if (!tipoDisponible) return;

    setCategorias((anteriores) => [
      ...anteriores,
      {
        tipo: tipoDisponible,
        cupos: "",
        precioBase: "",
      },
    ]);
  };

  // Eliminar categoría
  const eliminarCategoria = (index) => {
    setCategorias((anteriores) =>
      anteriores.filter((_, i) => i !== index)
    );
  };

  // Sumar los cupos de todas las categorías
  const totalCupos = categorias.reduce(
    (total, categoria) =>
      total + Number(categoria.cupos),
    0
  );

  // Crear evento
  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setErroresCampos({});

    if (usuario?.rol !== "ORGANIZADOR") {
      setError("Solo un organizador puede crear eventos.");
      return;
    }

    if (totalCupos > Number(datosEvento.aforoMaximo)) {
      setError(
        "La suma de cupos no puede superar el aforo máximo."
      );
      return;
    }

    setCargando(true);

    try {

      // Construir el JSON según el contrato de la API
      const nuevoEvento = {
        nombre: datosEvento.nombre.trim(),
        lugar: datosEvento.lugar.trim(),

        fechaInicio: new Date(
          datosEvento.fechaInicio
        ).toISOString(),

        aforoMaximo: Number(datosEvento.aforoMaximo),

        organizadorId: usuario.id,
        organizadorCorreo: usuario.correo,

        categorias: categorias.map((categoria) => ({
          tipo: categoria.tipo,
          cupos: Number(categoria.cupos),
          precioBase: Number(categoria.precioBase),
        })),
      };

      const respuesta = await fetch("/api/eventos", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(nuevoEvento),
      });

      const resultado = await respuesta.json();

      // Manejar errores del backend
      if (!respuesta.ok) {

        if (respuesta.status === 400) {
          setErroresCampos(resultado.errores || {});
        }

        setError(
          resultado.detail || "No se pudo crear el evento."
        );

        return;
      }

      // Guardar el ID del evento en localStorage
      // porque aún no existe el endpoint "Mis eventos"

      const clave = `misEventos:${usuario.id}`;

      try {
        const eventosGuardados = JSON.parse(
          localStorage.getItem(clave) || "[]"
        );

        localStorage.setItem(
          clave,
          JSON.stringify([
            ...new Set([...eventosGuardados, resultado.id]),
          ])
        );
      } catch {
        setError(
          `El evento fue creado con ID ${resultado.id}, ` +
          "pero no se pudo guardar en el navegador."
        );
        return;
      }

      // Regresar a la vista del organizador
      navigate("/organizador");

    } catch (err) {

      setError(
        "No se pudo completar la solicitud. " +
        "Verifica la conexión antes de intentar nuevamente."
      );

    } finally {
      setCargando(false);
    }
  };

  return (

    <Pagina titulo="Gestión de eventos">

    <section className="mt-3 flex flex-col items-center">

      {/* TÍTULO DEL FORMULARIO */}

      <h2 className="font-inter-bold rounded-full bg-[#260477] px-8 py-2 text-[25px] text-white sm:text-[30px]">
        Nuevo evento
      </h2>

      <form
        onSubmit={handleSubmit}
        className="mt-3 flex w-full max-w-[420px] flex-col items-center gap-5"
      >

        {/* CONTENEDOR MORADO */}

        <div className="flex w-full flex-col gap-3 rounded-[45px] bg-[#69529b] p-6">

          {/* NOMBRE */}

          <input
            type="text"
            name="nombre"
            aria-label="Nombre del evento"
            placeholder="Nombre del evento"
            value={datosEvento.nombre}
            onChange={handleChange}
            className={inputStyle}
            required
          />

          {/* LUGAR */}

          <input
            type="text"
            name="lugar"
            aria-label="Lugar"
            placeholder="Lugar"
            value={datosEvento.lugar}
            onChange={handleChange}
            className={inputStyle}
            required
          />

          {/* FECHA Y HORA */}

          <div className="flex flex-col gap-1">

            <label
              htmlFor="fechaInicio"
              className="font-inter-semibold ml-3 text-sm text-white"
            >
              Fecha y hora del evento
            </label>

            <input
              id="fechaInicio"
              type="datetime-local"
              name="fechaInicio"
              value={datosEvento.fechaInicio}
              onChange={handleChange}
              className={inputStyle}
              required
            />

          </div>

          {/* AFORO MÁXIMO */}

          <input
            type="number"
            name="aforoMaximo"
            aria-label="Aforo máximo"
            placeholder="Aforo máximo"
            value={datosEvento.aforoMaximo}
            onChange={handleChange}
            className={inputStyle}
            min="1"
            step="1"
            required
          />

          <hr className="my-2 border-white/30" />

          {/* CATEGORÍAS DE BOLETAS */}

          <h3 className="font-inter-bold text-center text-[20px] text-white">
            Categorías de boletas
          </h3>

          {categorias.map((categoria, index) => (

            <div
              key={index}
              className="flex flex-col gap-3 rounded-[35px] bg-[#ffffff20] p-4"
            >

              <div className="flex items-center justify-between">

                <span className="font-inter-semibold text-sm text-white">
                  Categoría {index + 1}
                </span>

                {categorias.length > 1 && (

                  <button
                    type="button"
                    onClick={() => eliminarCategoria(index)}
                    className="rounded-lg px-2 text-white hover:bg-red-500"
                    aria-label={`Eliminar categoría ${index + 1}`}
                  >
                    ✕
                  </button>

                )}

              </div>

              {/* TIPO DE CATEGORÍA */}

              <select
                aria-label={`Tipo de categoría ${index + 1}`}
                value={categoria.tipo}
                onChange={(e) =>
                  cambiarCategoria(
                    index,
                    "tipo",
                    e.target.value
                  )
                }
                className={inputStyle}
              >

                {TIPOS_CATEGORIA.map((tipo) => (

                  <option
                    key={tipo}
                    value={tipo}
                    disabled={categorias.some(
                      (c, i) =>
                        i !== index && c.tipo === tipo
                    )}
                  >
                    {tipo}
                  </option>

                ))}

              </select>

              {/* CUPOS */}

              <input
                type="number"
                aria-label={`Cupos de categoría ${index + 1}`}
                placeholder="Cantidad de cupos"
                value={categoria.cupos}
                onChange={(e) =>
                  cambiarCategoria(
                    index,
                    "cupos",
                    e.target.value
                  )
                }
                className={inputStyle}
                min="1"
                step="1"
                required
              />

              {/* PRECIO BASE */}

              <input
                type="number"
                aria-label={`Precio de categoría ${index + 1}`}
                placeholder="Precio base (COP)"
                value={categoria.precioBase}
                onChange={(e) =>
                  cambiarCategoria(
                    index,
                    "precioBase",
                    e.target.value
                  )
                }
                className={inputStyle}
                min="0"
                step="1"
                required
              />

            </div>

          ))}

          {/* AGREGAR CATEGORÍA */}

          {categorias.length < TIPOS_CATEGORIA.length && (

            <button
              type="button"
              onClick={agregarCategoria}
              className="font-inter-semibold rounded-full! border-2 border-white py-2 text-white transition-colors hover:bg-white hover:text-[#69529b]"
            >
              + Agregar categoría
            </button>

          )}

          {/* RESUMEN DEL AFORO */}

          <div className="rounded-[25px] bg-white/15 p-3 text-center text-white">

            <p className="font-inter-semibold">
              Cupos asignados: {totalCupos} /{" "}
              {datosEvento.aforoMaximo || 0}
            </p>

            {totalCupos > Number(datosEvento.aforoMaximo) && (

              <p className="mt-2 rounded-lg bg-red-100 p-2 text-sm text-red-700">
                Los cupos superan el aforo máximo.
              </p>

            )}

          </div>

        </div>

        {/* MENSAJES DE ERROR */}

        {error && (

          <div
            role="alert"
            className="w-full rounded-xl bg-red-100 p-4 text-sm text-red-700"
          >
            <p className="font-inter-semibold">
              {error}
            </p>

            {Object.entries(erroresCampos).map(
              ([campo, mensaje]) => (
                <p key={campo} className="mt-1">
                  {campo}: {mensaje}
                </p>
              )
            )}

          </div>

        )}

        {/* BOTÓN CREAR */}

        <button
          type="submit"
          disabled={cargando}
          className="font-inter-bold min-w-[150px] rounded-full! bg-[#260477]! px-8 py-2 text-[22px] text-white transition-colors hover:bg-[#070024] disabled:cursor-not-allowed disabled:opacity-50"
        >
          {cargando ? "Creando..." : "Crear"}
        </button>

      </form>

    </section>

    </Pagina>

  );
}
