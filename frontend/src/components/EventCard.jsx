export default function EventCard({ event, onSelect, isSelected }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(event.id)}
      className="group min-w-0 rounded-[21px] text-left focus-visible:outline-none"
      aria-pressed={isSelected}
    >
      <div
        className={`block h-[207px] rounded-[21px] bg-white overflow-hidden transition duration-200 group-hover:-translate-y-1 group-hover:shadow-lg group-focus-visible:outline-4 group-focus-visible:outline-offset-4 group-focus-visible:outline-[#5524bf] ${
          isSelected
            ? "-translate-y-1 shadow-[0_12px_28px_rgba(15,1,64,0.18)] outline-4 outline-offset-4 outline-[#5524bf]"
            : ""
        }`}
      >
        {/* Si el evento trae una imagen de la API o base de datos, la muestra; si no, queda el cuadro limpio */}
        {event.imagen ? (
          <img src={event.imagen} alt={event.titulo} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm">
            Sin imagen
          </div>
        )}
      </div>
      <span className="font-inter-semibold mt-[13px] block px-[17px] text-[24px] leading-normal text-[#0f0140]">
        {event.titulo}
      </span>
    </button>
  );
}