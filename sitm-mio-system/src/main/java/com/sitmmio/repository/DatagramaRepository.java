package com.sitmmio.repository;

import com.sitmmio.common.model.Datagrama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatagramaRepository extends JpaRepository<Datagrama, Long> {
    List<Datagrama> findByIdBus(String busId);
    List<Datagrama> findByProcesadoFalse();
}
