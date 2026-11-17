import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Eventos from "./pages/Eventos";
import Boletas from "./pages/Boletas";
import Notificaciones from "./pages/Notificaciones";
import CheckinSelector from "./pages/CheckinSelector";
import CheckinDetalle from "./pages/CheckinDetalle";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/home" element={<Home />} /> 
        <Route path="/login" element={<Login />} />
        <Route path="/eventos" element={<Eventos />} />
        <Route path="/boletas" element={<Boletas />} />
        <Route path="/notificaciones" element={<Notificaciones />} />
        <Route path="/checkin" element={<CheckinSelector />} />
        <Route path="/checkin/:eventoId" element={<CheckinDetalle />} />
      </Routes>
    </BrowserRouter>
  );
}