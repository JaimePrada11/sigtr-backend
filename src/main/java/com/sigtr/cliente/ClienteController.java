package com.sigtr.cliente;

import com.sigtr.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final ClienteCarteraService clienteCarteraService;

    @GetMapping
    public List<ClienteDtos.ClienteResponse> listar() {
        Long tenantId = TenantContext.get();
        return clienteRepository.findByTenantIdAndActivoTrue(tenantId).stream()
                .map(ClienteDtos.ClienteResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDtos.ClienteResponse crear(@Valid @RequestBody ClienteDtos.ClienteRequest req) {
        // Idempotente por clientUuid: si ya se sincronizo antes, no duplica.
        Cliente cliente = clienteRepository.findByClientUuid(req.clientUuid())
                .orElseGet(Cliente::new);

        cliente.setTenantId(TenantContext.get());
        cliente.setClientUuid(req.clientUuid());
        cliente.setNombre(req.nombre());
        cliente.setTelefono(req.telefono());
        cliente.setLimiteCredito(req.limiteCredito() != null ? req.limiteCredito() : java.math.BigDecimal.ZERO);

        return ClienteDtos.ClienteResponse.from(clienteRepository.save(cliente));
    }

    @PutMapping("/{id}")
    public ClienteDtos.ClienteResponse actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody ClienteDtos.ClienteRequest req) {
        Long tenantId = TenantContext.get();
        Cliente cliente = clienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));

        cliente.setNombre(req.nombre());
        cliente.setTelefono(req.telefono());
        if (req.limiteCredito() != null) {
            cliente.setLimiteCredito(req.limiteCredito());
        }
        return ClienteDtos.ClienteResponse.from(clienteRepository.save(cliente));
    }

    /** Fase 3: estado de cuenta real (ventas fiadas + abonos, orden cronologico). */
    @GetMapping("/{id}/cuenta")
    public ClienteDtos.EstadoCuentaResponse estadoCuenta(@PathVariable Long id) {
        return clienteCarteraService.estadoCuenta(id);
    }

    /** Fase 3: limite de credito excedido, dias en mora, cliente de alto riesgo. */
    @GetMapping("/{id}/alertas")
    public ClienteDtos.AlertasResponse alertas(@PathVariable Long id) {
        return clienteCarteraService.alertas(id);
    }
}
