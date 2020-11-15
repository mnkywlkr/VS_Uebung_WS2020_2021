package de.hska.iwi.vislab.lab2.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class FibonacciWebServiceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        server.start();
        // create the client
        Client c = ClientBuilder.newClient();
        c.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);


        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and
        // Main.startServer())
        // --
        // c.configuration().enable(new
        // org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);
        target.path("/fbcc").request(MediaType.TEXT_PLAIN).delete();

    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void receiveFirstFibonacciNumberTest() {
        String fibonacciNumber = target
                .path("fbcc")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertEquals("0", fibonacciNumber);
    }

    @Test
    public void deleteFibonacciCounter() {
        String fibonacciNumber = "0";
        // Zwei mal die aktuelle Fibonacci Zahl holen
        for (int i = 0; i < 2; i++) {
            fibonacciNumber = target
                    .path("fbcc")
                    .request()
                    .accept(MediaType.TEXT_PLAIN)
                    .get(String.class);
        }

        // 0 1 1 2 3 5 8 13 ...
        Assert.assertEquals("1", fibonacciNumber);

        target.path("fbcc").request().delete();

        fibonacciNumber = target
                .path("fbcc")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);

        Assert.assertEquals("0", fibonacciNumber);
    }

    @Test
    public void setSpecificFibonacciIndex() {
        final int initalRequestedIndex = 5;

        String fibonacciNumber = "0";
        // Get the initial fibonacci number
        for (int i = 0; i < initalRequestedIndex; i++) {
            fibonacciNumber = target
                    .path("fbcc")
                    .request()
                    .accept(MediaType.TEXT_PLAIN)
                    .get(String.class);
        }

        // 0 1 1 2 3 5 8 13 ...
        Assert.assertEquals("3", fibonacciNumber);

        final String newSpecificIndex = "2";
        final String afterIndexUpdate = target
				.path("fbcc")
				.queryParam("specificFibonacci", newSpecificIndex)
				.request().put(null, String.class);

        Assert.assertEquals("1", afterIndexUpdate);

        fibonacciNumber = target
                .path("fbcc")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);

        // 0 1 1 2 3 5 8 13 ...
        Assert.assertEquals("2", fibonacciNumber);
    }
}
