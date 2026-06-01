package com.sitmmio.repository;

import com.sitmmio.common.model.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EstacionRepository extends JpaRepository<Estacion, Long> {
    List<Estacion> findByRutaId(String rutaId);
    List<Estacion> findByTipo(String tipo);
}
