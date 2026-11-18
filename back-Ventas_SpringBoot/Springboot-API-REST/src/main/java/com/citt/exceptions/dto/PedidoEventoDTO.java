package com.citt.exceptions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEventoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVenta;
    private String cliente;
    private String producto;
    private Integer cantidad;
    private Double total;
    private String estado;
    private LocalDateTime fechaEvento;
}