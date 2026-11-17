import { useEffect, useRef, useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

const events = [
  { id: 1, name: "Lorem ipsum dolor sit" },
  { id: 2, name: "Lorem ipsum dolor sit" },
  { id: 3, name: "Lorem ipsum dolor sit" },
];

const navigation = ["Boletas", "Eventos", "Notificaciones", "Check-in"];

export default function Eventos() {
  const [activeSection, setActiveSection] = useState("Eventos");
  const [verifiedEvent, setVerifiedEvent] = useState(null);
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
      {/* Header */}
      <header className="relative z-20 h-[125px] bg-[#0f0140] text-white">
        <div className="mx-auto flex h-full max-w-[1192px] items-center justify-between px-6 lg:px-0">
          <a
            className="flex items-center gap-2 rounded-md focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-white"
            href="#main-content"
            aria-label="EventHub, ir al contenido principal"
          >
            <img src={logoSvg} width="64" height="59" alt="EventHub" />
            <span className="font-bold text-[30px] leading-none sm:text-[36px]">
              EventHub
            </span>
          </a>

          <nav className="hidden md:flex items-center gap-8" aria-label="Navegación principal">
            {navigation.map((item) => (
              <a
                key={item}
                className={`font-semibold text-[20px] transition-colors ${
                  activeSection === item ? "text-white underline underline-offset-8" : "text-white/80 hover:text-white"
                }`}
                href={`#${item.toLowerCase()}`}
                onClick={(event) => {
                  event.preventDefault();
                  setActiveSection(item);
                }}
              >
                {item}
              </a>
            ))}
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
      <main id="main-content" className="mx-auto min-h-[calc(100dvh-125px)] max-w-[1090px] bg-[#e5e5e7] px-6 pt-[41px] pb-16 sm:px-10 lg:px-[58px]">
        <h1 className="font-bold text-[34px] leading-normal sm:text-[40px] text-[#0f0140]">
          Lorem ipsum dolor sit
        </h1>

        <section className="mt-[20px] grid grid-cols-1 gap-x-[34px] gap-y-8 sm:grid-cols-2 lg:grid-cols-3" aria-label="Eventos disponibles">
          {events.map((event) => {
            const isVerified = verifiedEvent === event.id;

            return (
              <article key={event.id} className="flex flex-col">
                <div className="h-[207px] rounded-[21px] bg-white shadow-sm" aria-hidden="true" />
                <h2 className="mt-[13px] px-[17px] text-[24px] font-semibold text-[#0f0140] leading-normal">
                  {event.name}
                </h2>
                <div className="mt-3 px-[17px]">
                  <button
                    type="button"
                    className={`rounded-xl px-5 py-2.5 text-white font-medium text-sm transition-all shadow-md ${
                      isVerified ? "bg-[#38128c]" : "bg-[#0f0140] hover:bg-[#20046b]"
                    }`}
                    aria-pressed={isVerified}
                    onClick={() => setVerifiedEvent(isVerified ? null : event.id)}
                  >
                    <span>{isVerified ? "Aforo verificado" : "Verificar aforo"}</span>
                  </button>
                </div>
              </article>
            );
          })}
        </section>

        <div className="mt-8">
          <button
            type="button"
            className="rounded-xl bg-[#0f0140] px-6 py-3 text-white font-semibold shadow-md hover:bg-[#20046b] transition-all"
            onClick={() => alert("Redirigiendo a creación de evento...")}
          >
            Crear evento
          </button>
        </div>

        <p className="sr-only" aria-live="polite">
          {verifiedEvent
            ? `Aforo verificado para el evento ${verifiedEvent}`
            : "Ningún aforo verificado"}
        </p>
      </main>
    </div>
  );
}