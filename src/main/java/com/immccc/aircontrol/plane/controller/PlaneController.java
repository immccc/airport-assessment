package com.immccc.aircontrol.plane.controller;

import com.immccc.aircontrol.airport.model.LandingStatus;
import com.immccc.aircontrol.airport.service.AirportService;
import com.immccc.aircontrol.plane.model.Plane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/planes")
@Slf4j
public class PlaneController {

    private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);

    private final AirportService airportService;

    PlaneController(AirportService airportService) {
        this.airportService = airportService;
    }

    @PostMapping
    public DeferredResult<ResponseEntity<Void>> land(@RequestBody Plane plane,
                                                     @RequestHeader(name = "delay", defaultValue = "0") int delay) {

        DeferredResult<ResponseEntity<Void>> deferredResponse = new DeferredResult<>();

        scheduledExecutor.schedule(() -> {
            LandingStatus landingStatus = airportService.landPlane(plane);
            deferredResponse.setResult(getResponseEntityFromLandingStatus(landingStatus));
        }, delay, TimeUnit.SECONDS);

        return deferredResponse;
    }

    @GetMapping
    public Set<Plane> getAcceptedPlanes() {
        return airportService.getPlanesInAirport();
    }

    private ResponseEntity<Void> getResponseEntityFromLandingStatus(LandingStatus landingStatus) {
        switch (landingStatus) {
            case REJECTED_WEATHER:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case REJECTED_BUSY_AIRPORT:
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            case ACCEPTED:
                return ResponseEntity.ok().build();
            default:
                log.error("Unrecognized landing status {}, returning BAD_REQUEST", landingStatus);
                return ResponseEntity.badRequest().build();
        }

    }
}
