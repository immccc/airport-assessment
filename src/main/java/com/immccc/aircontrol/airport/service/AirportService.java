package com.immccc.aircontrol.airport.service;

import com.immccc.aircontrol.airport.model.Airport;
import com.immccc.aircontrol.airport.model.LandingStatus;
import com.immccc.aircontrol.airport.model.Runway;
import com.immccc.aircontrol.airport.model.Weather;
import com.immccc.aircontrol.plane.model.Plane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirportService {

	private static final int MAX_ALLOWED_WIDTH_FOR_SMALL_PLANES = 2;
	private static final int MAX_ALLOWED_WIDTH_FOR_BIG_PLANES = 1;

	private final Airport airport;
	private final Weather weather;

	public Set<Plane> getPlanesInAirport() {
		return airport.getLandedPlanes();
	}

	public LandingStatus landPlane(Plane planeToLand) {

		if (weather.isFoggy()) {
			log.info("Fog present, flight {} is rejected", planeToLand.getFlightNumber());
			return LandingStatus.REJECTED_WEATHER;
		}

		if (airport.getLandedPlanes().contains(planeToLand)) {
			log.info("Flight {} is already landed ", planeToLand.getFlightNumber());
			return LandingStatus.ACCEPTED;
		}

		Optional<Runway> runway = getAvailableRunway(planeToLand);
		if (runway.isPresent()) {
			log.info("Flight {} accepted", planeToLand.getFlightNumber());
			runway.get().getPlanes().add(planeToLand);
			return LandingStatus.ACCEPTED;
		} else {
			log.info("Flight {} rejected", planeToLand.getFlightNumber());
			return LandingStatus.REJECTED_BUSY_AIRPORT;
		}

	}

	private Optional<Runway> getAvailableRunway(Plane planeToLand) {
		return airport.getRunways().stream().filter(runway -> isRunwayAvailable(runway, planeToLand)).findFirst();
	}

	private boolean isRunwayAvailable(Runway runway, Plane planeToLand) {
		LocalTime landingFinishedTime = planeToLand.getParsedLandingRequestTime()
				.plusSeconds(airport.getLandingRequiredTime());

		Predicate<? super Plane> retrievePlanesLandingBetweenTimeframe = plane -> isPlaneLandingInTimeframe(plane,
				planeToLand.getParsedLandingRequestTime(), landingFinishedTime);

		long totalRunwaySpaceUsed = runway.getPlanes().stream().filter(retrievePlanesLandingBetweenTimeframe)
				.mapToInt(plane -> plane.getSize().getWidth()).sum();

		switch (planeToLand.getSize()) {
			case BIG:
				return totalRunwaySpaceUsed < MAX_ALLOWED_WIDTH_FOR_BIG_PLANES;
			case SMALL:
				return totalRunwaySpaceUsed < MAX_ALLOWED_WIDTH_FOR_SMALL_PLANES;
			default:
				log.warn("Invalid plane size {}. This should not happen", planeToLand.getSize());
				return false;
		}

	}

	private boolean isPlaneLandingInTimeframe(Plane plane, LocalTime landingRequestTime,
											  LocalTime landingFinishedTime) {

		LocalTime planeLandingStartTime = plane.getParsedLandingRequestTime();
		LocalTime planeLandingFinishedTime = planeLandingStartTime.plusSeconds(airport.getLandingRequiredTime());

		boolean planeStartLandingBetweenTimeframe = planeLandingStartTime.isAfter(landingRequestTime)
				&& planeLandingStartTime.isBefore(landingFinishedTime);

		boolean planeFinishLandingBetweenTimeframe = planeLandingFinishedTime.isAfter(landingRequestTime)
				&& planeLandingFinishedTime.isBefore(landingFinishedTime);

		return planeStartLandingBetweenTimeframe || planeFinishLandingBetweenTimeframe
				|| planeLandingStartTime.equals(landingRequestTime);
	}
}
