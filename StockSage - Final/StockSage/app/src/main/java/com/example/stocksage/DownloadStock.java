package com.example.stocksage;

import android.util.Log;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.CloseableHttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.entity.StringEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.CloseableHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClients;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;


public class DownloadStock {

    public interface DownloadCompleteListener {
        void onDownloadComplete(String result);
    }

    // DON'T SHARE!!
    private static final String MARKETAUX_API_KEY = "PZqcesEVXdow3XSyeNsr76PHHsep3h7Rbu6HiPHy";
    private static final String OPENAI_API_KEY = "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";

    public static void download(String stockString, DownloadCompleteListener listener) {

        final String stock = stockString.toUpperCase();

        new Thread(() -> {

            System.out.println("Downloading: " + stock);
            String apiUrl = "https://api.marketaux.com/v1/news/all";
            Map<String, String> parameters = new HashMap<>();
            parameters.put("api_token", MARKETAUX_API_KEY);
            parameters.put("symbols", stock);
            parameters.put("limit", "1");

            try {
                StringJoiner sj = new StringJoiner("&");
                for(Map.Entry<String,String> entry : parameters.entrySet()) {
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
                }

                URL url = new URL(apiUrl + "?" + sj.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000); // 15 seconds connection timeout
                conn.setReadTimeout(15000);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    conn.disconnect();


                    listener.onDownloadComplete("Analyzing the news...");

                    String response = getResponseFromGpt(content.toString(), stock);

                    listener.onDownloadComplete(response);
                } else {
                    System.out.println("GET request not worked");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getResponseFromGpt(String prompt, String stock) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
//            systemMessage.put("content", "You are helping user to buy, sell or hold a stock based on the sentiment analysis of the news. Your answer should be one of 3: Buy, Sell or Hold. Don't say anything else just say Buy, Sell or Hold no explanation needed.");
            systemMessage.put("content", "You are helping user to buy, sell or hold " + stock + " stocks based on the sentiment analysis of the news. Your answer should start by one of these 3 words: Buy, Sell or Hold. Then try to explain if possible.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMessage);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4-1106-preview");
//            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", messages);

            StringEntity entity = new StringEntity(requestBody.toString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                System.out.println(jsonResponse);
                JSONObject obj = new JSONObject(jsonResponse);

                if (obj.has("error")) {
                    JSONObject errorObj = obj.getJSONObject("error");
                    return "Error: " + errorObj.optString("message", "An unknown error occurred");
                }

                JSONArray choices = obj.getJSONArray("choices");
                if (choices != null && choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    if (firstChoice.has("message") && firstChoice.getJSONObject("message").has("content")) {
                        String content = firstChoice.getJSONObject("message").getString("content");
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error in sending request";
    }
}
