package healer;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import utils.XPathUtils;

import java.util.concurrent.TimeUnit;

public class OpenAIHealer {

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client;
    private final String apiKey;
    
    public OpenAIHealer(String apiKey) {
        this.apiKey = apiKey;

        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public By heal(String brokenXpath, String pageSource) {

        String dom = pageSource.substring(
                0, Math.min(pageSource.length(), 12000));

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4-vision-preview");
        body.put("temperature", 0);

        JSONArray messages = new JSONArray();

        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "You are a senior Selenium automation expert. " +
                        "Return ONLY a valid XPath. No explanation."));

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", buildPrompt(brokenXpath, dom)));

        body.put("messages", messages);

        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        body.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new RuntimeException("OpenAI error: " + response);
            }

            String raw =
                    new JSONObject(response.body().string())
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

            String xpath = XPathUtils.sanitizeXPath(raw);

            System.out.println("🤖 GPT-4 healed XPath: " + xpath);

            return By.xpath(xpath);

        } catch (Exception e) {
            throw new RuntimeException("AI healing failed", e);
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


