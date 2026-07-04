package com.sigtr.caja;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FlujoCajaDtos {

    public record FlujoCajaResponse(
            LocalDate desde,
            LocalDate hasta,
            BigDecimal ingresosReales,
            BigDecimal egresosReales,
            BigDecimal neto,
            BigDecimal carteraPendienteCobrar,   // proyeccion: lo que deben los clientes
            BigDecimal cuentasPorPagarPendientes // proyeccion: lo que se debe a proveedores
    ) {
    }
}
