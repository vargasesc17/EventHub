export default function EstadoCarga({ cargando, error }) {
  return <>{cargando && <p role="status">Cargando...</p>}{error && <p className="eh-error" role="alert">{error.detail || error.message || 'No se pudo completar la operación.'}</p>}</>;
}
