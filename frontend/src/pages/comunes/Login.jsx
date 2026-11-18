import { useNavigate } from "react-router-dom";

import logoSvg from "../../assets/eventhubcolibri.svg";

import { USUARIOS } from "../../sesion/usuarios";
import { useSesion } from "../../sesion/Sesion";

// Configuración visual de cada rol
const ROLES = {
  ORGANIZADOR: {
    titulo: "Organizador",
    descripcion: "Crea y gestiona tus eventos",
    color: "bg-[#260477]",
    ruta: "/organizador",
    icono: (
      <>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M16 3v4M8 3v4M3 10h18" />
        <path d="M12 13v5M9.5 15.5h5" />
      </>
    ),
  },

  COMPRADOR: {
    titulo: "Comprador",
    descripcion: "Explora eventos y compra boletas",
    color: "bg-[#69529b]",
    ruta: "/eventos",
    icono: (
      <>
        <path d="M4 5h16v4a3 3 0 0 0 0 6v4H4v-4a3 3 0 0 0 0-6Z" />
        <path d="M13 5v14" strokeDasharray="2 3" />
      </>
    ),
  },

  STAFF: {
    titulo: "Staff",
    descripcion: "Gestiona el ingreso de asistentes",
    color: "bg-[#0f0140]",
    ruta: "/checkin",
    icono: (
      <>
        <path d="M3 7V3h4M17 3h4v4M21 17v4h-4M7 21H3v-4" />
        <rect x="7" y="7" width="4" height="4" />
        <rect x="14" y="7" width="3" height="3" />
        <rect x="7" y="14" width="3" height="3" />
        <path d="M14 14h3v3h-3z" />
      </>
    ),
  },
};

export default function Login() {

  const { ingresar } = useSesion();
  const navigate = useNavigate();

  // Iniciar sesión según el usuario seleccionado
  const iniciarSesion = (usuario) => {

    ingresar(usuario);

    navigate(ROLES[usuario.rol].ruta, {
      replace: true,
    });

  };

  return (
  <div className="min-h-dvh bg-[#373d4d]">

    {/* ENCABEZADO */}

    <header className="h-[125px] bg-[#0f0140]">

      <div className="mx-auto flex h-full max-w-[1192px] items-center justify-end px-6">

        

      </div>

    </header>

    {/* CONTENEDOR PRINCIPAL */}

    <main className="mx-auto flex min-h-[calc(100dvh-125px)] max-w-[1580px] flex-col items-center bg-[#0f0140] px-5 pb-12">

      {/* LOGO EVENTHUB */}

      <div className="flex items-center justify-center gap-1">

        <img
          src={logoSvg}
          alt=""
          className="h-[100px] w-[105px] sm:h-[140px] sm:w-[145px]"
        />

        <span className="font-inter-bold text-[42px] leading-none text-white sm:text-[80px]">
          EventHub
        </span>

      </div>

      {/* TÍTULO INICIO DE SESIÓN */}

      <h1 className="font-inter-bold mt-10 rounded-full bg-[#5524bf] px-7 py-1 text-[20px] text-white sm:text-[25px]">
        Inicio de sesión
      </h1>

      {/* CONTENEDOR TURQUESA */}

      <div className="mt-5 flex w-full max-w-[400px] flex-col gap-3 rounded-[28px] bg-[#298496] p-4">

        {USUARIOS.map((usuario) => {

          const rol = ROLES[usuario.rol];

          return (

            <button
              key={usuario.id}
              type="button"
              onClick={() => iniciarSesion(usuario)}
              className="
                group
                !flex !flex-row !items-center
                !justify-between
                w-full
                !min-h-0 !h-auto
                gap-3
                !rounded-[16px]
                !bg-[#5524bf]
                !px-4 !py-3
                text-left text-white
                shadow-md
                transition-all duration-200
                hover:brightness-110
                hover:-translate-y-0.5
                active:scale-[0.98]
                focus-visible:outline-2
                focus-visible:outline-white
              "
            >

              {/* ICONO DEL ROL */}

              <div className="flex size-[42px] shrink-0 items-center justify-center rounded-[12px] bg-white/15">

                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.7"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="size-[24px]"
                  aria-hidden="true"
                >
                  {rol.icono}
                </svg>

              </div>

              {/* TEXTO DEL BOTÓN */}

              <div className="min-w-0 flex-1">

                <span className="font-inter-bold block text-[18px] leading-tight">
                  {rol.titulo}
                </span>

                <span className="mt-1 block text-[11px] leading-tight text-white/80 sm:text-[12px]">
                  {rol.descripcion}
                </span>

              </div>

              {/* FLECHA */}

              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                className="size-4 shrink-0 transition-transform group-hover:translate-x-1"
                aria-hidden="true"
              >
                <path d="m9 18 6-6-6-6" />
              </svg>

            </button>

          );

        })}

      </div>

      {/* TEXTO INFERIOR */}

      <p className="mt-6 text-center text-[12px] text-white/60">
        Acceso de demostración · EventHub
      </p>

    </main>

  </div>
);
}