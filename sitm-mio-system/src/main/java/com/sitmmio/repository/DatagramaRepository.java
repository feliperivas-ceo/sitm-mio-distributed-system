package com.sitmmio.repository;

import com.sitmmio.common.model.Datagrama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatagramaRepository extends JpaRepository<Datagrama, Long> {
    List<Datagrama> findByBusIdOrderByTimestampDesc(String busId);
    Optional<Datagrama> findFirstByBusIdOrderByTimestampDesc(String busId);
}
