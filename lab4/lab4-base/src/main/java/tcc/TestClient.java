package tcc;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import tcc.flight.FlightReservationDoc;
import tcc.hotel.HotelReservationDoc;

/**
 * Simple non-transactional client. Can be used to populate the booking services
 * with some requests.
 */
public class TestClient {
	public static void main(String[] args) {
		for(int i = 1; i <= 11; i++){
			try {
				Client client = ClientBuilder.newClient();
				WebTarget target = client.target(TestServer.BASE_URI);

				GregorianCalendar tomorrow = new GregorianCalendar();
				tomorrow.setTime(new Date());
				tomorrow.add(GregorianCalendar.DAY_OF_YEAR, 1);

				// book flight

				WebTarget webTargetFlight = target.path("flight");

				FlightReservationDoc docFlight = new FlightReservationDoc();
				docFlight.setName("Christian");
				docFlight.setFrom("Karlsruhe");
				docFlight.setTo("Berlin");
				docFlight.setAirline("airberlin");
				docFlight.setDate(tomorrow.getTimeInMillis());

				Response responseFlight = webTargetFlight.request().accept(MediaType.APPLICATION_XML)
						.post(Entity.xml(docFlight));

				if (responseFlight.getStatus() != 200) {
					System.out.println("Failed : HTTP error code : " + responseFlight.getStatus());
				}

				FlightReservationDoc outputFlight = responseFlight.readEntity(FlightReservationDoc.class);
				System.out.println("Output from Server: " + outputFlight);

				// book hotel

				WebTarget webTargetHotel = target.path("hotel");

				HotelReservationDoc docHotel = new HotelReservationDoc();
				docHotel.setName("Christian");
				docHotel.setHotel("Interconti");
				docHotel.setDate(tomorrow.getTimeInMillis());

				Response responseHotel = webTargetHotel.request().accept(MediaType.APPLICATION_XML)
						.post(Entity.xml(docHotel));

				if (responseHotel.getStatus() != 200) {
					System.out.println("Failed : HTTP error code : " + responseHotel.getStatus());
				}

				HotelReservationDoc outputHotel = responseHotel.readEntity(HotelReservationDoc.class);
				System.out.println("Output from Server: " + outputHotel);

				//---------------------
				// lab5

				if (outputHotel != null && outputFlight != null) {

					if (responseHotel.getStatus() != 200 || responseFlight.getStatus() != 200) {
						System.out.println("Failed : HTTP error code : " + responseFlight.getStatus() + ", " + responseHotel.getStatus());

						// Rollback
						if(outputHotel.getUrl() != null)
						{
							String[] url_hotel = outputHotel.getUrl().split("/");
							String hotelId = url_hotel[url_hotel.length - 1];
							WebTarget webTargetHotelWithId = target.path("hotel/" + hotelId);
							webTargetHotelWithId.request().delete();
						}
						if(outputFlight.getUrl() != null)
						{
							String[] url_flight = outputFlight.getUrl().split("/");
							String flightId = url_flight[url_flight.length - 1];
							WebTarget webTargetFlightWithId = target.path("flight/" + flightId);
							webTargetFlightWithId.request().delete();
						}


					} else {
						// 3. Confirm both flight and hotel reservation
						String[] url_hotel = outputHotel.getUrl().split("/");
						String hotelId = url_hotel[url_hotel.length - 1];

						String[] url_flight = outputFlight.getUrl().split("/");
						String flightId = url_flight[url_flight.length - 1];

						WebTarget webTargetFlightWithId = target.path("flight/" + flightId);
						WebTarget webTargetHotelWithId = target.path("hotel/" + hotelId);

						long flightExpiration = outputFlight.getExpires();
						long hotelExpiration = outputHotel.getExpires();
						long currentTime = System.currentTimeMillis();
						long shorterExpiration = flightExpiration <= hotelExpiration ? flightExpiration : hotelExpiration;
						long longerExpiration = flightExpiration > hotelExpiration ? flightExpiration : hotelExpiration;

						boolean isFlightShorter = flightExpiration <= hotelExpiration;
						boolean isShorterSuccessful = false;
						boolean isLongerSuccessful = false;

						Response responseFlightConfirm;
						Response responseHotelConfirm;
						// responseFlightConfirm.getStatus() != 200 || responseHotelConfirm.getStatus() != 200
						do {
							currentTime = System.currentTimeMillis();
							if (!isShorterSuccessful) { // shorter first
								if (isFlightShorter) {
									responseFlightConfirm = webTargetFlightWithId.request().put(Entity.xml(""));
									if (responseFlightConfirm.getStatus() == 200) {
										isShorterSuccessful = true;
									}
								} else {
									responseHotelConfirm = webTargetHotelWithId.request().put(Entity.xml(""));
									if (responseHotelConfirm.getStatus() == 200) {
										isShorterSuccessful = true;
									}
								}
							} else {
								// first (shorter) was successful -> try to confim second until it also succed
								if (!isFlightShorter) { // now the longer one
									responseFlightConfirm = webTargetFlightWithId.request().put(Entity.xml(""));
									if (responseFlightConfirm.getStatus() == 200) {
										isLongerSuccessful = true;
									}
								} else {
									responseHotelConfirm = webTargetHotelWithId.request().put(Entity.xml(""));
									if (responseHotelConfirm.getStatus() == 200) {
										isLongerSuccessful = true;
									}
								}
							}
						} while (currentTime < shorterExpiration && !isLongerSuccessful);


						if (isShorterSuccessful && !isLongerSuccessful) {
							// if shorter timeout is over and only the first confirm succed
							// -> try to connfirm second one until its timeout
							do {
								currentTime = System.currentTimeMillis();

								if (!isFlightShorter) { // now the longer one
									responseFlightConfirm = webTargetFlightWithId.request().put(Entity.xml(""));
									if (responseFlightConfirm.getStatus() == 200) {
										isLongerSuccessful = true;
									}
								} else {
									responseHotelConfirm = webTargetHotelWithId.request().put(Entity.xml(""));
									if (responseHotelConfirm.getStatus() == 200) {
										isLongerSuccessful = true;
									}
								}

							} while (currentTime < longerExpiration && !isLongerSuccessful);
							if(!isLongerSuccessful && isShorterSuccessful)
							{
								throw new RuntimeException("Inkonsistenter Zustand!");
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// CHECK: Number of Flight Booking = Number of Hotel Booking
		try{
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(TestServer.BASE_URI);

			WebTarget webTargetFlight = target.path("flight");
			Response allFlightsResponse = webTargetFlight.request().get();
			int numberOfAllFlights = allFlightsResponse.readEntity(FlightReservationDoc[].class).length;

			WebTarget webTargetHotel = target.path("hotel");
			Response allHotelsResponse = webTargetHotel.request().get();
			int numberOfAllHotels = allHotelsResponse.readEntity(HotelReservationDoc[].class).length;

			boolean result = numberOfAllHotels == numberOfAllFlights;
			System.out.println("##### CHECKING #####");
			System.out.println("##### Is the number of Flight and Hotel Booking same ? : " + result );
			System.out.println("##### The number of Flight : " + numberOfAllFlights );
			System.out.println("##### The number of Hotel : " + numberOfAllHotels  );
		} catch (Exception e){
			System.out.println(e);
		}
	}
}
