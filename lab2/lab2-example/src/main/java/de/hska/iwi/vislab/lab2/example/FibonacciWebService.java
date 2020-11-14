package de.hska.iwi.vislab.lab2.example;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT; 
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "myresource" path)
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
    public void restartFibonacci() {
        setFibonacciIndex(0);
    }

    /**
    * Method handling HTTP PUT request. Updates current fibonacci number.
    */ 
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response specificFibonacci(@QueryParam ("fibonaccinumber") int i){
        setFibonacciIndex(i);
        return Response.ok(getFibonacci()).build();
    }
}
