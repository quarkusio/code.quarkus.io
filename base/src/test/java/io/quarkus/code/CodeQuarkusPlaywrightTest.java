package io.quarkus.code;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.code.model.ExtensionRef;
import io.quarkus.code.service.PlatformService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.net.URL;
import java.util.List;

import static io.quarkus.code.SnapshotTesting.assertThatMatchSnapshot;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithPlaywright(verbose = true)
@TestProfile(CodeQuarkusPlaywrightTest.PlaywrightTestProfile.class)
public class CodeQuarkusPlaywrightTest {

    @Inject PlatformService platformService;

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

    @Test
    public void testGenerateDefault(TestInfo testInfo) throws Throwable {
        final Page page = openIndex();
        checkDownloadLink(testInfo, page);
    }

    @Test
    public void testSearchExtensions(TestInfo testInfo) throws Throwable {
        final Page page = openIndex();

        search(page, "hibernate in name", () -> {
            final List<ElementHandle> names = page.querySelectorAll(".extension-name");
            final List<String> list = names.stream().map(ElementHandle::innerText).toList();
            assertThat(list).isNotEmpty().allMatch(s -> s.toLowerCase().contains("hibernate"));
        });
        search(page, "hibernate panache in name", () -> {
            final List<ElementHandle> names = page.querySelectorAll(".extension-name");
            final List<String> list = names.stream().map(ElementHandle::innerText).toList();
            assertThat(list).isNotEmpty().allMatch(s -> s.toLowerCase().contains("hibernate") && s.toLowerCase().contains("panache"));
        });

        search(page, "name:\"Hibernate ORM with Panache\"", () -> {
            final List<ElementHandle> names = page.querySelectorAll(".extension-name");
            final List<String> list = names.stream().map(ElementHandle::innerText).toList();
            assertThat(list).isNotEmpty().allMatch(s -> s.equals("Hibernate ORM with Panache"));
        });

        search(page, "cat:cloud", () -> {
            final List<ElementHandle> names = page.querySelectorAll(".extension-row");
            final List<ExtensionRef> refs = names.stream()
                    .map(e -> e.getAttribute("aria-label"))
                    .map(id -> platformService.recommendedPlatformInfo().extensionsById().get(id))
                    .toList();
            refs.forEach(ref -> {
                assertThat(platformService.recommendedPlatformInfo().codeQuarkusExtensions()).anyMatch(
                        e -> e.id().equals(ref.id()) && e.category().equalsIgnoreCase("cloud"));
            });
        });

    }

    private static void search(Page page, String query, Runnable runnable) {
        ElementHandle searchInput = page.waitForSelector("[aria-label='Search extensions']");
        searchInput.fill("");
        page.waitForSelector(".extension-category");
        searchInput.fill(query);
        page.waitForSelector(".search-results-info");
        runnable.run();
    }

    private static void checkDownloadLink(TestInfo testInfo, Page page) throws Throwable {
        // Click on generate button
        ElementHandle generateBtn = page.waitForSelector("[aria-label='Generate your application']");
        generateBtn.click();

        // Find download link and check href attribute
        ElementHandle downloadLink = page.waitForSelector("[aria-label='Download the zip']");
        String href = downloadLink.getAttribute("href");

        assertThatMatchSnapshot(href, testInfo, "downloadLink");
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
        ElementHandle closeModal = page.waitForSelector("[aria-label='Close the introduction modal']");
        closeModal.click();
    }

    public static final class PlaywrightTestProfile implements QuarkusTestProfile {

        @Override public String getConfigProfile() {
            return QuarkusTestProfile.super.getConfigProfile() + ",playwright";
        }

        public PlaywrightTestProfile() {
        }
    }
}
