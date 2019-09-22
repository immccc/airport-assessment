package com.immccc.aircontrol.plane.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PlaneController.class })
public class PlaneControllerDelayTest {

    private static final int DELAY_TIME = 5;

    @Test
    public void testLandIsDelayed() throws IllegalArgumentException, IllegalAccessException {

	ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);

	PlaneController planeController = new PlaneController(null);
	Whitebox.setInternalState(planeController, ScheduledThreadPoolExecutor.class, executor);

	planeController.land(null, DELAY_TIME);

	Mockito.verify(executor).schedule(any(Runnable.class), eq(Long.valueOf(DELAY_TIME)), eq(TimeUnit.SECONDS));
    }
}
