package com.citt.persistence.services;

import com.citt.exceptions.DespachoNotFoundException;
import com.citt.exceptions.dto.PedidoEventoDTO;
import com.citt.persistence.entity.Despacho;
import com.citt.persistence.repository.DespachoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DespachoServiceImpl implements DespachoService{

    private static final Logger LOGGER = LoggerFactory.getLogger(DespachoServiceImpl.class);

    @Autowired
    private DespachoRepository despachoRepository;

    @Autowired
    private DespachoKafkaProducer despachoKafkaProducer;

    @Override
    public List<Despacho> findAllDespachos() {
        return despachoRepository.findAll();
    }

    @Override
    public Despacho saveDespacho(Despacho despacho) {
        Despacho despachoGuardado = despachoRepository.save(despacho);
        publicarEvento(despachoGuardado);
        return despachoGuardado;
    }

    @Override
    public void procesarEventoVenta(PedidoEventoDTO evento) {
        if (evento == null || evento.getIdVenta() == null) {
            throw new IllegalArgumentException("El evento de venta debe incluir el ID de la venta");
        }

        if (despachoRepository.existsByIdCompra(evento.getIdVenta())) {
            LOGGER.info("Ya existe despacho para venta {}; se omite la creación", evento.getIdVenta());
            return;
        }

        Despacho despachoInicial = new Despacho();
        despachoInicial.setIdCompra(evento.getIdVenta());
        despachoInicial.setFechaDespacho(LocalDate.now());
        despachoInicial.setIntento(0);
        if (evento.getTotal() != null) {
            despachoInicial.setValorCompra(Math.round(evento.getTotal()));
        }

        Despacho despachoGuardado = despachoRepository.save(despachoInicial);
        LOGGER.info("Despacho inicial creado para venta {} con despacho {}",
                evento.getIdVenta(), despachoGuardado.getIdDespacho());
        publicarEvento(despachoGuardado);
    }

    @Override
    public Despacho updateDespacho(Long idDespacho, Despacho despacho) throws DespachoNotFoundException {
        Despacho despachoActualizado = despachoRepository.findById(idDespacho).map(existingDespacho -> {
            existingDespacho.setFechaDespacho(despacho.getFechaDespacho());
            existingDespacho.setPatenteCamion(despacho.getPatenteCamion());
            existingDespacho.setIntento(despacho.getIntento());
            existingDespacho.setIdCompra(despacho.getIdCompra());
            existingDespacho.setDireccionCompra(despacho.getDireccionCompra());
            existingDespacho.setValorCompra(despacho.getValorCompra());
            existingDespacho.setDespachado(despacho.isDespachado());
            return despachoRepository.save(existingDespacho);
        }).orElseThrow(() -> new DespachoNotFoundException("Despacho no encontrado con ID: " + idDespacho));
        publicarEvento(despachoActualizado);
        return despachoActualizado;
    }

    @Override
    public void deleteDespacho(Long idDespacho) throws DespachoNotFoundException {
        Optional<Despacho> despacho = despachoRepository.findById(idDespacho);
        if(!despacho.isPresent()){
            throw new DespachoNotFoundException("¡No es posible eliminar! No existe despacho con el ID:" + idDespacho);
        }else {
            despachoRepository.deleteById(idDespacho);
        }
    }

    @Override
    public Despacho findById(Long idDespacho) throws DespachoNotFoundException {
        Optional<Despacho> despacho = despachoRepository.findById(idDespacho);
        if(!despacho.isPresent()) throw new DespachoNotFoundException("¡No existe despacho con el ID:" + idDespacho);
        return despacho.get();
    }

    private void publicarEvento(Despacho despacho) {
        try {
            PedidoEventoDTO evento = PedidoEventoDTO.builder()
                    .idVenta(despacho.getIdCompra())
                    .total(despacho.getValorCompra() == null ? null : despacho.getValorCompra().doubleValue())
                    .estado(despacho.isDespachado() ? "DESPACHADO" : "DESPACHO_CREADO")
                    .fechaEvento(LocalDateTime.now())
                    .build();
            despachoKafkaProducer.enviarEventoDespacho(evento);
        } catch (Exception exception) {
            LOGGER.error("Despacho {} guardado, pero no se pudo publicar el evento Kafka",
                    despacho.getIdDespacho(), exception);
        }
    }
}
