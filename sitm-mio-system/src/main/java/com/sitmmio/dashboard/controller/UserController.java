package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.*;
import com.sitmmio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ZonaRepository zonaRepo;
    @Autowired private BusRepository busRepo;
    @Autowired private EstacionRepository estacionRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- Usuarios ---
    @GetMapping("/users")
    public List<Usuario> getAllUsers() {
        return usuarioRepo.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Usuario> getUser(@PathVariable Long id) {
        return usuarioRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body) {
        if (usuarioRepo.findByUsername((String) body.get("username")).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username ya existe"));
        }
        Usuario u = new Usuario();
        u.setUsername((String) body.get("username"));
        u.setPassword(passwordEncoder.encode((String) body.get("password")));
        u.setNombre((String) body.get("nombre"));
        u.setEmail((String) body.get("email"));
        u.setRol(Rol.valueOf((String) body.get("rol")));
        u.setActivo(true);
        if (body.get("zonaId") != null) {
            zonaRepo.findById(Long.valueOf(body.get("zonaId").toString()))
                .ifPresent(u::setZona);
        }
        return ResponseEntity.ok(usuarioRepo.save(u));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return usuarioRepo.findById(id).map(u -> {
            if (body.get("nombre") != null) u.setNombre((String) body.get("nombre"));
            if (body.get("email") != null) u.setEmail((String) body.get("email"));
            if (body.get("rol") != null) u.setRol(Rol.valueOf((String) body.get("rol")));
            if (body.get("activo") != null) u.setActivo((Boolean) body.get("activo"));
            if (body.get("password") != null && !((String)body.get("password")).isBlank()) {
                u.setPassword(passwordEncoder.encode((String) body.get("password")));
            }
            if (body.get("zonaId") != null) {
                zonaRepo.findById(Long.valueOf(body.get("zonaId").toString())).ifPresent(u::setZona);
            }
            return ResponseEntity.ok(usuarioRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!usuarioRepo.existsById(id)) return ResponseEntity.notFound().build();
        usuarioRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
    }

    // --- Zonas ---
    @GetMapping("/zonas")
    public List<Zona> getAllZonas() {
        return zonaRepo.findAll();
    }

    @GetMapping("/zonas/{id}")
    public ResponseEntity<?> getZonaDetalle(@PathVariable Long id) {
        return zonaRepo.findById(id).map(zona -> {
            List<Usuario> controladores = usuarioRepo.findByZonaId(id);
            List<String> rutaIds = controladores.stream()
                .filter(u -> u.getZona() != null)
                .map(u -> u.getZona().getNombre())
                .distinct().toList();
            return ResponseEntity.ok(Map.of(
                "zona", zona,
                "controladores", controladores,
                "totalControladores", controladores.size()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/zonas")
    public Zona createZona(@RequestBody Zona zona) {
        return zonaRepo.save(zona);
    }

    @PutMapping("/zonas/{id}")
    public ResponseEntity<Zona> updateZona(@PathVariable Long id, @RequestBody Zona body) {
        return zonaRepo.findById(id).map(z -> {
            z.setNombre(body.getNombre());
            z.setDescripcion(body.getDescripcion());
            return ResponseEntity.ok(zonaRepo.save(z));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/zonas/{id}")
    public ResponseEntity<?> deleteZona(@PathVariable Long id) {
        if (!zonaRepo.existsById(id)) return ResponseEntity.notFound().build();
        zonaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Zona eliminada"));
    }

    /** Asignar controlador a zona (R8) */
    @PostMapping("/zonas/{zonaId}/controladores/{userId}")
    public ResponseEntity<?> asignarControladorAZona(
            @PathVariable Long zonaId,
            @PathVariable Long userId) {
        return usuarioRepo.findById(userId).map(u -> {
            return zonaRepo.findById(zonaId).map(zona -> {
                u.setZona(zona);
                return ResponseEntity.ok((Object) Map.of(
                    "mensaje", "Controlador " + u.getNombre() + " asignado a zona " + zona.getNombre(),
                    "usuario", u.getUsername(),
                    "zona", zona.getNombre()
                ));
            }).orElse(ResponseEntity.notFound().build());
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Desasignar controlador de su zona (R8) */
    @DeleteMapping("/zonas/controladores/{userId}")
    public ResponseEntity<?> desasignarControlador(@PathVariable Long userId) {
        return usuarioRepo.findById(userId).map(u -> {
            u.setZona(null);
            usuarioRepo.save(u);
            return ResponseEntity.ok((Object) Map.of("mensaje", "Controlador desasignado de su zona"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Activar o desactivar usuario (R3) - Mejora 8 */
    @PatchMapping("/users/{id}/activo")
    public ResponseEntity<?> toggleActivo(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return usuarioRepo.findById(id).map(u -> {
            u.setActivo(body.getOrDefault("activo", !Boolean.TRUE.equals(u.getActivo())));
            usuarioRepo.save(u);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario " + (u.getActivo() ? "activado" : "desactivado"),
                "activo", u.getActivo()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Cambiar rol de usuario (R3) - Mejora 8 */
    @PatchMapping("/users/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return usuarioRepo.findById(id).map(u -> {
            try {
                u.setRol(Rol.valueOf(body.get("rol")));
                usuarioRepo.save(u);
                return ResponseEntity.ok((Object) Map.of(
                    "mensaje", "Rol actualizado a " + u.getRol().name(),
                    "username", u.getUsername(),
                    "rol", u.getRol().name()
                ));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body((Object) Map.of("error", "Rol inválido: " + body.get("rol")));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Listado de roles disponibles */
    @GetMapping("/roles")
    public List<String> getRoles() {
        return List.of("ADMIN", "CONTROLADOR");
    }

    /** Resumen de usuarios para administración (R8) */
    @GetMapping("/admin/resumen")
    public Map<String, Object> resumenAdmin() {
        long admins = usuarioRepo.findByRol(Rol.ADMIN).size();
        long controladores = usuarioRepo.findByRol(Rol.CONTROLADOR).size();
        long zonas = zonaRepo.count();
        long sinZona = usuarioRepo.findByRol(Rol.CONTROLADOR).stream()
            .filter(u -> u.getZona() == null).count();
        long activos = usuarioRepo.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getActivo())).count();
        return Map.of(
            "totalAdmins", admins,
            "totalControladores", controladores,
            "totalZonas", zonas,
            "controladoresSinZona", sinZona,
            "usuariosActivos", activos
        );
    }
}
