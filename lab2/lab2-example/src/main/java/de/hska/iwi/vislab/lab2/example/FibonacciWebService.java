package de.hska.iwi.vislab.lab2.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("fbcc")
public class FibonacciWebService {

    private int getFibonacci(int fibonacciNumber) {
        int prevSum = 1;

        if (fibonacciNumber == 1) {
            return 0;
        } else if (fibonacciNumber == 2) {
            return prevSum;
        } else {
            int sum = prevSum;
            for (int i = 3; i <= fibonacciNumber; i++) {
                int oldSum = sum;
                sum = sum + prevSum;
                prevSum = oldSum;
            }
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
        return Integer.toString(getFibonacci(2 + 1));
    }
}
