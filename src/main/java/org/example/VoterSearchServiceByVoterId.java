package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class VoterSearchServiceByVoterId {

    private static final String BASE_URL =
            "https://voter.nagpurnmc.in/api/api/search";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void searchVoter(String[] voterIds) {
        int totalcount =0;
        for(String voterId : voterIds) {
            voterId = voterId.trim();
            String encodedName = URLEncoder.encode(voterId, StandardCharsets.UTF_8);

            for (int prabhag = 1; prabhag <= 38; prabhag++) {

                try {
                    String url = BASE_URL +
                            "?q=" + encodedName +
                            "&type=voterid" +
                            "&prabhag=" + prabhag +
                            "&language=english";

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

                    HttpResponse<String> response =
                            client.send(request, HttpResponse.BodyHandlers.ofString());

                    JsonNode root = mapper.readTree(response.body());
                    int count = root.path("count").asInt();

                    // Skip null / empty results
                    if (count == 0) {
                        continue;
                    }

                    System.out.println("\n===============================");
                    System.out.println("Results found in Prabhag: " + prabhag);
                    System.out.println("Total Count: " + count);

                    JsonNode results = root.path("results");

                    for (JsonNode voter : results) {

                        String resultVoterId = voter.path("voter_id").asText();
                        String resultName = voter.path("name_english").asText();
                        String gender = voter.path("gender").asText();
                        String age = voter.path("age").asText();
                        String booth = voter.path("booth_name").asText();
                        String prabhagNo = voter.path("prabhag_kramank").asText();

                        // Print each result
                        System.out.println("-------------------------------");
                        System.out.println("Prabhag No : " + prabhagNo);
                        System.out.println("Name      : " + resultName);
                        System.out.println("Voter ID  : " + resultVoterId);
                        System.out.println("Gender    : " + gender);
                        System.out.println("Age       : " + age);
                        System.out.println("Booth     : " + booth);
                    }
                    totalcount++;
                    System.out.println("\n✅ Matching voter_id found in prabhag "+prabhag+".");
                    break;
                } catch (Exception e) {
                    System.err.println("Error in prabhag " + prabhag + ": " + e.getMessage());
                }
            }
        }
        if (totalcount==0){
            System.out.println("\n❌ No matching voter_id found in any prabhag (1–38).");
        }
    }

    // Example usage
    public static void main(String[] args) {
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {

            System.out.print("Enter voter ID: ");
            String voterId = scanner.nextLine();
            String[] voterIds = voterId.split(",");
            searchVoter(voterIds);
        }
    }
}

