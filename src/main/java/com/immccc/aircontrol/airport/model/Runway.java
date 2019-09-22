package com.immccc.aircontrol.airport.model;

import com.immccc.aircontrol.plane.model.Plane;
import lombok.Value;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@Value
public class Runway {

    private static final Comparator<Plane> LANDING_TIME_COMPARATOR = (plane1, plane2) -> {
        int compareResult = plane2.getParsedLandingRequestTime().compareTo(plane1.getParsedLandingRequestTime());
        return compareResult == 0
                ? Comparator.<Integer>naturalOrder().compare(plane1.getFlightNumber(), plane2.getFlightNumber())
                : compareResult;
    };

    private Set<Plane> planes;

    public Runway() {
        this.planes = new TreeSet<>(LANDING_TIME_COMPARATOR);
    }
}
