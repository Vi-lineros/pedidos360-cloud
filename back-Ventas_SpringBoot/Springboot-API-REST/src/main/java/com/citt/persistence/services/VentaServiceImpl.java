package com.citt.persistence.services;

import com.citt.exceptions.VentaNotFoundException;
import com.citt.exceptions.dto.PedidoEventoDTO;
import com.citt.persistence.entity.Venta;
import com.citt.persistence.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService{

    private static final Logger LOGGER = LoggerFactory.getLogger(VentaServiceImpl.class);

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private VentaKafkaProducer ventaKafkaProducer;

    @Override
    public List<Venta> findAllVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta saveVenta(Venta venta) {
        Venta ventaGuardada = ventaRepository.save(venta);
        try {
            ventaKafkaProducer.enviarEventoVenta(PedidoEventoDTO.builder()
                    .idVenta(ventaGuardada.getIdVenta())
                    .total((double) ventaGuardada.getValorCompra())
                    .estado("VENTA_CREADA")
                    .fechaEvento(LocalDateTime.now())
                    .build());
        } catch (Exception exception) {
            LOGGER.error("Venta {} guardada, pero no se pudo publicar el evento Kafka",
                    ventaGuardada.getIdVenta(), exception);
        }
        return ventaGuardada;
    }

    @Override
    public void procesarEventoDespacho(PedidoEventoDTO evento) {
        if (evento == null || evento.getIdVenta() == null) {
            throw new IllegalArgumentException("El evento de despacho debe incluir el ID de la venta");
        }

        Venta venta = ventaRepository.findById(evento.getIdVenta())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la venta asociada al evento de despacho: " + evento.getIdVenta()));
        if (!Boolean.TRUE.equals(venta.getDespachoGenerado())) {
            venta.setDespachoGenerado(true);
            ventaRepository.save(venta);
        }
        LOGGER.info("Confirmación de despacho procesada para venta {}", evento.getIdVenta());
    }

    @Override
    public Venta updateVenta(Long idVenta, Venta venta) throws VentaNotFoundException {
        Optional<Venta> optionalVenta = ventaRepository.findById(idVenta);

        if (optionalVenta.isPresent()) {
            Venta ventaDB = optionalVenta.get();

            if (Objects.nonNull(venta.getDireccionCompra()) && !venta.getDireccionCompra().trim().isEmpty()) {
                ventaDB.setDireccionCompra(venta.getDireccionCompra());
            }

            if (Objects.nonNull(venta.getValorCompra())) {
                ventaDB.setValorCompra(venta.getValorCompra());
            }

            if (Objects.nonNull(venta.getFechaCompra())) {
                ventaDB.setFechaCompra(venta.getFechaCompra());
            }

            if (Objects.nonNull(venta.getDespachoGenerado())) {
                ventaDB.setDespachoGenerado(venta.getDespachoGenerado());
            }

            // Guardar la venta actualizada en la BD
            return ventaRepository.save(ventaDB);
        } else {
            throw new VentaNotFoundException("!No es posible actualizar! No existe venta con ID: " + idVenta);
        }
    }

    @Override
    public void deleteVenta(Long idVenta) throws VentaNotFoundException {
        Optional<Venta> venta = ventaRepository.findById(idVenta);
        if(!venta.isPresent()) {
            throw new VentaNotFoundException("¡No es posible eliminar! No existe venta con el ID: " + idVenta);
        }else {
            ventaRepository.deleteById(idVenta);
        }
    }

    @Override
    public Venta findById(Long idVenta) throws VentaNotFoundException {
        Optional<Venta> venta = ventaRepository.findById(idVenta);
        if(!venta.isPresent()) throw new VentaNotFoundException("Venta no encontrada con el ID: " + idVenta);
        return venta.get();
    }
}
