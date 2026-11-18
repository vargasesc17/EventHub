export default function EventCard({
  event,
  onSelect,
  isSelected
}) {

  return (

    <button
      type="button"
      onClick={() => onSelect(event.id)}
      aria-pressed={isSelected}
      className="
        group
        flex!
        flex-col!
        items-start!
        justify-start!
        w-full
        min-w-0
        bg-transparent!
        p-0!
        border-0!
        shadow-none!
        text-left
        focus-visible:outline-2
        focus-visible:outline-[#0f0140]
      "
    >

      {/* TARJETA DEL EVENTO */}

      <div
        className={`
          block
          w-full
          h-[185px]
          rounded-[25px]
          bg-white
          overflow-hidden
          transition-all
          duration-200
          group-hover:-translate-y-1
          group-hover:shadow-lg

          ${isSelected
            ? "-translate-y-1 shadow-md"
            : ""}
        `}
      >

        <div className="
          w-full
          h-full
          flex
          items-center
          justify-center
          text-gray-400
          text-sm
        ">

          Sin imagen

        </div>

      </div>

      {/* NOMBRE DEL EVENTO */}

      <span className="
        font-inter-semibold
        font-semibold!
        mt-[13px]
        block
        px-[10px]
        text-[22px]
        leading-snug
        text-[#0f0140]
      ">

        {event.nombre}

      </span>

    </button>

  );

}