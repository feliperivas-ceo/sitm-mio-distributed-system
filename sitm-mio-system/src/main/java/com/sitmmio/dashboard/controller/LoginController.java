package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.service.AuthService;
import com.sitmmio.dashboard.view.LoginView;
import com.sitmmio.common.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired private AuthService authService;
    @Autowired private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds, HttpServletRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(creds.get("username"), creds.get("password"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

            Usuario usuario = authService.findByUsername(creds.get("username"));
            LoginView view = new LoginView();
            view.setUsername(usuario.getUsername());
            view.setNombre(usuario.getNombre());
            view.setRol(usuario.getRol().name());
            if (usuario.getZona() != null) {
                view.setZonaId(usuario.getZona().getId());
                view.setZonaNombre(usuario.getZona().getNombre());
            }
            view.setMensaje("Bienvenido, " + usuario.getNombre());
            return ResponseEntity.ok(view);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        try {
            Usuario usuario = authService.findByUsername(auth.getName());
            LoginView view = new LoginView();
            view.setUsername(usuario.getUsername());
            view.setNombre(usuario.getNombre());
            view.setRol(usuario.getRol().name());
            if (usuario.getZona() != null) {
                view.setZonaId(usuario.getZona().getId());
                view.setZonaNombre(usuario.getZona().getNombre());
            }
            return ResponseEntity.ok(view);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
    }
}
