package com.sigtr.cuentaabierta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoCuentaRepository extends JpaRepository<PagoCuenta, Long> {
    List<PagoCuenta> findByCuentaAbiertaId(Long cuentaAbiertaId);
    List<PagoCuenta> findByParticipanteId(Long participanteId);
}
