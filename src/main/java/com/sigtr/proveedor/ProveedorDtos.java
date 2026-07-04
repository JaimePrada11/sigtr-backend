package com.sigtr.proveedor;

import jakarta.validation.constraints.NotBlank;

public class ProveedorDtos {

    public record ProveedorRequest(
            @NotBlank String clientUuid,
            @NotBlank String nombre,
            String telefono,
            String email,
            String contacto,
            String direccion
    ) {
    }

    public record ProveedorResponse(Long id, String nombre, String telefono, String email,
                                     String contacto, String direccion, boolean activo) {
        public static ProveedorResponse from(Proveedor p) {
            return new ProveedorResponse(p.getId(), p.getNombre(), p.getTelefono(), p.getEmail(),
                    p.getContacto(), p.getDireccion(), p.isActivo());
        }
    }
}
