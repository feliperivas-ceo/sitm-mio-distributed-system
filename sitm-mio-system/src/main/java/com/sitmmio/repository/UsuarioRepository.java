package com.sitmmio.repository;

import com.sitmmio.common.model.Rol;
import com.sitmmio.common.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findByRol(Rol rol);
    List<Usuario> findByZonaId(Long zonaId);
}
