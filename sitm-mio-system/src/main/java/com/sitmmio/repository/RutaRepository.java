package com.sitmmio.repository;

import com.sitmmio.common.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, String> {
    Optional<Ruta> findByNombre(String nombre);
}
