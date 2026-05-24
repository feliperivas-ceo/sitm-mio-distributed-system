package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.model.Usuario;
import com.sitmmio.dashboard.service.AuthService;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    private final AuthService authService;

    private Usuario usuarioActual;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public boolean login(String username, String password) {

        Usuario user = authService.login(username, password);

        if (user != null) {
            this.usuarioActual = user;
            System.out.println("Login exitoso: " + user.getUsername());
            return true;
        }

        System.out.println("Credenciales inválidas");
        return false;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
