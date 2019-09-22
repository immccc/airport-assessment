package com.immccc.aircontrol.plane.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(of = "flightNumber")
public class Plane {
    private int flightNumber;
    private PlaneSize size;
    private String landingRequestTime;

    @JsonIgnore
    public LocalTime getParsedLandingRequestTime() {
	return LocalTime.parse(landingRequestTime);
    }

}
