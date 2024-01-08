import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.code.service.PlatformService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

@QuarkusTest
@WithPlaywright(verbose = true)
public class CodeQuarkusCommunityPlaywrightTest {

    public static final String LABEL_GENERATE_YOUR_APPLICATION = "[aria-label='Generate your application']";
    public static final String LABEL_DOWNLOAD_THE_ZIP = "[aria-label='Download the zip']";
    @Inject
    PlatformService platformService;

    @InjectPlaywright
    BrowserContext context;

    @TestHTTPResource("/")
    URL index;

    @Test
    public void testIndex() {
        try (Page page = context.newPage()) {
            Response response = page.navigate(index.toString());
            Assertions.assertEquals("OK", response.statusText());

            page.waitForLoadState();

            String title = page.title();
            Assertions.assertEquals("Quarkus - Start coding with code.quarkus.io", title);
        }
    }

    @Test
    public void testGenerateDefault(TestInfo testInfo) throws Throwable {
        try (Page page = openIndex()) {
            page.waitForSelector(LABEL_GENERATE_YOUR_APPLICATION).click();
            CountDownLatch latch = new CountDownLatch(1);
            page.onResponse(response -> {
                // Check if the URL of the response matches the expected URL of the zip file
                if (response.url().endsWith(".zip")) {
                    Assertions.assertEquals(200, response.status());
                    latch.countDown();
                }
            });

            page.waitForSelector(LABEL_DOWNLOAD_THE_ZIP).click();
            latch.await();
        }
    }

    private Page openIndex() {
        final Page page = context.newPage();
        Response response = page.navigate(index.toString());
        Assertions.assertEquals("OK", response.statusText());
        closeIntroductionModal(page);
        return page;
    }

    private static void closeIntroductionModal(Page page) {
        // Find the introduction modal close button
        page.waitForSelector("[aria-label='Close the introduction modal']").click();
    }

}
