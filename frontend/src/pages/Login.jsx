import { useRef, useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

export default function App() {
  const usernameRef = useRef(null);
  const [status, setStatus] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = (event) => {
    event.preventDefault();
    const form = event.currentTarget;

    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    setSubmitting(true);
    setStatus("");
    window.setTimeout(() => {
      setSubmitting(false);
      setStatus("Sesión iniciada correctamente");
    }, 650);
  };

  return (
    <main className="login-page">
      <div className="login-panel" />

      <header className="top-bar">
        <button
          className="account-button"
          type="button"
          aria-label="Ir al campo de usuario"
          onClick={() => usernameRef.current?.focus()}
        >
          <img src={profileSvg} alt="" />
        </button>
      </header>

      <section className="login-content" aria-labelledby="page-title">
        <div className="brand">
          <img src={logoSvg} alt="EventHub" className="hummingbird" />
          <h1>EventHub</h1>
        </div>

        <h2 id="page-title" className="form-title">
          Inicio de sesión
        </h2>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="field-card">
            <label className="sr-only" htmlFor="username">
              Usuario
            </label>
            <input
              ref={usernameRef}
              id="username"
              name="username"
              type="text"
              placeholder="Usuario"
              autoComplete="username"
              required
            />
            <label className="sr-only" htmlFor="password">
              Contraseña
            </label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="Contraseña"
              autoComplete="current-password"
              required
            />
          </div>

          <button className="submit-button" type="submit" disabled={submitting}>
            {submitting ? "Ingresando…" : "Ingresar"}
          </button>
          <p className="status-message" role="status" aria-live="polite">
            {status}
          </p>
        </form>
      </section>
    </main>
  );
}