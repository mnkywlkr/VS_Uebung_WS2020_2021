package de.hska.iwi.vislab.lab1.example.ws;

import javax.jws.WebService;

@WebService(endpointInterface = "de.hska.iwi.vislab.lab1.example.ws.FibonacciService")
public class FibonacciServiceImpl implements FibonacciService {

    @Override
    public int getFibonacci(int fibonacciNumber) {
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

    public int getNextFibonacci(int fibonacciNumber) {
        return getFibonacci(fibonacciNumber + 1);
    }
}
