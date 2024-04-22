package e2e;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.collections.ListSize;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;


public class BasicE2eTest {

    void init() {
       // Configuration.downloadsFolder
    }

    @Test
    void userListIsDisplayedE2e() {
        open("/");
        $("a[href='/users']").click();
        SelenideElement element = $("app-user-list");
        element.shouldBe(visible);
        element.$$("table tr").shouldHave(new ListSize(6));
    }
}
