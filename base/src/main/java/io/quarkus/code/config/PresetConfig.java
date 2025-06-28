package io.quarkus.code.config;

import java.util.List;

public interface PresetConfig {

    String key();

    String title();

    String icon();

    List<String> extensions();
}
