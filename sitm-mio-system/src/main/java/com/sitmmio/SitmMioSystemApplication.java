package com.sitmmio;

import com.sitmmio.bus.simulator.MultiBusSimulator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SitmMioSystemApplication {

    public static void main(String[] args) {

        SpringApplication.run(SitmMioSystemApplication.class, args);

        MultiBusSimulator simulator = new MultiBusSimulator(5);
        simulator.iniciarSimulacion();
    }
}
