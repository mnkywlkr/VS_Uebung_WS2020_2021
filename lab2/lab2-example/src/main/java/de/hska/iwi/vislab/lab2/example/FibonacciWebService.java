package de.hska.iwi.vislab.lab2.example;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("fbcc")
public class FibonacciWebService {

    private int fibonacciNumber = 0;

    private void setFibonacciNumber(int i){
        this.fibonacciNumber = i;
    }

    private int getFibonacciNumber(){
        return this.fibonacciNumber;
    }

    private int getFibonacci() {
        int prevSum = 1;
        if (getFibonacciNumber() == 1) {
            setFibonacciNumber(2);
            return 0;
        } else if (getFibonacciNumber() == 2) {
            setFibonacciNumber(3);
            return prevSum;
        } else {
            int sum = prevSum;
            for (int i = 3; i <= getFibonacciNumber(); i++) {
                int oldSum = sum;
                sum = sum + prevSum;
                prevSum = oldSum;
            }
            setFibonacciNumber(getFibonacciNumber()+1);
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
        setFibonacciNumber(0);
    }
}
