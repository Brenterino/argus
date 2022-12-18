package dev.zygon.argus.location;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Disabled;

@Disabled("Should only be ran manually to check how load is handled.")
@QuarkusTest
@TestProfile(ScaleIntegrationProfile.class)
public class LocationsSocketLoadScaleIT extends LocationsSocketLoadIT {
}
