package com.talk_space;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;

public class ParallelPostRequests {

    public static void main(String[] args) throws InterruptedException {
        int numberOfRequests = 26;

        HttpClient client = HttpClient.newHttpClient();

        CountDownLatch readyLatch = new CountDownLatch(numberOfRequests);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);

        String jsonBody = """
                {
                  "email": "mariam.pet97@gmail.com",
                  "password": "Mariam_P97#"
                }
                """;

        for (int i = 1; i <= numberOfRequests; i++) {
            int requestId = i;
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8080/account/auth"))
                            .header("Content-Type", "application/json")
                            .header("Origin", "http://localhost:3000")
                            .header("Referer", "http://localhost:3000/")
                            .header("User-Agent", "Mozilla/5.0")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    System.out.printf("Request %d: %d%n", requestId, response.statusCode());
                } catch (Exception e) {
                    System.out.printf("Request %d: Failed (%s)%n", requestId, e.getMessage());
                }
            });
        }

        readyLatch.await();

        long startTime = System.currentTimeMillis();

        startLatch.countDown();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("All requests sent in " + elapsed + " ms");
    }
}
