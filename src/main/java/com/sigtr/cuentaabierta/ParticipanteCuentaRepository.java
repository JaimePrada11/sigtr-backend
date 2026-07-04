package com.sigtr.cuentaabierta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipanteCuentaRepository extends JpaRepository<ParticipanteCuenta, Long> {
    List<ParticipanteCuenta> findByCuentaAbiertaId(Long cuentaAbiertaId);
    Optional<ParticipanteCuenta> findByIdAndCuentaAbiertaId(Long id, Long cuentaAbiertaId);
}
