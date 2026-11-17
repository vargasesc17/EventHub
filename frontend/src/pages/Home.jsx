import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";
import EventCard from "../components/EventCard";

const navigation = [
  { name: "Boletas", path: "/boletas" },
  { name: "Eventos", path: "/eventos" },
  { name: "Notificaciones", path: "/notificaciones" },
  { name: "Check-in", path: "/checkin" },
];

// Arreglo dinámico con los datos de los eventos
const eventsList = [
  { id: 1, titulo: "Conferencia Tech 2026", imagen: "" },
  { id: 2, titulo: "Taller de React y Tailwind", imagen: "" },
  { id: 3, titulo: "Meetup de IoT y Hardware", imagen: "" },
  { id: 4, titulo: "Hackathon EventHub", imagen: "" },
];

export default function Home() {
  const location = useLocation(); // Detecta la ruta actual para saber cuál pestaña está activa
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const handleSelectEvent = (id) => {
    setSelectedEventId(selectedEventId === id ? null : id);
  };

  return (
    <div className="min-h-dvh bg-[#7F8081] text-[#0f0140]">
      <header className="relative z-20 h-[125px] bg-[#0f0140] text-white">
        <div className="mx-auto flex h-full max-w-[1192px] items-center justify-between px-6 lg:px-0">
          
          {/* Logo que redirige a la raíz (Home) */}
          <Link
            to="/"
            className="flex items-center gap-0.5 rounded-md focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-white"
            aria-label="Ir al inicio"
          >
            <img src={logoSvg} alt="" className="h-[59px] w-[64px]" />
            <span className="font-inter-bold text-[30px] leading-none sm:text-[36px]">
              EventHub
            </span>
          </Link>

          {/* Navegación mediante Links de React Router */}
          <nav
            className={`${
              isMenuOpen ? "flex" : "hidden"
            } absolute top-[125px] right-0 left-0 flex-col bg-[#0f0140] px-6 pb-6 md:static md:flex md:flex-row md:items-center md:gap-9 md:bg-transparent md:p-0 lg:gap-[43px]`}
            aria-label="Navegación principal"
          >
            {navigation.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  onClick={() => setIsMenuOpen(false)}
                  className={`font-inter-bold rounded-sm py-3 px-2 text-left text-[20px] leading-normal transition-colors focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-white md:py-0 md:text-[24px] ${
                    isActive
                      ? "bg-[#070024] text-white"
                      : "text-white/80 hover:bg-[#070024] hover:text-white"
                  }`}
                  aria-current={isActive ? "page" : undefined}
                >
                  {item.name}
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center gap-3">
            <div className="relative">
              <button
                className="flex size-[50px] items-center justify-center rounded-full transition-colors hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
                onClick={() => setIsProfileOpen((open) => !open)}
                aria-label="Abrir menú de cuenta"
                aria-expanded={isProfileOpen}
              >
                <img src={profileSvg} alt="" className="size-[50px]" />
              </button>
              {isProfileOpen && (
                <div className="font-inter-semibold absolute top-[62px] right-0 w-44 rounded-xl bg-white p-2 text-[16px] text-[#0f0140] shadow-xl">
                  <button
                    className="w-full rounded-lg px-3 py-2 text-left hover:bg-[#e5e5e7] focus-visible:outline-2 focus-visible:outline-[#5524bf]"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Mi cuenta
                  </button>
                </div>
              )}
            </div>

            <button
              className="flex size-11 flex-col items-center justify-center gap-1.5 rounded-md focus-visible:outline-2 focus-visible:outline-white md:hidden"
              onClick={() => setIsMenuOpen((open) => !open)}
              aria-label="Abrir navegación"
              aria-expanded={isMenuOpen}
            >
              <span className="h-0.5 w-6 bg-white" />
              <span className="h-0.5 w-6 bg-white" />
              <span className="h-0.5 w-6 bg-white" />
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto min-h-[calc(100dvh-125px)] max-w-[1580px] bg-[#e5e5e7] px-6 pt-[41px] pb-16 sm:px-10 lg:px-[58px]">
        <h1 className="font-inter-bold text-[34px] leading-normal sm:text-[40px]">
          Eventos disponibles
        </h1>

        <section
          className="mt-[20px] grid grid-cols-1 gap-x-[34px] gap-y-8 sm:grid-cols-2 lg:grid-cols-4"
          aria-label="Eventos disponibles"
        >
          {/* Renderizado dinámico con el componente EventCard */}
          {eventsList.map((event) => (
            <EventCard
              key={event.id}
              event={event}
              isSelected={selectedEventId === event.id}
              onSelect={handleSelectEvent}
            />
          ))}
        </section>
      </main>
    </div>
  );
}