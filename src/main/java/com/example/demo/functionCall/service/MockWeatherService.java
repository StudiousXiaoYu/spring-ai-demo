package com.example.demo.functionCall.service;

import java.util.function.Function;

import org.springframework.context.annotation.Description;

import com.example.demo.functionCall.service.MockWeatherService.Request;
import com.example.demo.functionCall.service.MockWeatherService.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockWeatherService implements Function<Request, Response> {

    public enum Unit { C, F }
    public record Request(String location, Unit unit) {}
    public record Response(double temp, Unit unit) {}

    public Response apply(Request request) {
        log.info("Request: {}", request.location);
        return new Response(30.0, Unit.C);
    }
}
