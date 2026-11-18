import { useState, useEffect, useRef } from "react";
import {
  Link,
  NavLink,
  useNavigate,
  useLocation,
} from "react-router-dom";

import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

import { useSesion } from "../sesion/Sesion";

// NAVEGACIÓN SEGÚN EL ROL

const navigationByRole = {
  ORGANIZADOR: [
    { name: "Mis eventos", path: "/organizador", end: true },
    { name: "Crear evento", path: "/organizador/crear" },
  ],

  COMPRADOR: [
    { name: "Boletas", path: "/boletas" },
    { name: "Eventos", path: "/eventos" },
    { name: "Notificaciones", path: "/notificaciones" },
  ],

  STAFF: [
    { name: "Check-in", path: "/checkin" },
  ],
};

// PÁGINA PRINCIPAL SEGÚN EL ROL

const homeByRole = {
  ORGANIZADOR: "/organizador",
  COMPRADOR: "/eventos",
  STAFF: "/checkin",
};

// NOMBRES DE LOS ROLES

const roleNames = {
  ORGANIZADOR: "Organizador",
  COMPRADOR: "Comprador",
  STAFF: "Staff",
};

export default function Navbar() {

  const { usuario, salir } = useSesion();

  const navigate = useNavigate();
  const location = useLocation();

  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const profileRef = useRef(null);

  const navigation = navigationByRole[usuario?.rol] || [];
  const homePath = homeByRole[usuario?.rol] || "/login";

  // CERRAR LOS MENÚS AL CAMBIAR DE PÁGINA

  // Se ajusta durante el render, sin useEffect, para no causar un render de más.
  const [rutaAnterior, setRutaAnterior] = useState(location.pathname);
  if (rutaAnterior !== location.pathname) {
    setRutaAnterior(location.pathname);
    setIsMenuOpen(false);
    setIsProfileOpen(false);
  }

  // CERRAR EL PERFIL AL HACER CLIC AFUERA

  useEffect(() => {

    const closeMenu = (event) => {

      if (!profileRef.current?.contains(event.target)) {
        setIsProfileOpen(false);
      }

    };

    const handleEscape = (event) => {

      if (event.key === "Escape") {
        setIsProfileOpen(false);
        setIsMenuOpen(false);
      }

    };

    document.addEventListener("mousedown", closeMenu);
    document.addEventListener("keydown", handleEscape);

    return () => {

      document.removeEventListener("mousedown", closeMenu);
      document.removeEventListener("keydown", handleEscape);

    };

  }, []);

  // CERRAR SESIÓN

  const handleLogout = () => {

    setIsProfileOpen(false);
    setIsMenuOpen(false);

    salir();

    navigate("/login", { replace: true });

  };

  if (!usuario) return null;

  return (

    <header className="relative z-50 h-[125px]
      bg-[#0f0140] text-white">

      <div className="mx-auto flex h-full
        max-w-[1192px] items-center justify-between
        gap-4 px-6 xl:px-0">

        {/* LOGO DE EVENTHUB */}

        <Link
          to={homePath}
          onClick={() => {
            setIsMenuOpen(false);
            setIsProfileOpen(false);
          }}
          className="flex shrink-0 items-center gap-0.5
            rounded-lg text-white! no-underline!
            transition-opacity duration-200
            hover:opacity-80
            focus-visible:outline-2
            focus-visible:outline-white"
          aria-label="Ir al inicio de EventHub"
        >

          <img
            src={logoSvg}
            alt=""
            className="h-[59px] w-[64px]"
          />

          <span className="font-inter-bold
            text-[26px] leading-none sm:text-[36px]">

            EventHub

          </span>

        </Link>

        {/* NAVEGACIÓN PRINCIPAL */}

        <nav
          className={`
            ${isMenuOpen ? "flex" : "hidden"}
            absolute top-[125px] right-0 left-0
            flex-col gap-2 bg-[#0f0140] px-6 pb-6
            lg:static lg:flex lg:flex-row
            lg:items-center lg:gap-4
            lg:bg-transparent lg:p-0
          `}
          aria-label="Navegación principal"
        >

          {navigation.map((item) => (

            <NavLink
              key={item.path}
              to={item.path}
              end={item.end || false}
              onClick={() => {
                setIsMenuOpen(false);
                setIsProfileOpen(false);
              }}
              className={({ isActive }) => `
                font-inter-bold
                flex items-center justify-center
                min-h-[48px] px-4 py-2
                rounded-xl text-[20px]
                whitespace-nowrap no-underline!
                transition-colors duration-200
                focus-visible:outline-2
                focus-visible:outline-white
                ${
                  isActive
                    ? "bg-[#070024] text-white! shadow-inner"
                    : "text-white/80! hover:bg-[#070024] hover:text-white!"
                }
              `}
            >

              {item.name}

            </NavLink>

          ))}

        </nav>

        {/* PERFIL Y MENÚ RESPONSIVE */}

        <div className="flex shrink-0
          items-center gap-3">

          {/* PERFIL */}

          <div
            className="relative"
            ref={profileRef}
          >

            <button
              type="button"
              onClick={() => {
                setIsProfileOpen((open) => !open);
                setIsMenuOpen(false);
              }}
              aria-label="Mi perfil"
              aria-expanded={isProfileOpen}
              className="
                flex! size-[54px]!
                items-center! justify-center!
                rounded-full! bg-transparent!
                border-0! p-2! shadow-none!
                cursor-pointer transition-all duration-200
                hover:bg-white/15!
                active:scale-95
                focus-visible:outline-2
                focus-visible:outline-white
              "
            >

              <img
                src={profileSvg}
                alt=""
                className="
                  block size-[38px]!
                  object-contain
                  brightness-0 invert
                  pointer-events-none
                "
              />

            </button>

            {/* MENÚ DESPLEGABLE DEL PERFIL */}

            {isProfileOpen && (

              <div className="
                absolute right-0 top-[68px]
                z-50 w-[300px]
                max-w-[calc(100vw-32px)]
                rounded-[30px]
                border-2 border-[#d5c9e7]
                bg-white p-5
                text-[#0f0140] shadow-2xl
              ">

                {/* INFORMACIÓN DEL USUARIO */}

                <div className="flex flex-col
                  items-center gap-3 text-center">

                  {/* ICONO DEL PERFIL */}

                  <div className="flex size-[76px]
                    items-center justify-center
                    rounded-full bg-[#5524bf]">

                    <img
                      src={profileSvg}
                      alt=""
                      className="size-[46px]
                        object-contain
                        brightness-0 invert"
                    />

                  </div>

                  <h2 className="font-inter-bold
                    font-bold! text-[22px]
                    text-[#0f0140]">

                    {usuario.nombre}

                  </h2>

                  <p className="w-full break-all
                    font-inter-semibold
                    text-[14px] text-[#69529b]">

                    {usuario.correo}

                  </p>

                  <span className="rounded-full
                    border-2 border-[#d5c9e7]
                    bg-[#e9e1f8] px-5 py-2
                    font-inter-bold text-[14px]
                    text-[#5524bf]">

                    {roleNames[usuario.rol] || usuario.rol}

                  </span>

                </div>

                <hr className="my-5
                  border-[#d5c9e7]" />

                {/* INFORMACIÓN DE LA SESIÓN */}

                <div className="mb-5
                  rounded-[20px] bg-[#f5f2fa]
                  p-4">

                  <p className="font-inter-semibold
                    text-[13px] text-[#69529b]">

                    Tipo de cuenta

                  </p>

                  <p className="mt-1
                    font-inter-bold text-[17px]">

                    {roleNames[usuario.rol] || usuario.rol}

                  </p>

                </div>

                {/* CERRAR SESIÓN */}

                <button
                  type="button"
                  onClick={handleLogout}
                  className="
                    flex! w-full!
                    items-center! justify-center!
                    gap-3 rounded-full!
                    border-0! px-5! py-3!
                    bg-[#b94363]!
                    text-white!
                    font-inter-bold font-bold!
                    text-[16px]
                    cursor-pointer
                    transition-all duration-200
                    hover:bg-[#922f4b]!
                    hover:shadow-md
                    active:scale-95
                  "
                >

                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="21"
                    height="21"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                  >
                    <path d="M10 17l5-5-5-5" />
                    <path d="M15 12H3" />
                    <path d="M15 3h5a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1h-5" />
                  </svg>

                  Cerrar sesión

                </button>

              </div>

            )}

          </div>

          {/* BOTÓN HAMBURGUESA */}

          <button
            type="button"
            onClick={() => {
              setIsMenuOpen((open) => !open);
              setIsProfileOpen(false);
            }}
            aria-label={
              isMenuOpen
                ? "Cerrar navegación"
                : "Abrir navegación"
            }
            aria-expanded={isMenuOpen}
            className="
              flex! lg:hidden!
              size-[42px]!
              items-center! justify-center!
              rounded-full!
              bg-transparent!
              border-0! p-0! shadow-none!
              cursor-pointer
              transition-colors duration-200
              hover:bg-white/15!
            "
          >

            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="27"
              height="27"
              viewBox="0 0 24 24"
              fill="none"
              stroke="white"
              strokeWidth="2"
              strokeLinecap="round"
              aria-hidden="true"
            >

              {isMenuOpen ? (

                <>
                  <path d="M6 6l12 12" />
                  <path d="M18 6L6 18" />
                </>

              ) : (

                <>
                  <path d="M3 6h18" />
                  <path d="M3 12h18" />
                  <path d="M3 18h18" />
                </>

              )}

            </svg>

          </button>

        </div>

      </div>

    </header>

  );

}