import { useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";


const navigation = ["Boletas", "Eventos", "Notificaciones", "Check-in"];
const events = ["evento1", "evento2", "evento3"];

export default function Home() {
  const [activeSection, setActiveSection] = useState("Eventos");
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const selectSection = (section) => {
    setActiveSection(section);
    setIsMenuOpen(false);
  };

  return (
    <div className="min-h-dvh bg-[#373d4d] text-[#0f0140]">
      <header className="relative z-20 h-[125px] bg-[#0f0140] text-white">
        <div className="mx-auto flex h-full max-w-[1192px] items-center justify-between px-6 lg:px-0">
          <button
            className="flex items-center gap-0.5 rounded-md focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-white"
            onClick={() => selectSection("Eventos")}
            aria-label="Ir a Eventos"
          >
            <img src={logoSvg} alt="" className="h-[59px] w-[64px]" />
            <span className="font-inter-bold text-[30px] leading-none sm:text-[36px]">
              EventHub
            </span>
          </button>

          <nav
            className={`${
              isMenuOpen ? "flex" : "hidden"
            } absolute top-[125px] right-0 left-0 flex-col bg-[#0f0140] px-6 pb-6 md:static md:flex md:flex-row md:items-center md:gap-9 md:bg-transparent md:p-0 lg:gap-[43px]`}
            aria-label="Navegación principal"
          >
            {navigation.map((item) => (
              <button
                key={item}
                onClick={() => selectSection(item)}
                className={`font-inter-bold rounded-sm py-3 text-left text-[20px] leading-normal transition-colors focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-white md:py-0 md:text-[24px] ${
                  activeSection === item
                    ? "bg-[#070024] text-white  " // Azul más oscuro y marcado para la sección activa
                    : "text-white/80 hover:bg-[#070024] hover:text-white" // Azul más oscuro al pasar el mouse
                }`}
                aria-current={activeSection === item ? "page" : undefined}
              >
                {item}
              </button>
            ))}
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
          {events.map((eventName) => {
            const isSelected = selectedEvent === eventName;

            return (
              <button
                key={eventName}
                type="button"
                onClick={() => setSelectedEvent(isSelected ? null : eventName)}
                className="group min-w-0 rounded-[21px] text-left focus-visible:outline-none"
                aria-pressed={isSelected}
              >
                <span
                  className={`block h-[207px] rounded-[21px] bg-white transition duration-200 group-hover:-translate-y-1 group-hover:shadow-lg group-focus-visible:outline-4 group-focus-visible:outline-offset-4 group-focus-visible:outline-[#5524bf] ${
                    isSelected
                      ? "-translate-y-1 shadow-[0_12px_28px_rgba(15,1,64,0.18)] outline-4 outline-offset-4 outline-[#5524bf]"
                      : ""
                  }`}
                  aria-hidden="true"
                />
                <span className="font-inter-semibold mt-[13px] block px-[17px] text-[24px] leading-normal">
                  {eventName}
                </span>
              </button>
            );
          })}
        </section>

        <p className="sr-only" aria-live="polite">
          {selectedEvent
            ? `${selectedEvent} seleccionado`
            : "Ningún evento seleccionado"}
        </p>
      </main>
    </div>
  );
}