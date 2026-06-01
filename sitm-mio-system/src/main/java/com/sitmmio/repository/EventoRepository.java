package com.sitmmio.repository;

import com.sitmmio.common.model.Evento;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByBusBusIdOrderByTimestampDesc(String busId);
    List<Evento> findByRutaIdOrderByTimestampDesc(String rutaId);
    List<Evento> findByPrioridad(Prioridad prioridad);
    List<Evento> findByTipoEvento(TipoEvento tipoEvento);
    List<Evento> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
    long countByPrioridad(Prioridad prioridad);

    @Query("SELECT e.ruta.id, AVG(e.bus.velocidad) FROM Evento e WHERE e.bus.velocidad IS NOT NULL GROUP BY e.ruta.id")
    List<Object[]> avgVelocidadPorRuta();

    @Query("SELECT MONTH(e.timestamp), COUNT(e) FROM Evento e GROUP BY MONTH(e.timestamp)")
    List<Object[]> eventosPorMes();
}
