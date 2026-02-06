package healer;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import utils.XPathUtils;

import java.util.concurrent.TimeUnit;

public class GeminiHealer {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";

    private final OkHttpClient client;
    private final String apiKey;

    public GeminiHealer(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public By heal(String brokenXpath, String pageSource) {
        String dom = pageSource.substring(0, Math.min(pageSource.length(), 12000));

        JSONObject prompt = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONArray parts = new JSONArray();
        parts.put(new JSONObject().put("text",
                "You are a senior Selenium automation expert. Return ONLY a valid XPath. No explanation.\n" +
                buildPrompt(brokenXpath, dom)));
        contents.put(new JSONObject().put("parts", parts));
        prompt.put("contents", contents);

        Request request = new Request.Builder()
                .url(GEMINI_URL + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(prompt.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Gemini error: " + response);
            }
            JSONObject responseBody = new JSONObject(response.body().string());
            String raw = responseBody
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            String xpath = XPathUtils.sanitizeXPath(raw);
            System.out.println("🤖 Gemini healed XPath: " + xpath);
            return By.xpath(xpath);
        } catch (Exception e) {
            throw new RuntimeException("Gemini healing failed", e);
        }
    }

    private String buildPrompt(String brokenXpath, String dom) {
        StringBuilder sb = new StringBuilder();
        sb.append("Broken XPath:\n")
          .append(brokenXpath)
          .append("\n\nHTML Page Source:\n")
          .append(dom)
          .append("\n\nTask:\n")
          .append("- Identify the intended element\n")
          .append("- Generate the most stable XPath\n")
          .append("- Return ONLY XPath\n");
        return sb.toString();
    }
}
