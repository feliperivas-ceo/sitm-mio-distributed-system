package com.sitmmio.dashboard.config;

import com.sitmmio.common.model.*;
import com.sitmmio.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Random;

@Configuration
public class DatabaseConfig {

    @Bean
    public CommandLineRunner loadData(
            RutaRepository rutaRepo,
            BusRepository busRepo,
            EstacionRepository estacionRepo,
            EventoRepository eventoRepo,
            ZonaRepository zonaRepo,
            UsuarioRepository usuarioRepo,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Random rand = new Random(42);

            // --- Rutas MIO Cali ---
            String[][] rutaData = {
                {"A10A", "Aguablanca - Unidad Deportiva", "#E74C3C"},
                {"A10B", "Aguablanca - Centro", "#E67E22"},
                {"B10",  "Norte - Sur Troncal",          "#2ECC71"},
                {"C10",  "Oeste - Centro Expreso",        "#3498DB"},
                {"P11",  "Alameda - Mariano Ramos",       "#9B59B6"}
            };
            for (String[] r : rutaData) {
                Ruta ruta = new Ruta();
                ruta.setId(r[0]); ruta.setNombre(r[1]); ruta.setColor(r[2]);
                rutaRepo.save(ruta);
            }

            // --- Zonas ---
            String[][] zonaData = {
                {"Zona Norte", "Cobertura sector norte de Cali"},
                {"Zona Sur",   "Cobertura sector sur de Cali"},
                {"Zona Centro","Cobertura sector centro de Cali"}
            };
            for (String[] z : zonaData) {
                Zona zona = new Zona();
                zona.setNombre(z[0]); zona.setDescripcion(z[1]);
                zonaRepo.save(zona);
            }

            // --- Usuarios ---
            Zona zonaNorte = zonaRepo.findAll().get(0);
            Zona zonaSur   = zonaRepo.findAll().get(1);

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador MIO");
            admin.setEmail("admin@mio.gov.co");
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepo.save(admin);

            Usuario ctrl1 = new Usuario();
            ctrl1.setUsername("controlador1");
            ctrl1.setPassword(passwordEncoder.encode("ctrl123"));
            ctrl1.setNombre("Carlos Pérez");
            ctrl1.setEmail("cperez@mio.gov.co");
            ctrl1.setRol(Rol.CONTROLADOR);
            ctrl1.setZona(zonaNorte);
            ctrl1.setActivo(true);
            usuarioRepo.save(ctrl1);

            Usuario ctrl2 = new Usuario();
            ctrl2.setUsername("controlador2");
            ctrl2.setPassword(passwordEncoder.encode("ctrl123"));
            ctrl2.setNombre("Ana García");
            ctrl2.setEmail("agarcia@mio.gov.co");
            ctrl2.setRol(Rol.CONTROLADOR);
            ctrl2.setZona(zonaSur);
            ctrl2.setActivo(true);
            usuarioRepo.save(ctrl2);

            // --- Estaciones ---
            Object[][] estData = {
                {"Terminal Norte",         rutaRepo.findById("B10").get(),  3.4900, -76.5200, "ESTACION_MAYOR"},
                {"Unidad Deportiva",       rutaRepo.findById("A10A").get(), 3.4200, -76.5100, "ESTACION_MAYOR"},
                {"Centro Cali - Cali Viejo", rutaRepo.findById("C10").get(), 3.4516, -76.5320, "ESTACION_MAYOR"},
                {"Alameda",                rutaRepo.findById("P11").get(),  3.4450, -76.5250, "PARADA"},
                {"Mariano Ramos",          rutaRepo.findById("P11").get(),  3.3900, -76.5350, "PARADA"},
                {"Versalles",              rutaRepo.findById("B10").get(),  3.4700, -76.5300, "PARADA"},
                {"Aguablanca",             rutaRepo.findById("A10A").get(), 3.4100, -76.4900, "PARADA"},
                {"Calima",                 rutaRepo.findById("C10").get(),  3.4550, -76.5400, "PARADA"}
            };
            for (Object[] e : estData) {
                Estacion est = new Estacion();
                est.setNombre((String) e[0]);
                est.setRuta((Ruta) e[1]);
                est.setLatitud((Double) e[2]);
                est.setLongitud((Double) e[3]);
                est.setTipo((String) e[4]);
                estacionRepo.save(est);
            }

            // --- Buses y Eventos ---
            String[] rutaIds = {"A10A", "A10B", "B10", "C10", "P11"};
            double[][] rutaCentros = {
                {3.4150, -76.5000}, {3.4200, -76.5050},
                {3.4600, -76.5250}, {3.4480, -76.5360},
                {3.4200, -76.5300}
            };
            String[] estados = {"ACTIVO", "EN_RUTA", "EN_PARADA"};
            TipoEvento[] tipos = TipoEvento.values();
            Prioridad[] prioridades = Prioridad.values();

            for (int i = 1; i <= 20; i++) {
                int ri = (i - 1) % rutaIds.length;
                Ruta ruta = rutaRepo.findById(rutaIds[ri]).get();

                Bus bus = new Bus();
                bus.setId(String.format("MIO-%03d", i));
                bus.setNumeroPlaca(String.format("MIO%04d", i));
                bus.setRuta(ruta);
                bus.setLatitud(rutaCentros[ri][0] + (rand.nextDouble() - 0.5) * 0.04);
                bus.setLongitud(rutaCentros[ri][1] + (rand.nextDouble() - 0.5) * 0.04);
                bus.setUltimoTimestamp(LocalDateTime.now().minusMinutes(rand.nextInt(30)));
                bus.setUltimoEvento(tipos[rand.nextInt(tipos.length)]);
                bus.setPrioridad(prioridades[rand.nextInt(prioridades.length)]);
                bus.setEstado(estados[rand.nextInt(estados.length)]);
                bus.setVelocidad(20 + rand.nextDouble() * 40);
                busRepo.save(bus);

                // Generar eventos históricos por bus
                for (int j = 0; j < 5; j++) {
                    Evento ev = new Evento();
                    ev.setBus(bus);
                    ev.setRuta(ruta);
                    TipoEvento tipo = tipos[rand.nextInt(tipos.length)];
                    ev.setTipoEvento(tipo);
                    ev.setPrioridad(tipo == TipoEvento.ACCIDENTE ? Prioridad.ALTA :
                                    tipo == TipoEvento.CONGESTION ? Prioridad.MEDIA : Prioridad.BAJA);
                    ev.setLatitud(bus.getLatitud() + (rand.nextDouble() - 0.5) * 0.01);
                    ev.setLongitud(bus.getLongitud() + (rand.nextDouble() - 0.5) * 0.01);
                    ev.setTimestamp(LocalDateTime.now().minusHours(rand.nextInt(72)));
                    ev.setEstadoProcesado(rand.nextBoolean());
                    ev.setDescripcion("Evento " + tipo.name() + " registrado");
                    eventoRepo.save(ev);
                }
            }
        };
    }
}
