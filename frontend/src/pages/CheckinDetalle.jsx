import { useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

const navigation = ["Boletas", "Eventos", "Notificaciones", "Check-in"];

export default function CheckinDetalle() {
  const [activeNav, setActiveNav] = useState("Check-in");
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  const selectNavigation = (item) => {
    setActiveNav(item);
    setMenuOpen(false);
  };

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="header-inner">
          <button
            className="brand"
            type="button"
            aria-label="Ir al inicio de EventHub"
            onClick={() => selectNavigation("Eventos")}
          >
            <img src={logoSvg} alt="EventHub" width="64" height="59" />
            <span>EventHub</span>
          </button>

          <button
            className="menu-button"
            type="button"
            aria-label={menuOpen ? "Cerrar menú" : "Abrir menú"}
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((open) => !open)}
          >
            <span />
            <span />
            <span />
          </button>

          <nav
            className={menuOpen ? "main-nav is-open" : "main-nav"}
            aria-label="Navegación principal"
          >
            {navigation.map((item) => (
              <button
                key={item}
                type="button"
                className={activeNav === item ? "nav-item is-active" : "nav-item"}
                aria-current={activeNav === item ? "page" : undefined}
                onClick={() => selectNavigation(item)}
              >
                {item}
              </button>
            ))}
          </nav>

          <div className="profile">
            <button
              className="profile-button"
              type="button"
              aria-label="Abrir menú de cuenta"
              aria-expanded={profileOpen}
              onClick={() => setProfileOpen((open) => !open)}
            >
              <img src={profileSvg} alt="Mi cuenta" width="50" height="50" />
            </button>
            {profileOpen && (
              <div className="profile-popover" role="menu">
                <p>Mi cuenta</p>
                <button type="button" role="menuitem" onClick={() => setProfileOpen(false)}>
                  Cerrar sesión
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <main className="page">
        <h1>Check-in</h1>

        <section className="checkin-layout" aria-label="Información de check-in">
          <div className="qr-frame">
            <img src="/assets/aa297.png" alt="Código QR de la boleta" width="281" height="281" />
          </div>

          <article className="ticket-card">
            <dl className="ticket-details">
              <div>
                <dt>Nombre:</dt>
                <dd>[Name][Lastname]</dd>
              </div>
              <div>
                <dt>Lugar:</dt>
                <dd>[place]</dd>
              </div>
              <div>
                <dt>Fecha:</dt>
                <dd>[date]</dd>
              </div>
              <div>
                <dt>Tipo de boleta:</dt>
                <dd>[ticket]</dd>
              </div>
              <div>
                <dt>Hora ingreso:</dt>
                <dd>{isCheckedIn ? "10:30" : "[hour]"}</dd>
              </div>
            </dl>

            <button
              className={isCheckedIn ? "status-button is-confirmed" : "status-button"}
              type="button"
              aria-pressed={isCheckedIn}
              onClick={() => setIsCheckedIn((checkedIn) => !checkedIn)}
            >
              {isCheckedIn ? "Ingreso confirmado" : "Estado"}
            </button>
          </article>
        </section>

        {activeNav !== "Check-in" && (
          <button
            className="navigation-notice"
            type="button"
            onClick={() => setActiveNav("Check-in")}
          >
            Vista “{activeNav}” seleccionada. Volver a Check-in
          </button>
        )}
      </main>
    </div>
  );
}