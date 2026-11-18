import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

// SESIÓN Y CONTROL DE ROLES

import { SesionProvider, useSesion } from "./sesion/Sesion";
import { SoloRol } from "./sesion/SoloRol";

// PÁGINAS COMUNES

import Login from "./pages/comunes/Login";

// PÁGINAS DEL ORGANIZADOR

import GestionEventos from "./pages/organizador/GestionEventos";
import CrearEvento from "./pages/organizador/CrearEvento";
import EditarEvento from "./pages/organizador/EditarEvento";
import ReprogramarEvento from "./pages/organizador/ReprogramarEvento";
import AforoEvento from "./pages/organizador/AforoEvento";

// PÁGINAS DEL COMPRADOR

import Home from "./pages/comprador/Home";
import DetalleEvento from "./pages/comprador/DetalleEvento";
import ComprarBoleta from "./pages/comprador/ComprarBoleta";
import Boletas from "./pages/comprador/Boletas";
import DetalleBoleta from "./pages/comprador/DetalleBoleta";
import Notificaciones from "./pages/comprador/Notificaciones";

// PÁGINAS DEL STAFF

import CheckinSelector from "./pages/staff/CheckinSelector";
import CheckinDetalle from "./pages/staff/CheckinDetalle";

// RUTAS DE INICIO SEGÚN EL ROL

const inicioPorRol = {
  ORGANIZADOR: "/organizador",
  COMPRADOR: "/eventos",
  STAFF: "/checkin",
};

// REDIRECCIÓN SEGÚN LA SESIÓN

function InicioPorRol() {
  const { usuario } = useSesion();

  return (
    <Navigate
      to={inicioPorRol[usuario?.rol] || "/login"}
      replace
    />
  );
}

// APLICACIÓN PRINCIPAL

export default function App() {
  return (
    <SesionProvider>

      <BrowserRouter>

        <Routes>

          {/* RUTAS COMUNES */}

          <Route
            path="/"
            element={<InicioPorRol />}
          />

          <Route
            path="/login"
            element={<Login />}
          />

          {/* ============================ */}
          {/*          ORGANIZADOR         */}
          {/* ============================ */}

          {/* MIS EVENTOS */}

          <Route
            path="/organizador"
            element={
              <SoloRol roles={["ORGANIZADOR"]}>
                <GestionEventos />
              </SoloRol>
            }
          />

          {/* CREAR EVENTO */}

          <Route
            path="/organizador/crear"
            element={
              <SoloRol roles={["ORGANIZADOR"]}>
                <CrearEvento />
              </SoloRol>
            }
          />

          {/* EDITAR BORRADOR */}

          <Route
            path="/organizador/:eventoId/editar"
            element={
              <SoloRol roles={["ORGANIZADOR"]}>
                <EditarEvento />
              </SoloRol>
            }
          />

          {/* REPROGRAMAR EVENTO */}

          <Route
            path="/organizador/:eventoId/reprogramar"
            element={
              <SoloRol roles={["ORGANIZADOR"]}>
                <ReprogramarEvento />
              </SoloRol>
            }
          />

          {/* CONSULTAR AFORO */}

          <Route
            path="/organizador/:eventoId/aforo"
            element={
              <SoloRol roles={["ORGANIZADOR"]}>
                <AforoEvento />
              </SoloRol>
            }
          />

          {/* ============================ */}
          {/*           COMPRADOR          */}
          {/* ============================ */}

          {/* CATÁLOGO DE EVENTOS */}

          <Route
            path="/eventos"
            element={
              <SoloRol roles={["COMPRADOR"]}>
                <Home />
              </SoloRol>
            }
          />

          {/* DETALLE DEL EVENTO */}

          <Route
            path="/eventos/:eventoId"
            element={
              <SoloRol roles={["COMPRADOR"]}>
                <DetalleEvento />
              </SoloRol>
            }
          />

          {/* COMPRAR BOLETA */}

          <Route
            path="/eventos/:eventoId/comprar"
            element={
              <SoloRol roles={["COMPRADOR"]}>
                <ComprarBoleta />
              </SoloRol>
            }
          />

          {/* MIS BOLETAS */}

          <Route
            path="/boletas"
            element={
              <SoloRol roles={["COMPRADOR"]}>
                <Boletas />
              </SoloRol>
            }
          />

          {/* DETALLE DE LA BOLETA */}

          <Route
            path="/boletas/:boletaId"
            element={
              <SoloRol roles={["COMPRADOR"]}>
                <DetalleBoleta />
              </SoloRol>
            }
          />

          {/* NOTIFICACIONES */}

          <Route
            path="/notificaciones"
            element={
              <SoloRol roles={["COMPRADOR"]}>
                <Notificaciones />
              </SoloRol>
            }
          />

          {/* ============================ */}
          {/*             STAFF            */}
          {/* ============================ */}

          {/* SELECCIONAR EVENTO */}

          <Route
            path="/checkin"
            element={
              <SoloRol roles={["STAFF"]}>
                <CheckinSelector />
              </SoloRol>
            }
          />

          {/* CHECK-IN DEL EVENTO */}

          <Route
            path="/checkin/:eventoId"
            element={
              <SoloRol roles={["STAFF"]}>
                <CheckinDetalle />
              </SoloRol>
            }
          />

          {/* RUTAS NO ENCONTRADAS */}

          <Route
            path="*"
            element={<InicioPorRol />}
          />

        </Routes>

      </BrowserRouter>

    </SesionProvider>
  );
}