package com.sitmmio.repository;

import com.sitmmio.common.model.Datagrama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatagramaRepository extends JpaRepository<Datagrama, Long> {
    List<Datagrama> findByIdBusOrderByTimestampDesc(String idBus);
    Optional<Datagrama> findFirstByIdBusOrderByTimestampDesc(String idBus);
    List<Datagrama> findByIdBus(String idBus);
    List<Datagrama> findByProcesadoFalse();
}
