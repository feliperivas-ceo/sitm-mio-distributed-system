package com.sitmmio.dashboard.service;

import com.sitmmio.dashboard.model.Usuario;
import com.sitmmio.dashboard.model.Rol;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final Map<String, Usuario> usuarios = new HashMap<>();

    public AuthService() {
        usuarios.put("admin", new Usuario("admin", "1234", Rol.ADMIN));
        usuarios.put("operador", new Usuario("operador", "1234", Rol.OPERADOR));
        usuarios.put("cco", new Usuario("cco", "1234", Rol.CONTROLADOR));
    }

    public Usuario login(String username, String password) {

        Usuario user = usuarios.get(username);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }
}