package io.quarkus.code.testing;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Playwright.CreateOptions;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@QuarkusMain
public class AcceptanceTestApp implements QuarkusApplication {

    public static final String DEFAULT_URL = "https://stage.code.quarkus.io";

    private static final Logger LOG = LoggerFactory.getLogger(AcceptanceTestApp.class);

    private static final String GENERATE_YOUR_APPLICATION_TEXT = "generate your application";
    private static final String DOWNLOAD_ZIP_TEXT = "download the zip";
    private static final int DEFAULT_MIN_EXTENSIONS = 50;
    private static final int DEFAULT_MIN_STREAMS = 1;

    private final TestConfig testConfig;

    public AcceptanceTestApp(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Override public int run(String... args) throws Exception {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setArgs(List.of("--headless", "--disable-gpu", "--no-sandbox")));
            BrowserContext context = browser.newContext();

            // Open new page
            Page page = context.newPage();
            final String url = testConfig.getUrl().orElse(DEFAULT_URL);
            LOG.info("Navigating to {}", url);
            page.navigate(url);
            final ElementHandle generateButton = page.waitForSelector(".generate-button");
            if (!generateButton.textContent().toLowerCase().contains(GENERATE_YOUR_APPLICATION_TEXT)) {
               LOG.error("{} was not found", GENERATE_YOUR_APPLICATION_TEXT);
               return 1;
            } else {
                LOG.info("Generate button found: {}", generateButton.textContent());
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
                LOG.error("{} streams found is low than minimum requirement: {}", streams.size(), minStreams);
                return 1;
            } else {
                LOG.info("{} streams found", streams.size());
            }

            page.waitForSelector(".extensions-picker .extension-row");
            final List<ElementHandle> extensions = page.querySelectorAll(".extensions-picker .extension-row");
            final Integer minExtensions = testConfig.getMinExtensions().orElse(DEFAULT_MIN_EXTENSIONS);

            if (extensions.size() < minExtensions) {
                LOG.error("{} extensions found is low than minimum requirement: {}", extensions.size(), minExtensions);
                return 1;
            } else {
                LOG.info("{} extensions found", extensions.size());
            }
            LOG.info("Click on first extensions");
            extensions.get(0).click(new ElementHandle.ClickOptions().setForce(true));

            LOG.info("Click on second extensions");
            extensions.get(1).click(new ElementHandle.ClickOptions().setForce(true));

            final ElementHandle extensionsCart = page.waitForSelector(".extensions-cart button");
            LOG.info("Focus on extensions-cart button");
            page.mouse().move(extensionsCart.boundingBox().x + 5, extensionsCart.boundingBox().y +5);

            final List<ElementHandle> selected = page.querySelectorAll(".selected-extensions .extension-row");

            if (selected.size() != 2) {
                LOG.error("{} extensions selected instead of 2", selected.size());
                return 1;
            } else {
                LOG.info("{} extensions selected", selected.size());
            }

            LOG.info("Click on generate button");
            generateButton.click();

            final ElementHandle downloadButton = page.waitForSelector(".download-button");

            LOG.info("Click on download button");
            downloadButton.click();

            LOG.info("ACCEPTANCE TEST PASSED");
        }
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
