package com.sitmmio.repository;

import com.sitmmio.common.model.Bus;
import com.sitmmio.common.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusRepository extends JpaRepository<Bus, String> {
    List<Bus> findByRutaId(String rutaId);
    List<Bus> findByEstado(String estado);
    List<Bus> findByPrioridad(Prioridad prioridad);
}
