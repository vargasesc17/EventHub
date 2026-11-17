import { useRef, useState } from "react";
import logoSvg from "../assets/eventhubcolibri.svg";
import profileSvg from "../assets/account.svg";

export default function Login() {
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
    <main className="min-h-dvh bg-[#070024] text-white flex flex-col relative overflow-hidden">
      {/* Barra superior con el botón de cuenta */}
      <header className="w-full px-8 py-6 flex justify-end items-center">
        <button
          className="flex size-[50px] items-center justify-center rounded-full transition-colors hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-white"
          type="button"
          aria-label="Ir al campo de usuario"
          onClick={() => usernameRef.current?.focus()}
        >
          <img src={profileSvg} alt="Cuenta" className="size-[50px]" />
        </button>
      </header>

      {/* Contenido principal centrado */}
      <section className="flex-1 flex flex-col items-center justify-center px-4 pb-20" aria-labelledby="page-title">
        {/* Marca: Logo grande del colibrí y título EventHub */}
        <div className="flex items-center gap-4 mb-8">
          <img 
            src={logoSvg} 
            alt="EventHub Colibrí" 
            className="w-[120px] h-[110px] sm:w-[150px] sm:h-[138px] object-contain" 
          />
          <h1 className="text-white font-bold text-[50px] sm:text-[75px] tracking-wide leading-none">
            EventHub
          </h1>
        </div>

        {/* Botón o título decorativo "Inicio de sesión" estilo píldora */}
        <div id="page-title" className="bg-[#5524bf] text-white font-semibold text-[20px] px-8 py-3 rounded-full shadow-lg mb-8 tracking-wide">
          Inicio de sesión
        </div>

        {/* Formulario con tarjeta de fondo turquesa/azul verdoso como en Figma */}
        <form className="w-full max-w-[420px] flex flex-col items-center" onSubmit={handleSubmit}>
          <div className="w-full bg-[#32838d] p-8 rounded-[30px] shadow-2xl flex flex-col gap-5 mb-8">
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
              className="w-full bg-white text-[#0f0140] placeholder-gray-400 px-5 py-3.5 rounded-full text-[16px] font-medium outline-none shadow-inner focus:ring-4 focus:ring-[#5524bf]/40"
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
              className="w-full bg-white text-[#0f0140] placeholder-gray-400 px-5 py-3.5 rounded-full text-[16px] font-medium outline-none shadow-inner focus:ring-4 focus:ring-[#5524bf]/40"
            />
          </div>

          {/* Botón Ingresar */}
          <button 
            className="bg-[#5524bf] hover:bg-[#43199e] text-white font-bold text-[20px] px-12 py-3.5 rounded-full shadow-xl transition-all transform hover:-translate-y-0.5 active:translate-y-0 disabled:opacity-50"
            type="submit" 
            disabled={submitting}
          >
            {submitting ? "Ingresando…" : "Ingresar"}
          </button>

          <p className="status-message mt-4 text-sm font-medium" role="status" aria-live="polite">
            {status}
          </p>
        </form>
      </section>
    </main>
  );
}