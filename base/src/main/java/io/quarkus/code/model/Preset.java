package io.quarkus.code.model;

import java.util.List;

public record Preset(String key, String title, String icon, List<String> extensions) {
}
