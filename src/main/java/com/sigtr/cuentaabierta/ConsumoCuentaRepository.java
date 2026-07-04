package com.sigtr.cuentaabierta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumoCuentaRepository extends JpaRepository<ConsumoCuenta, Long> {
    List<ConsumoCuenta> findByCuentaAbiertaId(Long cuentaAbiertaId);
    List<ConsumoCuenta> findByParticipanteId(Long participanteId);
}
