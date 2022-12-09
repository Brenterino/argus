package dev.zygon.argus.location;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class LocationsSocketNoScaleIT extends LocationsSocketIT {
}
