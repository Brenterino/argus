/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.group;

import dev.zygon.argus.group.helper.DataSetup;
import dev.zygon.argus.group.profile.IntegrationProfile;
import dev.zygon.argus.namespace.Namespace;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.OK;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class NamespaceResourceIT {

    private static final String URL = "/groups/namespaces";
    private static final String MAPPING_NAME = "mapping";

    private final DataSetup setup;

    public NamespaceResourceIT(DataSetup setup) {
        this.setup = setup;
    }

    private String civmc;
    private String helios;

    @BeforeEach
    public void setUp() {
        civmc = "civmc";
        helios = "helios";
        setup.createNamespace(civmc);
        setup.createNamespace(helios);
    }

    @AfterEach
    public void tearDown() {
        setup.deleteAllPermissions();
        setup.deleteAllAudit();
        setup.deleteAllGroups();
        setup.deleteAllNamespaceMappings();
        setup.deleteAllNamespaces();
    }

    @Nested
    public class Mapping {

        private static final String CIVMC_MAPPING_ONE = "play.civmc.net";
        private static final String CIVMC_MAPPING_TWO = "15.204.132.210";
        private static final String HELIOS_MAPPING_ONE = "play.heliospvp.com";

        @Test
        public void whenNoMappingExistsNoResultsAreFound() {
            var request = given()
                    .contentType(ContentType.JSON)
                    .queryParam(MAPPING_NAME, CIVMC_MAPPING_ONE);

            request.get(URL)
                    .then()
                    .statusCode(NOT_FOUND)
                    .body(is("Namespace mapping was not found."));
        }

        @Test
        public void whenMappingExistsResultIsFound() {
            setup.createNamespaceMapping(civmc, CIVMC_MAPPING_ONE);

            var request = given()
                    .contentType(ContentType.JSON)
                    .queryParam(MAPPING_NAME, CIVMC_MAPPING_ONE);

            var response = request.get(URL);

            var result = response.body()
                    .as(Namespace.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(OK);

            assertThat(result.name())
                    .isEqualTo(civmc);
        }

        @Test
        public void canFindNamespaceCorrectlyWhenMultipleMappingsExist() {
            setup.createNamespaceMapping(civmc, CIVMC_MAPPING_TWO);
            setup.createNamespaceMapping(civmc, CIVMC_MAPPING_ONE);

            var request = given()
                    .contentType(ContentType.JSON)
                    .queryParam(MAPPING_NAME, CIVMC_MAPPING_TWO);

            var response = request.get(URL);

            var result = response.body()
                    .as(Namespace.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(OK);

            assertThat(result.name())
                    .isEqualTo(civmc);
        }

        @Test
        public void canFindNamespaceCorrectlyWithMultipleNamespaces() {
            setup.createNamespaceMapping(civmc, CIVMC_MAPPING_ONE);
            setup.createNamespaceMapping(helios, HELIOS_MAPPING_ONE);

            var request = given()
                    .contentType(ContentType.JSON)
                    .queryParam(MAPPING_NAME, HELIOS_MAPPING_ONE);

            var response = request.get(URL);

            var result = response.body()
                    .as(Namespace.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(OK);

            assertThat(result.name())
                    .isEqualTo(helios);
        }
    }
}
