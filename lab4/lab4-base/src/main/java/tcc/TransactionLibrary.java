package tcc;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import tcc.flight.FlightReservationDoc;
import tcc.hotel.HotelReservationDoc;


/**
 * Transaction Library
 */

 @Path("/transactionLib")
public class TransactionLibrary {

    /**
     * Send confirmation of both flight and hotel booking.
     * Can only confirm, if both flight and hotel reservation exist.
     */

    @PUT
    @Path("confirm/{flightId}/{hotelId}")
	@Produces(MediaType.TEXT_PLAIN)
    public Response confirmFlightAndHotelBooking(@PathParam("flightId") String flightId, @PathParam("hotelId") String hotelId) {

        try {
        
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(TestServer.BASE_URI);

            // 1. Is there any flight reservation?

            WebTarget webTargetFlight = target.path("flight/" + flightId);
            Response responseFlightReservation = webTargetFlight.request().get();

            // 2. Is there any hotel reservation?
            WebTarget webTargetHotel = target.path("hotel/" + hotelId);
            Response responseHotelReservation = webTargetHotel.request().get();

            if (responseFlightReservation.getStatus() != 200 || responseHotelReservation.getStatus() != 200) {
                System.out.println("Failed : HTTP error code : " + responseFlightReservation.getStatus());
                // try again

			} else {
                // 3. Confirm both flight and hotel reservation
                long flightExpiration =((FlightReservationDoc)responseFlightReservation.getEntity()).getExpires();
                long hotelExpiration =((HotelReservationDoc)responseHotelReservation.getEntity()).getExpires();                
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
                    if(!isShorterSuccessful){ // shorter first
                        if (isFlightShorter){
                            responseFlightConfirm = webTargetFlight.request().put(null);
                            if(responseFlightConfirm.getStatus() == 200)
                            {
                                isShorterSuccessful = true;
                            }
                        } else {
                            responseHotelConfirm = webTargetHotel.request().put(null);
                            if(responseHotelConfirm.getStatus() == 200)
                            {
                                isShorterSuccessful = true;
                            }
                        }
                    }
                    else{
                        // first (shorter) was successful -> try to confim second until it also succed
                        if (!isFlightShorter){ // now the longer one
                            responseFlightConfirm = webTargetFlight.request().put(null);
                            if(responseFlightConfirm.getStatus() == 200)
                            {
                                isLongerSuccessful = true;
                            }
                        } else {
                            responseHotelConfirm = webTargetHotel.request().put(null);
                            if(responseHotelConfirm.getStatus() == 200)
                            {
                                isLongerSuccessful = true;
                            }
                        }
                    }
                }while (currentTime < shorterExpiration && !isLongerSuccessful);


                if(isShorterSuccessful && !isLongerSuccessful)
                {
                    // if shorter timeout is over and only the first confirm succed
                    // -> try to connfirm second one until its timeout
                    do{
                        currentTime = System.currentTimeMillis();

                        if (!isFlightShorter){ // now the longer one
                            responseFlightConfirm = webTargetFlight.request().put(null);
                            if(responseFlightConfirm.getStatus() == 200)
                            {
                                isLongerSuccessful = true;
                            }
                        } else {
                            responseHotelConfirm = webTargetHotel.request().put(null);
                            if(responseHotelConfirm.getStatus() == 200)
                            {
                                isLongerSuccessful = true;
                            }
                        }

                    }while (currentTime < longerExpiration && !isLongerSuccessful);
                }

                if(isShorterSuccessful && isLongerSuccessful)
                {
                    return Response.status(200).entity(null).build();
                }
                else
                {
                    // Rollback transaction
                    // 408 request Timeout
                    String msg = "Reservation Timeout";
                    return Response.status(408).entity(msg).build();

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(408).entity(null).build();
    }
}
