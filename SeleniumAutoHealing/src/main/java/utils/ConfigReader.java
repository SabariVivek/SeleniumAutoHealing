package utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream is =
                     ConfigReader.class.getClassLoader()
                             .getResourceAsStream("config.properties")) {

            if (is == null) {
                throw new RuntimeException("config.properties not found");
            }
            properties.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static boolean isHealingEnabled() {
        return Boolean.parseBoolean(
                properties.getProperty("healing.enabled", "false"));
    }

    public static String getOpenAIKey() {
        return properties.getProperty("openai.key");
    }
}


