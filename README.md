# Air Control test

This application emulates air control operations on an airport, allowing
planes to land if the request is done whenever the airport runways are not
packed.

A public RESTful API is provided to perform two basic operations:
- Allow / reject landing request from planes.
- Retrieve already landed planes.  

## How to use

### Perform a landing request
POST to http://localhost:8080/planes with a body like:

```json
{
    "flightNumber": "0000",
    "size": "BIG",
    "landingRequestTime": "
}
```

Size scan be BIG or SMALL (uppercase). Otherwise, an HTTP Bad Request will be returned. 

All flights have a landing time that can be configured by setting up the property `landing_time`, located at com/immccc/aircontrol/airport/airport.properties

A plane is accepted (Http 200 returned) to be landed or not (http 429) if there are no other planes landing at the same time and there is no more capacity for landing planes. **Assumption taken**: A plane is considered landed since the landing request, plus the defined landing time. Once landed, a plane is not considered to be blocking its part of the airport capacity.   

An specific landing request for a plane is idempotent, as it is comparing the rest of the planes in the airport at the landing request timeframe. Also, once a plane is landed, it is considered to be already landed even if the user introduces a new request with the same flight number, but different request time.  

It is possible to add a delay to the response, by adding the header `delay` with the delay time in seconds.

It is possible to configure by JMX the airport weather to be foggy. In that case, all flights will be rejected returning an HTTP Forbidden status. The MBean is `AirControl/JMX/WeatherConditions`

### Retrieval of landed planes
GET to http://localhost:8080/planes

## Some technical considerations
- Foremost, I tried to write as less code non related to business implementation as possible. Lombok and Spring Boot helps to not be lose the focus on verbose, repetitive and functionally meaningless code.
- Immutability has been a priority on designing the model and handling the objects, mainly because this is intended to be a multithreaded app. 
- Test coverage has been made in order to cover the most functional requisites and situations. However, I am not fully happy with the test
written for checking that delay on POST request works, as it digs into really internal implementation details of the tested class. However, I think it is the best solution.

