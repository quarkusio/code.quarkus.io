package io.quarkus.code;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

@QuarkusTest
@WithPlaywright(verbose = true)
@TestProfile(CodeQuarkusPlaywrightTest.PlaywrightTestProfile.class)
public class CodeQuarkusPlaywrightTest {
    @InjectPlaywright
    BrowserContext context;

    @TestHTTPResource("/")
    URL index;

    @Test
    public void testIndex() {
        final Page page = context.newPage();
        Response response = page.navigate(index.toString());
        Assertions.assertEquals("OK", response.statusText());

        page.waitForLoadState();

        String title = page.title();
        Assertions.assertEquals("Quarkus - Start coding with code.quarkus.io", title);
    }

    public static final class PlaywrightTestProfile implements QuarkusTestProfile {

        @Override public String getConfigProfile() {
            return QuarkusTestProfile.super.getConfigProfile() + ",playwright";
        }

        public PlaywrightTestProfile() {
        }
    }
}
