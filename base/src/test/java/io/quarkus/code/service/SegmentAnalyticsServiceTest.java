package io.quarkus.code.service;

import static io.quarkus.code.service.SegmentAnalyticsService.USER_AGENT_PATTERN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SegmentAnalyticsServiceTest {
    void testUserAgentPattern() {
        assertTrue(USER_AGENT_PATTERN.matcher(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .matches());
        assertTrue(USER_AGENT_PATTERN.matcher(
                "Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 YaBrowser/17.6.1.749 Yowser/2.5 Safari/537.36")
                .matches());
        assertTrue(USER_AGENT_PATTERN.matcher(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148")
                .matches());
        assertTrue(USER_AGENT_PATTERN.matcher("Outlook-iOS/709.2189947.prod.iphone (3.24.0)").matches());
        assertFalse(USER_AGENT_PATTERN.matcher("Java/1.8").matches());
    }
}