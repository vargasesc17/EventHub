import { useEffect, useRef, useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

const events = [
  { id: 1, name: "Lorem ipsum dolor sit" },
  { id: 2, name: "Lorem ipsum dolor sit" },
  { id: 3, name: "Lorem ipsum dolor sit" },
];

const navigation = ["Boletas", "Eventos", "Notificaciones", "Check-in"];

export default function CheckinSelector() {
  const [activeSection, setActiveSection] = useState("Check-in");
  const [monitoredEvent, setMonitoredEvent] = useState(null);
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
    <div className="app-shell" data-node-id="2016:243">
      <header className="site-header" data-node-id="2016:253">
        <div className="header-content">
          <a
            className="brand"
            href="#main-content"
            aria-label="EventHub, ir al contenido principal"
            data-node-id="2016:254"
          >
            <img
              src={logoSvg}
              width="64"
              height="59"
              alt="EventHub"
              data-node-id="2016:255"
            />
            <span data-node-id="2016:267">EventHub</span>
          </a>

          <nav className="main-nav" aria-label="Navegación principal" data-node-id="2016:268">
            {navigation.map((item) => (
              <a
                key={item}
                className={activeSection === item ? "active" : ""}
                href={item === "Check-in" ? "#main-content" : `#${item.toLowerCase()}`}
                onClick={(event) => {
                  if (item !== "Check-in") event.preventDefault();
                  setActiveSection(item);
                }}
              >
                {item}
              </a>
            ))}
          </nav>

          <div className="account-area" ref={accountMenuRef}>
            <button
              className="account-button"
              type="button"
              aria-label="Abrir menú de cuenta"
              aria-expanded={accountOpen}
              onClick={() => setAccountOpen((open) => !open)}
              data-node-id="2016:284"
            >
              <img src={profileSvg} width="50" height="50" alt="Mi cuenta" />
            </button>
            {accountOpen && (
              <div className="account-menu" role="menu">
                <strong>Mi cuenta</strong>
                <button type="button" role="menuitem" onClick={() => setAccountOpen(false)}>
                  Ver perfil
                </button>
                <button type="button" role="menuitem" onClick={() => setAccountOpen(false)}>
                  Cerrar sesión
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <main id="main-content" className="content" data-node-id="2016:273">
        <h1 data-node-id="2016:274">Lorem ipsum dolor sit</h1>

        <section className="event-grid" aria-label="Eventos disponibles">
          {events.map((event, index) => {
            const isMonitored = monitoredEvent === event.id;
            const eventNodeIds = [
              ["2016:275", "2016:277", "2016:276", "2016:290", "2016:291"],
              ["2016:281", "2016:283", "2016:282", "2016:293", "2016:294"],
              ["2016:278", "2016:280", "2016:279", "2016:245", "2016:246"],
            ][index];

            return (
              <article className="event-card" key={event.id} data-node-id={eventNodeIds[0]}>
                <div className="event-image" aria-hidden="true" data-node-id={eventNodeIds[1]} />
                <h2 data-node-id={eventNodeIds[2]}>{event.name}</h2>
                <button
                  type="button"
                  className={isMonitored ? "monitor-button monitored" : "monitor-button"}
                  aria-pressed={isMonitored}
                  onClick={() => setMonitoredEvent(isMonitored ? null : event.id)}
                  data-node-id={eventNodeIds[3]}
                >
                  <span data-node-id={eventNodeIds[4]}>
                    {isMonitored ? "Monitoreando check-in" : "Monitoreo de check-in"}
                  </span>
                </button>
              </article>
            );
          })}
        </section>
        <p className="sr-only" aria-live="polite">
          {monitoredEvent
            ? `Monitoreo de check-in activado para el evento ${monitoredEvent}`
            : "Monitoreo de check-in desactivado"}
        </p>
      </main>
    </div>
  );
}