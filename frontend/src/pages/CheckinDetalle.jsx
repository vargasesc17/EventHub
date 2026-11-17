import { useEffect, useRef, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

const navigation = [
  { name: "Boletas", path: "/boletas" },
  { name: "Eventos", path: "/eventos" },
  { name: "Notificaciones", path: "/notificaciones" },
  { name: "Check-in", path: "/checkin" },
];

export default function CheckinDetalle() {
  const { eventoId } = useParams();
  const location = useLocation();
  const navigate = useNavigate(); // Hook para manejar la navegación hacia atrás
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [accountOpen, setAccountOpen] = useState(false);
  const accountMenuRef = useRef(null);

  useEffect(() => {
    if (!accountOpen) return;

    const closeMenu = (event) => {
      if (!accountMenuRef.current?.contains(event.target)) {
        setAccountOpen(false);
      }
    };

    window.addEventListener("mousedown", closeMenu);
    return () => window.removeEventListener("mousedown", closeMenu);
  }, [accountOpen]);

  return (
    <div className="min-h-dvh bg-[#373d4d] text-[#0f0140]">
      {/* Header corporativo */}
      <header className="relative z-20 h-[125px] bg-[#0f0140] text-white">
        <div className="mx-auto flex h-full max-w-[1192px] items-center justify-between px-6 lg:px-0">
          <Link
            to="/"
            className="flex items-center gap-2 rounded-md focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-white"
          >
            <img src={logoSvg} width="64" height="59" alt="EventHub" />
            <span className="font-bold text-[30px] leading-none sm:text-[36px]">
              EventHub
            </span>
          </Link>

          <nav className="hidden md:flex items-center gap-8" aria-label="Navegación principal">
            {navigation.map((item) => {
              const isActive = location.pathname.startsWith(item.path);
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  className={`font-semibold text-[20px] transition-colors px-4 py-2 rounded-lg ${
                    isActive ? "bg-[#070024] text-white shadow-inner" : "text-white/80 hover:bg-[#070024] hover:text-white"
                  }`}
                >
                  {item.name}
                </Link>
              );
            })}
          </nav>

          <div className="relative" ref={accountMenuRef}>
            <button
              className="flex size-[50px] items-center justify-center rounded-full transition-colors hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
              type="button"
              aria-label="Abrir menú de cuenta"
              aria-expanded={accountOpen}
              onClick={() => setAccountOpen((open) => !open)}
            >
              <img src={profileSvg} width="50" height="50" alt="Mi cuenta" className="rounded-full" />
            </button>
            {accountOpen && (
              <div className="absolute top-[62px] right-0 w-48 rounded-xl bg-white p-2 text-[16px] text-[#0f0140] shadow-xl">
                <strong className="block px-3 py-1 text-xs text-gray-500">Mi cuenta</strong>
                <button
                  type="button"
                  className="w-full rounded-lg px-3 py-2 text-left hover:bg-[#e5e5e7] transition-colors"
                  onClick={() => setAccountOpen(false)}
                >
                  Ver perfil
                </button>
                <button
                  type="button"
                  className="w-full rounded-lg px-3 py-2 text-left hover:bg-[#e5e5e7] transition-colors text-red-600"
                  onClick={() => setAccountOpen(false)}
                >
                  Cerrar sesión
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main id="main-content" className="mx-auto min-h-[calc(100dvh-125px)] max-w-[1580px] bg-[#e5e5e7] px-6 pt-[41px] pb-16 sm:px-10 lg:px-[58px]">
       
        <h1 className="font-bold text-[34px] leading-normal sm:text-[40px] text-[#0f0140] mb-8">
          Check-in del Evento #{eventoId}
        </h1>

        <section className="bg-white rounded-[24px] p-8 shadow-md max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8 items-center" aria-label="Información de check-in">
          {/* Marco del QR */}
          <div className="flex justify-center bg-gray-50 p-6 rounded-[20px] border border-gray-100">
            <div className="w-[250px] h-[250px] bg-gray-200 rounded-xl flex items-center justify-center text-gray-500 font-medium">
              <span>Código QR [281x281]</span>
            </div>
          </div>

          {/* Detalles de la boleta */}
          <article className="flex flex-col gap-4">
            <dl className="grid grid-cols-1 gap-3 text-sm">
              <div className="flex justify-between border-b pb-2">
                <dt className="font-semibold text-gray-500">Nombre:</dt>
                <dd className="font-medium text-[#0f0140]">[Name] [Lastname]</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="font-semibold text-gray-500">Lugar:</dt>
                <dd className="font-medium text-[#0f0140]">[place]</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="font-semibold text-gray-500">Fecha:</dt>
                <dd className="font-medium text-[#0f0140]">[date]</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="font-semibold text-gray-500">Tipo de boleta:</dt>
                <dd className="font-medium text-[#0f0140]">[ticket]</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="font-semibold text-gray-500">Hora ingreso:</dt>
                <dd className="font-medium text-[#0f0140]">{isCheckedIn ? "10:30" : "[hour]"}</dd>
              </div>
            </dl>

            <button
              className={`mt-4 w-full py-3 px-6 rounded-xl font-bold text-white transition-all shadow-md ${
                isCheckedIn 
                  ? "bg-green-600 hover:bg-green-700" 
                  : "bg-[#0f0140] hover:bg-[#20046b]"
              }`}
              type="button"
              aria-pressed={isCheckedIn}
              onClick={() => setIsCheckedIn((checkedIn) => !checkedIn)}
            >
              {isCheckedIn ? "Ingreso confirmado ✓" : "Confirmar Ingreso (Estado)"}
            </button>
          </article>
        </section>
        {/* Botón Volver */}
        <div className="mt-20">
          <button
            type="button"
            onClick={() => navigate("/checkin")}
            className="inline-flex items-center gap-2 bg-[#0f0140] hover:bg-[#20046b] text-white font-medium px-4 py-2 rounded-full text-lg transition-all shadow-md"
          >
            <span>← Volver a selección</span>
          </button>
        </div>

      </main>
    </div>
  );
}