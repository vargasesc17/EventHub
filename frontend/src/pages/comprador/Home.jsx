import { useNavigate } from "react-router-dom";

import Navbar from "../../components/Navbar";
import EventCard from "../../components/EventCard";

import { EVENTOS_PRUEBA } from "../../data/eventosPrueba";

export default function Home() {

  const navigate = useNavigate();

  const handleSelectEvent = (id) => {
    navigate(`/eventos/${id}`);
  };

  return (

    <div className="min-h-dvh bg-[#e5e5e7] text-[#0f0140]">

      {/* BARRA DE NAVEGACIÓN */}

      <Navbar />

      {/* CONTENIDO PRINCIPAL */}

      <main className="
        mx-auto
        min-h-[calc(100dvh-125px)]
        max-w-[1440px]
        px-6
        pt-[41px]
        pb-16
        sm:px-10
        lg:px-[58px]
      ">

        {/* TÍTULO */}

        <h1 className="
          font-inter-bold
          text-[34px]
          leading-normal
          sm:text-[40px]
        ">

          Eventos disponibles

        </h1>

        {/* TARJETAS DE EVENTOS */}

        <section
          className="
            mt-[26px]
            grid
            grid-cols-1
            gap-x-[28px]
            gap-y-8
            sm:grid-cols-2
            lg:grid-cols-3
            xl:grid-cols-4
          "
          aria-label="Eventos disponibles"
        >

          {EVENTOS_PRUEBA.map((event) => (

            <EventCard
              key={event.id}
              event={event}
              isSelected={false}
              onSelect={handleSelectEvent}
            />

          ))}

        </section>

      </main>

    </div>

  );

}