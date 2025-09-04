package io.quarkus.code;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitForSelectorState;
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
import java.util.stream.Collectors;

import static io.quarkus.code.SnapshotTesting.assertThatMatchSnapshot;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithPlaywright(verbose = true, slowMo = 150)
@TestProfile(CodeQuarkusPlaywrightTest.PlaywrightTestProfile.class)
public class CodeQuarkusPlaywrightTest {

    public static final String LABEL_TOGGLE_SEARCH_COMBO = "[aria-label='Toggle %s combobox']";
    public static final String LABEL_EDIT_GROUP_ID = "[aria-label='Edit groupId']";
    public static final String LABEL_EDIT_ARTIFACT_ID = "[aria-label='Edit artifactId']";
    public static final String LABEL_EDIT_PROJECT_VERSION = "[aria-label='Edit project version']";
    public static final String LABEL_EDIT_BUILD_TOOL = "[aria-label='Edit build tool']";
    public static final String LABEL_EDIT_JAVA_VERSION = "[aria-label='Edit Java version']";
    public static final String LABEL_SELECTED_EXTENSIONS = "[aria-label='Selected extensions']";
    public static final String LABEL_MORE_OPTIONS_TO_GET_THE_APP = "[aria-label='More options to get the app']";
    public static final String LABEL_SEARCH_EXTENSIONS = "[aria-label='Search extensions']";
    public static final String LABEL_GENERATE_YOUR_APPLICATION = "[aria-label='Generate your application']";
    public static final String LABEL_DOWNLOAD_THE_ZIP = "[aria-label='Download the zip']";
    public static final String LABEL_TOGGLE_PANEL = "[aria-label='Toggle panel']";
    public static final String LABEL_TOGGLE_FULL_LIST = "[aria-label='Toggle full list of extensions']";
    public static final String LABEL_CLEAR_SELECTION = "[aria-label='Clear extension selection']";

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
            checkDownloadLink(testInfo, page);
        }
    }

    @Test
    public void testSearchExtensions(TestInfo testInfo) throws Throwable {
        try (Page page = openIndex()) {

            search(page, "hibernate in name", () -> {
                final List<ElementHandle> names = page.querySelectorAll(".extension-name");
                final List<String> list = names.stream().map(ElementHandle::innerText).toList();
                assertThat(list).isNotEmpty().allMatch(s -> s.toLowerCase().contains("hibernate"));
            });
            search(page, "hibernate panache in name", () -> {
                final List<ElementHandle> names = page.querySelectorAll(".extension-name");
                final List<String> list = names.stream().map(ElementHandle::innerText).toList();
                assertThat(list).isNotEmpty()
                        .allMatch(s -> s.toLowerCase().contains("hibernate") && s.toLowerCase().contains("panache"));
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

            search(page, "status:experimental", () -> {
                final List<ElementHandle> names = page.querySelectorAll(".extension-row");
                assertThat(names.stream()
                        .map(e -> e.querySelectorAll(".status-experimental"))
                        .noneMatch(List::isEmpty)).isTrue();
            });

            search(page, "quarkus-hibernate-orm", () -> {
                final List<ElementHandle> names = page.querySelectorAll(".extension-id");
                final List<String> list = names.stream().map(ElementHandle::innerText).toList();
                assertThat(list).isNotEmpty().allMatch(s -> s.toLowerCase().contains("quarkus-hibernate-orm"));
            });
        }

    }

    @Test
    public void testPresets() {
        final Page page = openIndex();
        page.waitForSelector("[aria-label='Select db-service preset']").click();
        final List<ElementHandle> pickerSelectedExtensions = page.querySelectorAll(".selected-extensions.picker .extension-id");
        assertThat(pickerSelectedExtensions)
                .hasSize(4)
                .extracting(ElementHandle::innerText)
                .map(String::trim)
                .containsExactlyInAnyOrder("[quarkus-rest]", "[quarkus-rest-jackson]", "[quarkus-hibernate-orm-panache]",
                        "[quarkus-jdbc-postgresql]");
        page.onDialog(Dialog::accept);
        page.waitForSelector(LABEL_CLEAR_SELECTION).click();
        page.waitForSelector("[aria-label='Select webapp-npm preset']");
    }

    @Test
    public void testFullList() {
        final Page page = openIndex();
        page.waitForSelector(LABEL_TOGGLE_FULL_LIST).click();
        selectExtension(page, "io.quarkus:quarkus-grpc");
        page.waitForSelector(LABEL_TOGGLE_FULL_LIST).click();
        final List<ElementHandle> pickerSelectedExtensions = page.querySelectorAll(".selected-extensions.picker .extension-id");
        assertThat(pickerSelectedExtensions)
                .hasSize(1)
                .extracting(ElementHandle::innerText)
                .map(String::trim)
                .containsExactlyInAnyOrder("[quarkus-grpc]");
    }

    @Test
    public void testSearchFilters() {
        final Page page = openIndex();

        ElementHandle searchInput = page.waitForSelector(LABEL_SEARCH_EXTENSIONS);
        assertThat(searchInput.inputValue().trim()).isEqualTo("");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("platform")).click();
        page.waitForSelector(".inactive[aria-label='Add platform:yes filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("platform:yes");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("platform")).click();
        page.waitForSelector(".active[aria-label='Remove platform:yes filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("category")).click();
        page.waitForSelector("[aria-label='Add category:web filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("category:web");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("category")).click();
        page.waitForSelector("[aria-label='Remove category:web filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("platform")).click();
        page.waitForSelector(".inactive[aria-label='Add platform:yes filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("platform:yes");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("category")).click();
        page.waitForSelector("[aria-label='Add category:web filter']").click();

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("status")).click();
        page.waitForSelector("[aria-label='Add status:experimental filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("status:experimental category:web platform:yes");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("platform")).click();
        page.waitForSelector(".active[aria-label='Remove platform:yes filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("status:experimental category:web");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("category")).click();
        page.waitForSelector("[aria-label='Add category:data filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("status:experimental category:web,data");

        page.waitForSelector(LABEL_TOGGLE_SEARCH_COMBO.formatted("category")).click();
        page.waitForSelector("[aria-label='Drop category filter']").click();
        assertThat(searchInput.inputValue().trim()).isEqualTo("status:experimental");

        // Test clear search button
        page.waitForSelector("[aria-label='Clear filters']").click();
        page.waitForSelector(".extension-picker-summary");
        assertThat(searchInput.inputValue().trim()).isEqualTo("");

        page.close();
    }

    @Test
    public void testUrlParams() {
        try (Page page = context.newPage()) {
            Response response = page.navigate(index.toString()
                    + "?g=my.app&a=foo&v=2.0.0&b=GRADLE&j=21&e=hibernate-orm&e=grpc&e=io.quarkus:quarkus-hibernate-validator");
            Assertions.assertEquals("OK", response.statusText());
            page.waitForLoadState();
            closeIntroductionModal(page);

            assertThat(page.waitForSelector(LABEL_EDIT_GROUP_ID).inputValue()).isEqualTo("my.app");
            assertThat(page.querySelector(LABEL_EDIT_ARTIFACT_ID).inputValue()).isEqualTo("foo");
            assertThat(page.querySelector(LABEL_EDIT_PROJECT_VERSION).inputValue()).isEqualTo("2.0.0");
            assertThat(page.querySelector(LABEL_EDIT_BUILD_TOOL).inputValue()).isEqualTo("GRADLE");
            assertThat(page.querySelector(LABEL_EDIT_JAVA_VERSION).inputValue()).isEqualTo("21");

            final ElementHandle selectedExtensionsButton = page.waitForSelector(LABEL_SELECTED_EXTENSIONS);
            assertThat(selectedExtensionsButton.innerText()).isEqualTo("3");
            selectedExtensionsButton.hover();
            final List<ElementHandle> cartExtensions = page.querySelectorAll(".selected-extensions.cart .extension-id");
            assertThat(cartExtensions)
                    .hasSize(3)
                    .extracting(ElementHandle::innerText)
                    .map(String::trim)
                    .containsExactlyInAnyOrder("[quarkus-hibernate-orm]", "[quarkus-grpc]", "[quarkus-hibernate-validator]");
            final List<ElementHandle> pickerSelectedExtensions = page
                    .querySelectorAll(".selected-extensions.picker .extension-id");
            assertThat(pickerSelectedExtensions)
                    .hasSize(3)
                    .extracting(ElementHandle::innerText)
                    .map(String::trim)
                    .containsExactlyInAnyOrder("[quarkus-hibernate-orm]", "[quarkus-grpc]", "[quarkus-hibernate-validator]");
        }
    }

    @Test
    void testSaveApp() {
        try (Page page = openIndex()) {
            page.waitForSelector(LABEL_EDIT_GROUP_ID).fill("other.app");
            page.waitForSelector(LABEL_EDIT_ARTIFACT_ID).fill("bar");
            search(page, "grpc", null);
            selectExtension(page, "io.quarkus:quarkus-grpc");
            page.waitForSelector(LABEL_MORE_OPTIONS_TO_GET_THE_APP).hover();
            page.waitForSelector("[aria-label='Store current app as default']").click();
            page.waitForSelector("[aria-label='Restore default app']");
        }
        try (Page page2 = openIndex()) {
            assertThat(page2.waitForSelector(LABEL_EDIT_GROUP_ID).inputValue()).isEqualTo("other.app");
            assertThat(page2.querySelector(LABEL_EDIT_ARTIFACT_ID).inputValue()).isEqualTo("bar");
            assertThat(page2.querySelector(LABEL_EDIT_BUILD_TOOL).inputValue()).isEqualTo("MAVEN");
            final ElementHandle selectedExtensionsButton = page2.waitForSelector(LABEL_SELECTED_EXTENSIONS);
            assertThat(selectedExtensionsButton.innerText()).isEqualTo("1");
            selectedExtensionsButton.hover();
            final List<ElementHandle> extensions = page2.querySelectorAll(".selected-extensions.cart .extension-id");
            assertThat(extensions)
                    .hasSize(1)
                    .extracting(ElementHandle::innerText)
                    .map(String::trim)
                    .containsExactly("[quarkus-grpc]");
            page2.waitForSelector(LABEL_MORE_OPTIONS_TO_GET_THE_APP).hover();
            page2.waitForSelector("[aria-label='Restore default app']").click();
            page2.waitForSelector("[aria-label='Restore default app']", new Page.WaitForSelectorOptions().setState(
                    WaitForSelectorState.HIDDEN));

        }
        try (Page page3 = openIndex()) {
            assertThat(page3.waitForSelector(LABEL_EDIT_GROUP_ID).inputValue()).isEqualTo("org.acme");
            assertThat(page3.querySelector(LABEL_EDIT_ARTIFACT_ID).inputValue()).isEqualTo("code-with-quarkus");
        }
    }

    @Test
    public void testCustomGAVAndExtensions(TestInfo testInfo) throws Throwable {
        try (Page page = openIndex()) {
            // Custom GAV
            ElementHandle groupIdInput = page.waitForSelector(LABEL_EDIT_GROUP_ID);
            groupIdInput.fill("io.test.group");
            ElementHandle artifactIdInput = page.waitForSelector(LABEL_EDIT_ARTIFACT_ID);
            artifactIdInput.fill("custom-test-app");
            page.waitForSelector(LABEL_TOGGLE_PANEL).click();
            ElementHandle versionInput = page.waitForSelector(LABEL_EDIT_PROJECT_VERSION);
            versionInput.fill("1.0.0-TEST");

            // Select extensions
            page.waitForSelector(LABEL_TOGGLE_FULL_LIST).click();
            selectExtension(page, "io.quarkus:quarkus-grpc");
            selectExtension(page, "io.quarkus:quarkus-resteasy");
            selectExtension(page, "io.quarkus:quarkus-resteasy-jackson");

            // Check download link
            checkDownloadLink(testInfo, page);
        }
    }

    private static void selectExtension(Page page, String name) {
        page.waitForSelector("[aria-label='Switch %s extension']".formatted(name)).click();
    }

    private static void search(Page page, String query, Runnable runnable) {
        ElementHandle searchInput = page.waitForSelector(LABEL_SEARCH_EXTENSIONS);
        searchInput.fill("");
        page.waitForSelector(".extension-picker-summary");
        searchInput.fill(query);
        page.waitForSelector(".search-results-info");
        if (runnable != null) {
            runnable.run();
        }
    }

    private static void checkDownloadLink(TestInfo testInfo, Page page) throws Throwable {
        // Click on generate button
        page.waitForSelector(LABEL_GENERATE_YOUR_APPLICATION).click();

        // Find download link and check href attribute
        ElementHandle downloadLink = page.waitForSelector(LABEL_DOWNLOAD_THE_ZIP);
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
        page.waitForSelector("[aria-label='Close the introduction modal']").click();
    }

    public static final class PlaywrightTestProfile implements QuarkusTestProfile {

        @Override
        public String getConfigProfile() {
            return QuarkusTestProfile.super.getConfigProfile() + ",playwright";
        }

        public PlaywrightTestProfile() {
        }
    }
}
