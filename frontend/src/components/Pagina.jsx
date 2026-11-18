import Navbar from "./Navbar";

export default function Pagina({ titulo, children }) {

  return (

    <div className="min-h-dvh w-full bg-[#e5e5e7] text-[#0f0140]">

      {/* BARRA DE NAVEGACIÓN */}

      <Navbar />

      {/* CONTENIDO PRINCIPAL */}

      <main className="
        mx-auto
        w-full
        min-h-[calc(100dvh-125px)]
        max-w-[1440px]
        px-6
        pt-[41px]
        pb-16
        sm:px-10
        lg:px-[58px]
      ">

        {/* TÍTULO DE LA PÁGINA */}

        <h1 className="
          mb-[26px]
          font-inter-bold
          font-bold!
          text-[34px]
          leading-normal
          sm:text-[40px]
          text-[#0f0140]
        ">

          {titulo}

        </h1>

        {/* CONTENIDO DE CADA PANTALLA */}

        {children}

      </main>

    </div>

  );

}