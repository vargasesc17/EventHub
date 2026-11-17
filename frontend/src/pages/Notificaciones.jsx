import { useEffect, useRef, useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

const initialNotifications = [
  { id: 1, title: "¡Tu boleta ha sido confirmada!", description: "El evento Lorem ipsum dolor sit ya tiene tu ingreso asegurado.", time: "Hace 10 minutos", read: false },
  { id: 2, title: "Actualización de evento", description: "Se han modificado los horarios de acceso para el evento Lorem ipsum.", time: "Hace 2 horas", read: false },
  { id: 3, title: "Recordatorio de check-in", description: "Recuerda tener tu código QR listo para el ingreso rápido.", time: "Ayer", read: true },
];

const navigation = ["Boletas", "Eventos", "Notificaciones", "Check-in"];

export default function Notificaciones() {
  const [activeSection, setActiveSection] = useState("Notificaciones");
  const [notifications, setNotifications] = useState(initialNotifications);
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

  const markAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((item) => (item.id === id ? { ...item, read: true } : item))
    );
  };

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="header-content">
          <a
            className="brand"
            href="#main-content"
            aria-label="EventHub, ir al contenido principal"
          >
            <img
              src={logoSvg}
              width="64"
              height="59"
              alt="EventHub"
            />
            <span>EventHub</span>
          </a>

          <nav className="main-nav" aria-label="Navegación principal">
            {navigation.map((item) => (
              <a
                key={item}
                className={activeSection === item ? "active" : ""}
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

          <div className="account-area" ref={accountMenuRef}>
            <button
              className="account-button"
              type="button"
              aria-label="Abrir menú de cuenta"
              aria-expanded={accountOpen}
              onClick={() => setAccountOpen((open) => !open)}
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

      <main id="main-content" className="content">
        <h1>Notificaciones</h1>

        <section className="notifications-list" aria-label="Lista de notificaciones" style={{ marginTop: "24px", display: "flex", flexDirection: "column", gap: "16px" }}>
          {notifications.map((notif) => (
            <article
              key={notif.id}
              className={`notification-card ${notif.read ? "read" : "unread"}`}
              style={{
                background: "#ffffff",
                padding: "20px",
                borderRadius: "16px",
                boxShadow: "0 4px 12px rgba(0,0,0,0.05)",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                  <h2 style={{ fontSize: "18px", fontWeight: "600", color: "#0f0140", margin: 0 }}>
                    {notif.title}
                  </h2>
                  {!notif.read && (
                    <span style={{ width: "10px", height: "10px", backgroundColor: "#5524bf", borderRadius: "50%" }} />
                  )}
                </div>
                <p style={{ margin: "8px 0 4px 0", color: "#373d4d", fontSize: "14px" }}>
                  {notif.description}
                </p>
                <span style={{ fontSize: "12px", color: "#888" }}>{notif.time}</span>
              </div>

              {!notif.read && (
                <button
                  type="button"
                  onClick={() => markAsRead(notif.id)}
                  style={{
                    backgroundColor: "#0f0140",
                    color: "white",
                    border: "none",
                    padding: "8px 14px",
                    borderRadius: "8px",
                    cursor: "pointer",
                    fontSize: "14px",
                  }}
                >
                  Marcar como leída
                </button>
              )}
            </article>
          ))}
        </section>
      </main>
    </div>
  );
}