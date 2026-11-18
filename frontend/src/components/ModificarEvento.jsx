import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Pagina from './Pagina';
import FormularioEvento from './FormularioEvento';
import EstadoCarga from './EstadoCarga';
import { obtenerEvento, editarEvento, reprogramarEvento } from '../api/eventos';
import useConsulta from '../api/useConsulta';
import { useSesion } from '../sesion/Sesion';
export default function ModificarEvento({ reprogramar = false }) {
  const { eventoId } = useParams();
  const { usuario } = useSesion();
  const { datos: evento, cargando, error } = useConsulta(obtenerEvento, eventoId);
  const [fallo, setFallo] = useState(null);
  const [ocupado, setOcupado] = useState(false);
  const navigate = useNavigate();
  const permitido = evento?.organizadorId === usuario.id && evento?.estado === (reprogramar ? 'PUBLICADO' : 'BORRADOR');
  async function guardar(datos) { setOcupado(true); setFallo(null); try { await (reprogramar ? reprogramarEvento : editarEvento)(eventoId, datos); navigate('/organizador'); } catch (e) { setFallo(e); } finally { setOcupado(false); } }
  return <Pagina titulo={reprogramar ? 'Reprogramar evento' : 'Editar borrador'}><EstadoCarga cargando={cargando} error={error || fallo} />{evento && (permitido ? <FormularioEvento key={evento.id} evento={evento} reprogramar={reprogramar} ocupado={ocupado} onGuardar={guardar} /> : <p>No puedes modificar este evento con tu sesión o su estado actual.</p>)}</Pagina>;
}
