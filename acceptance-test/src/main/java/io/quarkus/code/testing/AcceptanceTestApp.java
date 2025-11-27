package io.quarkus.code.testing;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.restassured.http.ContentType;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import org.hamcrest.Matchers;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;

@QuarkusMain
public class AcceptanceTestApp implements QuarkusApplication {

    public static final String DEFAULT_URL = "https://stage.code.quarkus.io";

    private static final Logger LOG = Logger.getLogger(AcceptanceTestApp.class);

    private static final String GENERATE_YOUR_APPLICATION_TEXT = "generate your application";
    public static final String LABEL_TOGGLE_FULL_LIST = "[aria-label='Toggle full list of extensions']";
    public static final String LABEL_DOWNLOAD_THE_ZIP = "[aria-label='Download the zip']";
    private static final int DEFAULT_MIN_EXTENSIONS = 50;
    private static final int DEFAULT_MIN_STREAMS = 1;

    private final TestConfig testConfig;

    public AcceptanceTestApp(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Override
    public int run(String... args) throws Exception {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setArgs(List.of("--headless", "--disable-gpu", "--no-sandbox")));
            BrowserContext context = browser.newContext();

            // Open new page
            Page page = context.newPage();
            final String url = testConfig.getUrl().orElse(DEFAULT_URL);
            if (url.contains(",")) {
                var urls = url.split(",");
                for (String u : urls) {
                    final int x = testUrl(u.trim(), page);
                    if (x != 0)
                        return x;
                }
            } else {
                final int x = testUrl(url.trim(), page);
                if (x != 0)
                    return x;
            }

            LOG.info("ACCEPTANCE TEST PASSED");
        }

        return 0;
    }

    private int testUrl(String url, Page page) {
        LOG.infof("Navigating to %s", url);
        page.navigate(url);
        final ElementHandle generateButton = page.waitForSelector(".generate-button");
        if (!generateButton.textContent().toLowerCase().contains(GENERATE_YOUR_APPLICATION_TEXT)) {
            LOG.errorf("%s was not found", GENERATE_YOUR_APPLICATION_TEXT);
            return 1;
        } else {
            LOG.infof("Generate button found: %s", generateButton.textContent());
        }

        final ElementHandle startCodingBlurbButton = page.waitForSelector(".quarkus-blurb .btn-secondary");
        LOG.info("Click on start coding");
        startCodingBlurbButton.click();

        final ElementHandle streamPicker = page.waitForSelector(".stream-picker");
        LOG.info("Click on stream-picker");
        streamPicker.click();

        final List<ElementHandle> streams = page.querySelectorAll(".stream-picker .dropdown-menu .dropdown-item");
        final Integer minStreams = testConfig.getMinStreams().orElse(DEFAULT_MIN_STREAMS);

        if (streams.size() < minStreams) {
            LOG.errorf("%s streams found is low than minimum requirement: %s", streams.size(), minStreams);
            return 1;
        } else {
            LOG.infof("%s streams found", streams.size());
        }
        page.waitForSelector(LABEL_TOGGLE_FULL_LIST).click();
        page.waitForSelector(".extensions-picker .extension-row");
        final List<ElementHandle> extensions = page.querySelectorAll(".extensions-picker .extension-row");
        final Integer minExtensions = testConfig.getMinExtensions().orElse(DEFAULT_MIN_EXTENSIONS);

        if (extensions.size() < minExtensions) {
            LOG.errorf("%s extensions found is low than minimum requirement: %s", extensions.size(), minExtensions);
            return 1;
        } else {
            LOG.infof("%s extensions found", extensions.size());
        }
        LOG.info("Click on first extensions");
        extensions.get(0).click(new ElementHandle.ClickOptions().setForce(true));

        LOG.info("Click on second extensions");
        extensions.get(1).click(new ElementHandle.ClickOptions().setForce(true));

        final ElementHandle extensionsCart = page.waitForSelector(".extensions-cart button");
        LOG.info("Focus on extensions-cart button");
        page.mouse().move(extensionsCart.boundingBox().x + 5, extensionsCart.boundingBox().y + 5);

        final List<ElementHandle> selected = page.querySelectorAll(".selected-extensions .extension-row");

        if (selected.size() != 2) {
            LOG.errorf("%s extensions selected instead of 2", selected.size());
            return 1;
        } else {
            LOG.infof("%s extensions selected", selected.size());
        }

        LOG.info("Click on generate button");
        generateButton.click();

        final ElementHandle downloadButton = page.waitForSelector(LABEL_DOWNLOAD_THE_ZIP);

        Response response = page.waitForResponse(resp -> resp.url().contains("/d") && resp.status() == 200, () -> {
            LOG.info("Click on download button");
            downloadButton.click();
        });

        String contentType = response.headerValue("Content-Type");
        if (contentType != null && contentType.contains("zip")) {
            LOG.info("Download link returned 200 and is a ZIP file");
        } else {
            LOG.error("Download link did not return a ZIP file");
            return 1;
        }

        LOG.infof("ACCEPTANCE TEST PASSED FOR %s", url);

        given()
                .accept(ContentType.JSON)
                .baseUri(url)
                .when().get("/api/config")
                .then()
                .statusCode(200)
                .body("stageBuild", Matchers.in(new Boolean[] { null, false }));
        return 0;
    }

    private void screenshot(Page page) throws IOException {
        final Path screenshot = Files.createTempFile("screenshot", ".png");
        Files.write(screenshot, page.screenshot());
        System.out.println(screenshot);
    }

    @ConfigMapping(prefix = "test-config")
    public interface TestConfig {
        @WithName("url")
        Optional<String> getUrl();

        @WithName("min-extensions")
        Optional<Integer> getMinExtensions();

        @WithName("min-streams")
        Optional<Integer> getMinStreams();
    }

}
