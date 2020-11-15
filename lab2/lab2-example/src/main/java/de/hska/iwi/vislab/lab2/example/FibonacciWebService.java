package de.hska.iwi.vislab.lab2.example;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "fbcc" path)
 */
@Path("fbcc")
@Singleton
public class FibonacciWebService {
    private int fibonacciIndex = 0;

    private void setFibonacciIndex(int i){
        this.fibonacciIndex = i;
    }

    private int getFibonacciIndex(){
        return this.fibonacciIndex;
    }

    private int getFibonacci() {
        int prevSum = 1;
        if (getFibonacciIndex() == 0) {
            setFibonacciIndex(1);
            return 0;
        } else if (getFibonacciIndex() == 1) {
            setFibonacciIndex(2);
            return 1;
        } else if (getFibonacciIndex() == 2) {
            setFibonacciIndex(3);
            return 1;
        } else {
            int sum = prevSum;
            for (int i = 3; i <= getFibonacciIndex(); i++) {
                int oldSum = sum;
                sum = sum + prevSum;
                prevSum = oldSum;
            }
            setFibonacciIndex(getFibonacciIndex()+1);
            return sum;
        }
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return Integer.toString(getFibonacci());
    }

    @DELETE
    public Response restartFibonacci() {
        setFibonacciIndex(0);
        return Response.noContent().build();
    }

    /**
    * Method handling HTTP PUT request. Updates current fibonacci number.
    */ 
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public Response specificFibonacci(@QueryParam("specificFibonacci") int specificFibonacci) {
        setFibonacciIndex(specificFibonacci);
        return Response.ok(getFibonacci()).build();
    }
}
