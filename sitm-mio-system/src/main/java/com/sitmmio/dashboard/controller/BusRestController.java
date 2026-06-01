package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.dto.BusDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BusRestController {

    @GetMapping("/api/buses")
    public List<BusDTO> getBuses() {

        return List.of(

            new BusDTO(
                "BUS-01",
                3.4516,
                -76.5320,
                "A13A"
            ),

            new BusDTO(
                "BUS-02",
                3.4450,
                -76.5350,
                "T31"
            )

        );
    }
}