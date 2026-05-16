package com.sitmmio.repository;

import com.sitmmio.common.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByBusId(String busId);
    List<Evento> findByAckEnviadoFalse();
}
