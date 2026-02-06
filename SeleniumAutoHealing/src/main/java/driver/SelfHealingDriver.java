package driver;

import healer.OpenAIHealer;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.XPathUtils;

import java.time.Duration;

public class SelfHealingDriver {

    private final WebDriver driver;
    private final boolean healingEnabled;
    private final OpenAIHealer healer;
    private final WebDriverWait wait;

    public SelfHealingDriver(WebDriver driver) {
        this.driver = driver;
        this.healingEnabled = ConfigReader.isHealingEnabled();
        this.healer = new OpenAIHealer(ConfigReader.getOpenAIKey());
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public WebElement findElement(By locator) {

        try {
            // ✅ WAIT before finding
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));

        } catch (TimeoutException | NoSuchElementException e) {

            if (!healingEnabled) {
                throw e;
            }

            // 🔒 AI healing ONLY for XPath
            if (!locator.toString().startsWith("By.xpath:")) {
                System.out.println(
                        "Skipping AI healing (non-XPath): " + locator);
                throw e;
            }

            System.out.println("⚠ Broken XPath: " + locator);

            String brokenXpath = XPathUtils.extractXPath(locator);
            String pageSource = driver.getPageSource();

            By healed = healer.heal(brokenXpath, pageSource);

            // ✅ WAIT again for healed XPath
            return wait.until(
                    ExpectedConditions.presenceOfElementLocated(healed)
            );
        }
    }

    public void quit() {
        driver.quit();
    }
}