import com.codeborne.selenide.*;
import com.github.javafaker.Faker;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class TestForm {
    private SoftAssertions softAssertions;
    Random random = new Random();
    String firstName;
    String lastName;
    String email;
    String gender;
    String number;
    String hobby1;
    String hobby2;
    Date birthday;
    String year;
    String month;
    String day;
    String address;
    String state;
    String city;
    @BeforeEach
    public void setUp() {
        Configuration.baseUrl = "https://demoqa.com";
        softAssertions = new SoftAssertions();
        // generate data for inputs
        String[] locale = {"bg", "ca", "ca-CAT", "da-DK", "de", "de-AT", "de-CH", "en", "en-AU", "en-au-ocker",
                "en-BORK", "en-CA", "en-GB", "en-IND", "en-MS", "en-NEP", "en-NG", "en-NZ", "en-PAK", "en-SG",
                "en-UG", "en-US", "en-ZA", "es", "es-MX", "fa", "fi-FI", "fr", "he", "hu", "in-ID", "it", "ja",
                "ko", "nb-NO", "nl", "pl", "pt", "pt-BR", "ru", "sk", "sv", "sv-SE", "tr", "uk", "vi", "zh-CN", "zh-TW"};
        Faker faker = new Faker(new Locale(locale[random.nextInt(locale.length)]));
        Faker fakerEn = new Faker(new Locale("en-US"));
        firstName = faker.name().firstName();
        lastName = faker.name().lastName();
        email = fakerEn.internet().emailAddress();
        gender = List.of("Male", "Female", "Other").get(random.nextInt(3));
        number = faker.phoneNumber().subscriberNumber(10);
        List<String> hobbies = List.of("Sports", "Reading", "Music");
        hobby1 = hobbies.get(random.nextInt(3));
        hobbies = hobbies.stream().filter(e -> !e.equals(hobby1)).collect(Collectors.toList());
        hobby2 = hobbies.get(random.nextInt(2));
        birthday = faker.date().birthday();
        year = String.valueOf(1900 + birthday.getYear());
        month = Month.of(birthday.getMonth() + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        day = String.valueOf(birthday.getDate());
        address = faker.address().fullAddress();
        state = List.of("NCR", "Uttar Pradesh", "Haryana", "Rajasthan").get(random.nextInt(4));
        List<String> cites = Map.of("NCR", List.of("Delhi", "Gurgaon", "Noida"),
                "Uttar Pradesh", List.of("Agra", "Lucknow", "Merrut"),
                "Haryana", List.of("Karnal", "Panipat"),
                "Rajasthan", List.of("Jaipur", "Jaiselmer")).get(state);
        city = cites.get(random.nextInt(cites.size()));
    }

    @AfterEach
    public void tearDown() {
        softAssertions.assertAll();
    }

    @Test
    public void successfulFillForm() {
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
        $("#dateOfBirthInput").click(ClickOptions.usingJavaScript());
        $("[class*='year-select']").selectOption(year);
        $("[class*='month-select']").selectOption(month);
        $x(String.format(".//div[contains(@class, 'datepicker__day') and contains(text(), %s)]", day)).click();
        $("#subjectsInput").click(ClickOptions.usingJavaScript());
        $("#subjectsInput").setValue("a");
        ElementsCollection temp = $$("[class*=subjects-auto-complete__option]");
        String subject = temp.get(random.nextInt(temp.size())).getText();
        temp.findBy(Condition.text(subject)).click();
        temp = $$("#hobbiesWrapper .custom-checkbox");
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
        softAssertions.assertThat((firstName + " " + lastName).equals(data.get("Student Name")));
        softAssertions.assertThat(email.equals(data.get("Student Email")));
        softAssertions.assertThat(gender.equals(data.get("Gender")));
        softAssertions.assertThat(number.equals(data.get("Mobile")));
        softAssertions.assertThat(new SimpleDateFormat("dd MMMM,yyyy", Locale.ENGLISH).format(birthday)
                .equals(data.get("Date of Birth")));
        softAssertions.assertThat(subject.equals(data.get("Subject")));
        softAssertions.assertThat((hobby1 + ", " + hobby2).equals(data.get("Hobbies")));
        softAssertions.assertThat("stitch.jpg".equals(data.get("Picture")));
        softAssertions.assertThat(address.equals(data.get("Address")));
        softAssertions.assertThat((state + " " + city).equals(data.get("State and City")));
    }
}
