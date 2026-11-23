package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

@RestController
public class InfoController {
    private final int port;

    public InfoController(@Value("${server.port}") int port) {
        this.port = port;
    }

    @GetMapping("/port")
    public int getPort() {
        return port;
    }

    @GetMapping("/sum")
    public int getSum() {
        return IntStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
    }
}
