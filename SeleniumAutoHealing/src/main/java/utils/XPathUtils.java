package utils;

import org.openqa.selenium.By;

public class XPathUtils {

    public static String extractXPath(By locator) {

        String value = locator.toString();

        if (!value.startsWith("By.xpath:")) {
            throw new IllegalArgumentException(
                    "AI healing supports XPath only");
        }

        return value.replace("By.xpath: ", "").trim();
    }

    public static String sanitizeXPath(String aiResponse) {

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new RuntimeException("AI returned empty XPath");
        }

        String xpath = aiResponse.trim();

        xpath = xpath.replace("```", "");
        xpath = xpath.replaceAll("^xpath\\s*=", ""); //xpath=
        xpath = xpath.replaceAll("^\"|\"$", "");

        if (!xpath.startsWith("//") && !xpath.startsWith("(//")) {
            throw new RuntimeException(
                    "Invalid XPath returned by AI: " + xpath);
        }

        return xpath;
    }
}


