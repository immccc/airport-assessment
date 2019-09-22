package com.immccc.aircontrol.plane.controller;

import static com.immccc.aircontrol.airport.model.LandingStatus.ACCEPTED;
import static com.immccc.aircontrol.airport.model.LandingStatus.REJECTED_BUSY_AIRPORT;
import static com.immccc.aircontrol.airport.model.LandingStatus.REJECTED_WEATHER;
import static com.immccc.aircontrol.plane.model.PlaneSize.BIG;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.immccc.aircontrol.airport.model.LandingStatus;
import com.immccc.aircontrol.airport.service.AirportService;
import com.immccc.aircontrol.plane.model.Plane;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class PlaneControllerTest {

    private static final int BIG_LANDING_DELAY = 5;

    private static final Gson JSON_MAPPER = new Gson();

    private static final int PLANE_TO_LAND_FLIGHT_NUMBER = 1;
    private static final String LANDING_REQUEST_TIME = "00:00:00";

    @Mock
    private AirportService airportService;

    private PlaneController planeController;

    private MockMvc mockMvc;

    @Before
    public void setupTest() {
	MockitoAnnotations.initMocks(this);
	planeController = new PlaneController(airportService);

	mockMvc = MockMvcBuilders.standaloneSetup(planeController).build();
    }

    @Test
    @Parameters(method = "parametersForLandReturnsExpectedErrorCode")
    public void testLandReturnsExpectedErrorCode(LandingStatus returnedLandingStatus, HttpStatus expectedHttpStatus)
	    throws Exception {
	Plane plane = givenPlane();
	String planeAsJson = JSON_MAPPER.toJson(plane);

	when(airportService.landPlane(any())).thenReturn(returnedLandingStatus);

	ResponseEntity<Void> expectedResponseEntity = ResponseEntity.status(expectedHttpStatus).build();
	mockMvc.perform(post("/planes").contentType(MediaType.APPLICATION_JSON).content(planeAsJson))
		.andExpect(request().asyncResult(expectedResponseEntity));

    }

    @Test
    public void testLandIsDelayed() throws Exception {
	Plane plane = givenPlane();
	JSON_MAPPER.toJson(plane);

	when(airportService.landPlane(any())).thenReturn(ACCEPTED);

	DeferredResult<ResponseEntity<Void>> deferredHttpResponse = planeController.land(plane, BIG_LANDING_DELAY);

	Assert.assertFalse(deferredHttpResponse.hasResult());
    }

    @Test
    public void testGetPlanesInAirport() throws Exception {
	Set<Plane> planesToBeReturned = ImmutableSet.of(givenPlane());
	when(airportService.getPlanesInAirport()).thenReturn(planesToBeReturned);

	mockMvc.perform(get("/planes"))
		.andExpect(MockMvcResultMatchers.content().json(JSON_MAPPER.toJson(planesToBeReturned)));
    }

    @SuppressWarnings("unused")
    private Object parametersForLandReturnsExpectedErrorCode() {
	return new Object[] {
		//@formatter:off

			       // returned landing status  | expected Http status
		    new Object[]{ ACCEPTED,                  OK },
		    new Object[]{ REJECTED_BUSY_AIRPORT,          TOO_MANY_REQUESTS },
		    new Object[]{ REJECTED_WEATHER,          FORBIDDEN }

		//@formatter:on
	};
    }

    private Plane givenPlane() {
	return Plane.builder().flightNumber(PLANE_TO_LAND_FLIGHT_NUMBER).landingRequestTime(LANDING_REQUEST_TIME)
		.size(BIG).build();
    }

}
