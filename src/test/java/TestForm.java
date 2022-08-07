import com.codeborne.selenide.*;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class TestForm {
    @Test
    public void successfulFillForm() {
        Configuration.baseUrl = "https://demoqa.com";

        // generate data for inputs
        Random random = new Random();
        String[] locale = {"bg", "ca", "ca-CAT", "da-DK", "de", "de-AT", "de-CH", "en", "en-AU", "en-au-ocker",
                "en-BORK", "en-CA", "en-GB", "en-IND", "en-MS", "en-NEP", "en-NG", "en-NZ", "en-PAK", "en-SG",
                "en-UG", "en-US", "en-ZA", "es", "es-MX", "fa", "fi-FI", "fr", "he", "hu", "in-ID", "it", "ja",
                "ko", "nb-NO", "nl", "pl", "pt", "pt-BR", "ru", "sk", "sv", "sv-SE", "tr", "uk", "vi", "zh-CN", "zh-TW"};
        Faker faker = new Faker(new Locale(locale[random.nextInt(locale.length)]));
        Faker fakerEn = new Faker(new Locale("en-US"));
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = fakerEn.internet().emailAddress();
        String gender = List.of("Male", "Female", "Other").get(random.nextInt(3));
        String number = faker.phoneNumber().subscriberNumber(10);
        List<String> hobbies = List.of("Sports", "Reading", "Music");
        String hobby1 = hobbies.get(random.nextInt(3));
        hobbies = hobbies.stream().filter(e -> !e.equals(hobby1)).collect(Collectors.toList());
        String hobby2 = hobbies.get(random.nextInt(2));
        Date birthday = faker.date().birthday();
        String year = String.valueOf(1900 + birthday.getYear());
        String month = Month.of(birthday.getMonth() + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String day = String.valueOf(birthday.getDate());
        String address = faker.address().fullAddress();
        String state = List.of("NCR", "Uttar Pradesh", "Haryana", "Rajasthan").get(random.nextInt(5));
        List<String> cites = Map.of("NCR", List.of("Delhi", "Gurgaon", "Noida"),
                "Uttar Pradesh", List.of("Agra", "Lucknow", "Merrut"),
                        "Haryana", List.of("Karnal", "Panipat"),
                "Rajasthan", List.of("Jaipur", "Jaiselmer")).get(state);
        String city = cites.get(random.nextInt(cites.size()));

        // open page and fill form
        open("/automation-practice-form");
        $("#firstName").sendKeys(firstName);
        $("#lastName").sendKeys(lastName);
        $("#userEmail").sendKeys(email);
        $("#genterWrapper").$(byText(gender)).click();
        // scroll page down
        $("#userNumber").sendKeys(Keys.PAGE_DOWN);
        $("#userNumber").sendKeys(number);
        // select date in calendar
        $("#dateOfBirthInput").doubleClick();
        $("[class*='year-select']").selectOption(year);
        $("[class*='month-select']").selectOption(month);
        $x(String.format(".//div[contains(@class, 'datepicker__day') and contains(text(), %s)]", day)).click();
        ElementsCollection temp = $$("#hobbiesWrapper .custom-checkbox");
        temp.findBy(Condition.text(hobby1)).click();
        temp.findBy(Condition.text(hobby2)).click();
        $("#currentAddress").sendKeys(address);
        $("#uploadPicture").uploadFile(new File("src/test/resources/stitch.jpg"));

        // resize page window due to see submit button (it is hidden by contextual advertising)
        webdriver().driver().getWebDriver().manage().window().setSize(new Dimension(700, 600));
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
        $("#state").click();
        $(byText(state)).click();
        $("#city").click();
        $(byText(city)).click();
        $("#submit").click();

        // collect data from notification window
        List<String> list = new ArrayList<>();
        $$("[class='modal-body'] td").asFixedIterable().forEach(e -> list.add(e.getText()));
        HashMap<String, String> data = new HashMap<>();
        for (int i = 0; i < 20; i += 2) {
            data.put(list.get(i), list.get(i + 1));
        }

        // check data: inserted information and from notification window ones
        Assertions.assertEquals(firstName + " " + lastName, data.get("Student Name"));
        Assertions.assertEquals(email, data.get("Student Email"));
        Assertions.assertEquals(gender, data.get("Gender"));
        Assertions.assertEquals(number, data.get("Mobile"));
        Assertions.assertEquals(new SimpleDateFormat("dd MMMM,yyyy", Locale.ENGLISH).format(birthday), data.get("Date of Birth"));
        Assertions.assertEquals(hobby1 + ", " + hobby2, data.get("Hobbies"));
        Assertions.assertEquals("stitch.jpg", data.get("Picture"));
        Assertions.assertEquals(address, data.get("Address"));
        Assertions.assertEquals(state + " " + city, data.get("State and City"));
        sleep(4000);
    }
}
