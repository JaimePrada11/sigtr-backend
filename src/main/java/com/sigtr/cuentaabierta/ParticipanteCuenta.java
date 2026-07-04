package com.sigtr.cuentaabierta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "participantes_cuenta")
public class ParticipanteCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "cuenta_abierta_id", nullable = false)
    private Long cuentaAbiertaId;

    @Column(nullable = false)
    private String nombre; // ej. "Jairo" -- no requiere ser cliente registrado

    // Solo si se quiere poder fiar a su nombre al cerrar la cuenta.
    @Column(name = "cliente_id")
    private Long clienteId;
}
