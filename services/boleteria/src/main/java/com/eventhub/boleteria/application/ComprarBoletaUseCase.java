package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;

import java.time.Clock;

/**
 * Compra de una boleta (RF-01, RNF-01). Debe ejecutarse dentro de UNA transacción: si algo falla
 * después de descontar el cupo (por ejemplo, el pago), el descuento se deshace.
 */
public class ComprarBoletaUseCase {

    private final AforoRepository aforos;
    private final InventarioRepository inventarios;
    private final BoletaRepository boletas;
    private final RegistroEstrategias estrategias;
    private final RegistroFabricas fabricas;
    private final PasarelaPagos pasarela;
    private final GeneradorQr generadorQr;
    private final PublicadorBoleteria publicador;
    private final Clock reloj;
    private final int umbralAlerta;

    public ComprarBoletaUseCase(AforoRepository aforos, InventarioRepository inventarios, BoletaRepository boletas,
                                RegistroEstrategias estrategias, RegistroFabricas fabricas, PasarelaPagos pasarela,
                                GeneradorQr generadorQr, PublicadorBoleteria publicador, Clock reloj,
                                int umbralAlerta) {
        this.aforos = aforos;
        this.inventarios = inventarios;
        this.boletas = boletas;
        this.estrategias = estrategias;
        this.fabricas = fabricas;
        this.pasarela = pasarela;
        this.generadorQr = generadorQr;
        this.publicador = publicador;
        this.reloj = reloj;
        this.umbralAlerta = umbralAlerta;
    }

    public CompraRealizada comprar(Compra compra) {
        var eventoId = compra.eventoId();
        var categoria = compra.categoria();

        // 1. ¿El evento está a la venta y vende esta categoría?
        var evento = aforos.buscar(eventoId).orElseThrow(() -> new EventoNoEncontradoException(eventoId));
        if (!evento.aceptaVentas()) {
            throw new ReglaDeNegocioException("El evento " + evento.nombre() + " no está a la venta");
        }
        var inventario = inventarios.buscar(eventoId, categoria)
                .orElseThrow(() -> new ReglaDeNegocioException("El evento no vende boletas " + categoria));
        // Solo para responder rápido. NO protege contra la concurrencia: eso lo hace descontarCupo.
        if (inventario.disponibles() == 0) {
            throw new SinCuposException(categoria);
        }

        // 2. Precio (Strategy, #22) y boleta (Factory Method, #21). Se valida ANTES de tocar la base.
        long precio = estrategias.estrategiaPara(inventario.tipoPrecio())
                .calcular(inventario.contextoPrecio(reloj.instant()));
        var boleta = fabricas.fabricaPara(categoria).emitir(compra, precio);

        // 3. Descuento atómico (ADR-0003): si otra compra se llevó el último cupo, aquí se detecta.
        if (!inventarios.descontarCupo(eventoId, categoria)) {
            throw new SinCuposException(categoria);
        }
        aforos.sumarVenta(eventoId);

        // 4. Cobro. Si se rechaza, la transacción deshace el descuento.
        if (precio > 0) {
            var pago = pasarela.cobrar(compra.compradorId(), precio,
                    "Boleta " + categoria + " · " + evento.nombre());
            if (!pago.aprobado()) {
                throw new PagoRechazadoException(pago.motivoRechazo());
            }
        }

        // 5. Guardar, generar el QR y avisar.
        boletas.guardar(boleta);
        var codigoQr = generadorQr.generar(boleta.getId());
        publicador.boletaComprada(boleta, compra, evento, codigoQr);

        // 6. Alerta del 90 %: solo la compra que logra marcarla la publica.
        if (aforos.marcarAlertaSiAlcanzaUmbral(eventoId, umbralAlerta)) {
            publicador.aforoCritico(aforos.buscar(eventoId).orElseThrow(), umbralAlerta);
        }
        return new CompraRealizada(boleta, codigoQr);
    }
}