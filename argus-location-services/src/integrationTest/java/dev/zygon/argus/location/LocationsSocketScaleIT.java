package dev.zygon.argus.location;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(ScaleIntegrationProfile.class)
public class LocationsSocketScaleIT extends LocationsSocketIT {
}
