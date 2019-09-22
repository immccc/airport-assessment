package com.immccc.aircontrol.airport.model;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "AirControl:type=JMX,name=WeatherConditions")
public class Weather {
    private boolean foggy;

    @ManagedAttribute
    public boolean isFoggy() {
	return foggy;
    }

    @ManagedAttribute
    public void setFoggy(boolean foggy) {
	this.foggy = foggy;
    }

}
