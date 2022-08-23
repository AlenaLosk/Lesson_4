import com.codeborne.selenide.*;
import com.codeborne.selenide.selector.ByText;
import com.github.javafaker.Faker;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
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
    String firstName, lastName, email, gender, number, hobby1, hobby2, year, month, day, address, state, city;
    Date birthday;

    @BeforeEach
    public void setUpEach() {
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

    @BeforeAll
    public static void setUp() {
        Configuration.baseUrl = "https://demoqa.com";
    }

    @AfterEach
    public void tearDownEach() {
        softAssertions.assertAll();
    }

    @Test
    public void fillFormTest() {
        // open page and check header form
        open("/automation-practice-form");
        $(".practice-form-wrapper").shouldHave(Condition.text("Student Registration Form"));

        // close advertising footer
        executeJavaScript("$('footer').remove()");
        executeJavaScript("$('#fixedban').remove()");

        // fill form
        $("#firstName").setValue(firstName);
        $("#lastName").setValue(lastName);
        $("#userEmail").setValue(email);
        $("#genterWrapper").$(byText(gender)).click();

        // scroll page down
        $("#userNumber").setValue(Keys.PAGE_DOWN);
        $("#userNumber").setValue(number);

        // select date in calendar
        $("#dateOfBirthInput").click(ClickOptions.usingJavaScript());
        $("[class*='year-select']").selectOption(year);
        $("[class*='month-select']").selectOption(month);
        $(String.format("[class*='day--%s']:not([class*='outside-month'])", String.format("%03d", Integer.parseInt(day)))).click();

        // select subject
        $("#subjectsInput").click(ClickOptions.usingJavaScript());
        $("#subjectsInput").setValue("a");
        ElementsCollection temp = $$("[class*=subjects-auto-complete__option]");
        String subject = temp.get(random.nextInt(temp.size())).getText();
        temp.findBy(Condition.text(subject)).click();

        // select hobbies
        temp = $$("#hobbiesWrapper .custom-checkbox");
        temp.findBy(Condition.text(hobby1)).click();
        temp.findBy(Condition.text(hobby2)).click();

        // fill address and upload picture
        $("#currentAddress").setValue(address);
        $("#uploadPicture").uploadFile(new File("src/test/resources/stitch.jpg"));
        //$("#uploadPicture").uploadFromClasspath("stitch.jpg"); - доп. вариант загрузки из папки resources

        // final actions
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
        $("#state").click();
        $(byText(state)).click();
        $("#city").click();
        $(byText(city)).click();
        $("#submit").click();

        // check data: inserted information and from notification window ones
        $(".modal-dialog").should(Condition.appear);
        $(".modal-title").shouldHave(Condition.text("Thanks for submitting the form"));
        SelenideElement element = $(".table-responsive table");

        element.$(byText("Student Name")).parent().shouldHave(Condition.text(firstName + " " + lastName));
        element.$(byText("Student Email")).parent().shouldHave(Condition.text(email));
        element.$(byText("Gender")).parent().shouldHave(Condition.text(gender));
        element.$(byText("Mobile")).parent().shouldHave(Condition.text(number));
        element.$(byText("Date of Birth")).parent()
                .shouldHave(Condition.text(new SimpleDateFormat("dd MMMM,yyyy", Locale.ENGLISH).format(birthday)));
        element.$(byText("Subjects")).parent().shouldHave(Condition.text(subject));
        element.$(byText("Hobbies")).parent().shouldHave(Condition.text(hobby1 + ", " + hobby2));
        element.$(byText("Picture")).parent().shouldHave(Condition.text("stitch.jpg"));
        element.$(byText("Address")).parent().shouldHave(Condition.text(address));
        element.$(byText("State and City")).parent().shouldHave(Condition.text(state + " " + city));
    }
}
