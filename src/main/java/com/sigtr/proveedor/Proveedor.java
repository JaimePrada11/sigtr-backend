package com.sigtr.proveedor;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "proveedores")
public class Proveedor extends TenantSyncEntity {

    @Column(nullable = false)
    private String nombre;

    private String telefono;
    private String email;
    private String contacto;
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;
}
