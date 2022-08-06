import com.codeborne.selenide.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;

public class TestForm {
    @Test
    public void successfulFillForm() {
        open("https://demoqa.com/automation-practice-form");
        $("#firstName").sendKeys("Olga");
        $("#lastName").sendKeys("Ivanova");
        $("#userEmail").sendKeys("olga_ivanova@mail.com");

        //set gender
        $$("[for*='gender-radio']").get(1).click();
        //scroll page down
        $("#userNumber").sendKeys(Keys.PAGE_DOWN);
        $("#userNumber").sendKeys("9267573590");

        //set dateOfBirthInput
        $("#dateOfBirthInput").doubleClick();
        $("[class*='year-select']").selectOption("2020");
        $("[class*='month-select']").selectOption("May");
        $x(".//div[contains(@class, 'datepicker__day') and contains(text(), 15)]").click();

        //set hobbies
        ElementsCollection temp = $$("#hobbiesWrapper .custom-checkbox");
        temp.get(0).click();
        temp.get(1).click();
        $("#currentAddress").sendKeys("gljljljasgfoiuyyuwef120");
        $("#uploadPicture").uploadFile(new File("src/test/resources/stitch.jpg"));
        webdriver().driver().getWebDriver().manage().window().setSize(new Dimension(700, 600));
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
        $("#state").click();
        $$("[class*='menu'] [id*='react-select-3']").get(1).click();

        $("#city").click();
        $$("[class*='menu'] [id*='react-select-4']").get(0).click();
        $("#submit").click();

        //check data
        List<String> list = new ArrayList<>();
        $$("[class='modal-body'] td").asFixedIterable().forEach(e -> list.add(e.getText()));
        HashMap<String, String> data = new HashMap<>();
        for (int i = 0; i < 20; i += 2) {
            data.put(list.get(i), list.get(i + 1));
        }
        Assertions.assertEquals("Olga Ivanova", data.get("Student Name"));
        Assertions.assertEquals("olga_ivanova@mail.com", data.get("Student Email"));
        Assertions.assertEquals("Female", data.get("Gender"));
        Assertions.assertEquals("9267573590", data.get("Mobile"));
        Assertions.assertEquals("15 May,2020", data.get("Date of Birth"));
        Assertions.assertEquals("Sports, Reading", data.get("Hobbies"));
        Assertions.assertEquals("stitch.jpg", data.get("Picture"));
        Assertions.assertEquals("gljljljasgfoiuyyuwef120", data.get("Address"));
        Assertions.assertEquals("Uttar Pradesh Agra", data.get("State and City"));
        sleep(4000);
    }
}
