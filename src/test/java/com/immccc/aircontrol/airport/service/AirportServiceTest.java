package com.immccc.aircontrol.airport.service;

import static com.immccc.aircontrol.airport.model.LandingStatus.ACCEPTED;
import static com.immccc.aircontrol.airport.model.LandingStatus.REJECTED_BUSY_AIRPORT;
import static com.immccc.aircontrol.plane.model.PlaneSize.BIG;
import static com.immccc.aircontrol.plane.model.PlaneSize.SMALL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.immccc.aircontrol.airport.model.Airport;
import com.immccc.aircontrol.airport.model.LandingStatus;
import com.immccc.aircontrol.airport.model.Runway;
import com.immccc.aircontrol.airport.model.Weather;
import com.immccc.aircontrol.plane.model.Plane;
import com.immccc.aircontrol.plane.model.PlaneSize;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class AirportServiceTest {

    private static final int PLANE_TO_LAND_FLIGHT_NUMBER = 1000;
    private static final int NUMBER_OF_RUNWAYS = 2;
    private static final int REQUIRED_LANDING_TIME = 5;

    private static final String LANDING_REQUEST_TIME = "10:00:00";
    private static final String LANDING_REQUEST_TIME_NOT_YET_LANDED = "09:59:56";
    private static final String LANDING_REQUEST_TIME_ALREADY_LANDED = "09:00:00";

    private Airport airport;
    private Weather weather;

    private AirportService airportService;

    @Before
    public void setupTest() {

	airport = new Airport(NUMBER_OF_RUNWAYS, REQUIRED_LANDING_TIME);
	weather = new Weather();

	airportService = new AirportService(airport, weather);
    }

    @Test
    @Parameters(method = "parametersForLandBasedOnWeatherConditions")
    public void testLandBasedOnWeatherConditions(boolean foggy, List<List<Plane>> planesInAirport,
	    boolean expectedRejection) {

	LandingStatus landingStatus = whenPlaneLandsBasedOnStatus(foggy, SMALL, planesInAirport);

	assertThat("Land rejection is as expected", LandingStatus.REJECTED_WEATHER.equals(landingStatus),
		is(expectedRejection));

    }

    @Test
    @Parameters(method = "parametersForAcceptedLandingRequest")
    public void testLandAcceptedLandingRequest(PlaneSize planeSize, List<List<Plane>> planesInAirport) {

	LandingStatus landingStatus = whenPlaneLandsBasedOnStatus(false, planeSize, planesInAirport);
	assertThat("Landing accepted for plane", landingStatus, is(ACCEPTED));
    }

    @Test
    @Parameters(method = "parametersForRejectedLandingRequest")
    public void testLandRejectedLandingRequest(PlaneSize planeSize, List<List<Plane>> planesInAirport) {
    	LandingStatus landingStatus = whenPlaneLandsBasedOnStatus(false, planeSize, planesInAirport);
		assertThat("Landing rejected for plane", landingStatus, is(REJECTED_BUSY_AIRPORT));
    }

    @Test
    @Parameters(method = "parametersForAcceptedLandingRequest, parametersForRejectedLandingRequest")
    public void testLandIsIdempotent(PlaneSize planeSize, List<List<Plane>> planesInAirport) {
	LandingStatus landingStatusFirstCall = whenPlaneLandsBasedOnStatus(false, planeSize, planesInAirport);
	LandingStatus landingStatusSecondCall = whenPlaneLandsBasedOnStatus(false, planeSize, planesInAirport);

	assertThat("Land is an idempotent operation", landingStatusFirstCall.equals(landingStatusSecondCall), is(true));
    }

    @Test
    public void testGetPlanes() {
	List<List<Plane>> planesInAirport = createPlanesForBusyAirport();
	givenAirportHasPlanes(planesInAirport);

	Set<Plane> expectedPlanes = planesInAirport.stream().flatMap(planes -> planes.stream())
		.collect(Collectors.toSet());
	Set<Plane> actualPlanes = airportService.getPlanesInAirport();
	assertThat("Planes are retrieved correctly", actualPlanes, equalTo(expectedPlanes));
    }

    @SuppressWarnings("unused")
    private Object parametersForLandBasedOnWeatherConditions() {

	return new Object[] {
	    //@formatter:off

		       // foggy        | already landed planes           | expected rejection caused by weather
	    new Object[]{ true,          createNoPlanesForFreeAirport(),   true},
	    new Object[]{ true,          createPlanesForBusyAirport(),     true},
	    new Object[]{ false,         createNoPlanesForFreeAirport(),   false},
	    new Object[]{ false,         createPlanesForBusyAirport(),     false}
	    //@formatter:on
	};
    }

    @SuppressWarnings("unused")
    private Object parametersForAcceptedLandingRequest() {

	return new Object[] {
	    //@formatter:off

		       // actual plane size  | already landed planes
	    new Object[]{ SMALL,               createNoPlanesForFreeAirport() },
	    new Object[]{ SMALL,               createAlreadyLandedPlanesForFreeAirport() },
	    new Object[]{ SMALL,               createPlanesForOnlyOneSmallPlaneFreeAirport() },
	    new Object[]{ BIG,                 createNoPlanesForFreeAirport() },
	    new Object[]{ BIG,                 createAlreadyLandedPlanesForFreeAirport() },
	    new Object[]{ BIG,                 createPlanesForOnlyOneBigPlaneFreeAirport() }

	    //@formatter:on
	};

    }

    @SuppressWarnings("unused")
    private Object parametersForRejectedLandingRequest() {

	return new Object[] {
	    //@formatter:off

		       // actual plane size  | already landed planes
	    new Object[]{ SMALL,               createPlanesForBusyAirport() },
	    new Object[]{ BIG,                 createPlanesForBusyAirport() },
	    new Object[]{ BIG,                 createPlanesForOnlyOneSmallPlaneFreeAirport() }

	    //@formatter:on
	};

    }

    private void givenAirportHasPlanes(List<List<Plane>> planesInAirport) {

	Iterator<List<Plane>> planesInAirportIterator = planesInAirport.iterator();
	for (Runway runway : airport.getRunways()) {
	    runway.getPlanes().addAll(planesInAirportIterator.next());
	}

    }

    private Plane givenPlaneToLand(PlaneSize size) {
	return Plane.builder().flightNumber(PLANE_TO_LAND_FLIGHT_NUMBER).landingRequestTime(LANDING_REQUEST_TIME)
		.size(size).build();
    }

    private LandingStatus whenPlaneLandsBasedOnStatus(boolean foggy, PlaneSize planeSize,
	    List<List<Plane>> planesInAirport) {

	givenAirportHasPlanes(planesInAirport);
	Plane plane = givenPlaneToLand(planeSize);

	weather.setFoggy(foggy);

	LandingStatus landingStatus = airportService.landPlane(plane);
	return landingStatus;
    }

    private List<List<Plane>> createNoPlanesForFreeAirport() {
	return ImmutableList.of(ImmutableList.of(), ImmutableList.of());
    }

    private List<List<Plane>> createPlanesForBusyAirport() {

	List<Plane> planesInRunway1 = ImmutableList.of(Plane.builder().flightNumber(1).size(BIG)
		.landingRequestTime(LANDING_REQUEST_TIME_NOT_YET_LANDED).build());

	List<Plane> planesInRunway2 = ImmutableList.of(Plane.builder().flightNumber(2).size(BIG)
		.landingRequestTime(LANDING_REQUEST_TIME_NOT_YET_LANDED).build());

	return ImmutableList.of(planesInRunway1, planesInRunway2);
    }

    private List<List<Plane>> createAlreadyLandedPlanesForFreeAirport() {

	List<Plane> planesInRunway1 = ImmutableList.of(Plane.builder().flightNumber(1).size(BIG)
		.landingRequestTime(LANDING_REQUEST_TIME_ALREADY_LANDED).build());

	List<Plane> planesInRunway2 = ImmutableList.of(Plane.builder().flightNumber(2).size(BIG)
		.landingRequestTime(LANDING_REQUEST_TIME_ALREADY_LANDED).build());

	return ImmutableList.of(planesInRunway1, planesInRunway2);
    }

    private List<List<Plane>> createPlanesForOnlyOneSmallPlaneFreeAirport() {
	List<Plane> planesInRunway1 = ImmutableList.of(Plane.builder().flightNumber(1).size(BIG)
		.landingRequestTime(LANDING_REQUEST_TIME_NOT_YET_LANDED).build());

	List<Plane> planesInRunway2 = ImmutableList.of(Plane.builder().flightNumber(2).size(SMALL)
		.landingRequestTime(LANDING_REQUEST_TIME_NOT_YET_LANDED).build());

	return ImmutableList.of(planesInRunway1, planesInRunway2);
    }

    private List<List<Plane>> createPlanesForOnlyOneBigPlaneFreeAirport() {
	List<Plane> planesInRunway1 = ImmutableList.of(Plane.builder().flightNumber(1).size(BIG)
		.landingRequestTime(LANDING_REQUEST_TIME_NOT_YET_LANDED).build());

	List<Plane> planesInRunway2 = ImmutableList.of();

	return ImmutableList.of(planesInRunway1, planesInRunway2);
    }

}
