package edu.sjsu.cmpe172;

import org.springframework.web.client.RestTemplate;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HelloClient {

    public static void main(String[] args) {
        // Show usage if no args or invalid args
        if (args.length == 0 || (!args[0].equals("list") && !args[0].equals("post"))) {
            System.out.println("Usage: java -jar helloClient.jar list");
            System.out.println("Usage: java -jar helloClient.jar post <message>");
            return;
        }

        if (args[0].equals("post") && args.length != 2) {
            System.out.println("Usage: java -jar helloClient.jar post <message>");
            return;
        }

        // Load config
        Properties props = new Properties();
        try {
            String configPath = System.getProperty("user.home") + "/.config/cmpe172hello.properties";
            props.load(new FileInputStream(configPath));
        } catch (Exception e) {
            System.err.println("Could not read config file: " + e.getMessage());
            return;
        }

        String baseUrl = props.getProperty("baseUrl");
        String token = props.getProperty("token");
        String author = props.getProperty("author");

        RestTemplate restTemplate = new RestTemplate();

        try {
            if (args[0].equals("list")) {
                listMessages(restTemplate, baseUrl);
            } else {
                postMessage(restTemplate, baseUrl, token, author, args[1]);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void listMessages(RestTemplate restTemplate, String baseUrl) {
        ZoneId zone = ZoneId.systemDefault();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int page = 0;
        while (true) {
            Map<String, Object> response = restTemplate.getForObject(baseUrl + "/posts?page=" + page, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

            for (Map<String, Object> message : content) {
                Long id = ((Number) message.get("id")).longValue();
                var zdt = Instant.ofEpochMilli(id).atZone(zone);
                System.out.printf("%s %s said %s%n",
                        zdt.format(dtf),
                        message.get("author"),
                        message.get("message"));
            }

            if ((Boolean) response.get("last")) break;
            page++;
        }
    }

    private static void postMessage(RestTemplate restTemplate, String baseUrl, String token, String author, String message) {
        ZoneId zone = ZoneId.systemDefault();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("author", author);
        requestBody.put("message", message);
        requestBody.put("token", token);

        Map<String, Object> response = restTemplate.postForObject(baseUrl + "/posts", requestBody, Map.class);

        Long id = ((Number) response.get("id")).longValue();
        var zdt = Instant.ofEpochMilli(id).atZone(zone);
        System.out.printf("%s %s said %s%n",
                zdt.format(dtf),
                response.get("author"),
                response.get("message"));
    }
}