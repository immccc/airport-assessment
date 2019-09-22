package com.immccc.aircontrol.airport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.PropertySource;

import com.immccc.aircontrol.airport.model.Airport;
import com.immccc.aircontrol.airport.model.Weather;

@Configuration
@EnableMBeanExport
@PropertySource("classpath:/com/immccc/aircontrol/airport/airport.properties")
public class AirportConfiguration {

    private static int DEFAULT_NUMBER_OF_LANES = 2;

    @Value("${landing_time}")
    private int landingTime;

    @Bean
    public Weather createWeatherBean() {
	return new Weather();
    }

    @Bean
    public Airport createAirportBean() {
	return new Airport(DEFAULT_NUMBER_OF_LANES, landingTime);
    }
}