package io.quarkus.code.testing;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class AcceptanceTestAppTest {

    @Test
    @Launch(value = {}, exitCode = 0)
    public void testLaunchCommand(LaunchResult result) {
    }

}