package com.immccc.aircontrol.airport.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.immccc.aircontrol.plane.model.Plane;
import lombok.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Value
public class Airport {

    private List<Runway> runways;

    private int landingRequiredTime;

    public Airport(long numberOfRunway, int landingRequiredTime) {

        this.runways = buildRunways(numberOfRunway);
        this.landingRequiredTime = landingRequiredTime;
    }

    private List<Runway> buildRunways(long numberOfLanes) {

        Builder<Runway> runwaysBuilder = ImmutableList.builder();
        LongStream.rangeClosed(1, numberOfLanes).forEach(laneNumber -> runwaysBuilder.add(new Runway()));

        return runwaysBuilder.build();
    }

    public Set<Plane> getLandedPlanes() {
        return getRunways().stream().flatMap(runway -> runway.getPlanes().stream()).collect(Collectors.toSet());
    }

}
