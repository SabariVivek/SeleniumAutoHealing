package tests;

import driver.SelfHealingDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class OrangeHRMTest {

    @Test
    public void loginTest() {

        WebDriver baseDriver = new ChromeDriver();
        SelfHealingDriver driver = new SelfHealingDriver(baseDriver);

        baseDriver.get("https://opensource-demo.orangehrmlive.com/");

        driver.findElement(By.name("username")).sendKeys("Admin");
        driver.findElement(By.name("password")).sendKeys("admin123");

        // Intentionally broken locator
        driver.findElement(By.xpath("//button[@type='wrong']")).click();
        ////button[@type='submit']

        driver.quit();
    }
}


