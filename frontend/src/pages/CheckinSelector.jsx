import { useEffect, useRef, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";
import EventCard from "../components/EventCard";

const eventsList = [
  { id: 1, nombre: "Conferencia Tech 2026", imagen: "" },
  { id: 2, nombre: "Taller de React y Tailwind", imagen: "" },
  { id: 3, nombre: "Meetup de IoT y Hardware", imagen: "" },
];

const navigation = [
  { name: "Boletas", path: "/boletas" },
  { name: "Eventos", path: "/eventos" },
  { name: "Notificaciones", path: "/notificaciones" },
  { name: "Check-in", path: "/checkin" },
];

export default function CheckinSelector() {
  const location = useLocation();
  const navigate = useNavigate();
  const [selectedEventId, setSelectedEventId] = useState(null);
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

  const handleSelectEvent = (id) => {
    setSelectedEventId(id);
    // Redirige automáticamente al detalle de check-in de ese evento específico
    navigate(`/checkin/${id}`);
  };

  return (
    <div className="min-h-dvh bg-[#7F8081] text-[#0f0140]">
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
              const isActive = location.pathname === item.path;
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
        <h1 className="font-bold text-[34px] leading-normal sm:text-[40px] text-[#0f0140]">
          Selecciona el evento para Check-in
        </h1>

        <section className="mt-[20px] grid grid-cols-1 gap-x-[34px] gap-y-8 sm:grid-cols-2 lg:grid-cols-4" aria-label="Eventos disponibles para check-in">
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