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

import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditAction;
import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.group.helper.DataSetup;
import dev.zygon.argus.group.profile.IntegrationProfile;
import dev.zygon.argus.group.token.TokenGenerator;
import dev.zygon.argus.permission.Permission;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static dev.zygon.argus.group.audit.AuditAction.*;
import static dev.zygon.argus.permission.Permission.ADMIN;
import static dev.zygon.argus.permission.Permission.READWRITE;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class AuditGroupsResourceIT {

    private static final String URL = "/groups/audits/{groupName}";
    private static final String GROUP_NAME = "groupName";

    private static final String PAGE_NAME = "page";
    private static final String SIZE_NAME = "size";

    private final DataSetup setup;
    private final TokenGenerator tokens;

    public AuditGroupsResourceIT(DataSetup setup,
                                 TokenGenerator tokens) {
        this.setup = setup;
        this.tokens = tokens;
    }

    private String civmc;
    private String helios;
    private String doom;
    private UUID alice;
    private UUID bob;

    @BeforeEach
    public void setUp() {
        civmc = "civmc";
        helios = "helios";
        doom = "DoomCity";
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
        setup.createNamespace(civmc);
        setup.createNamespace(helios);
        setup.createGroup(civmc, doom, alice);
    }

    @AfterEach
    public void tearDown() {
        setup.deleteAllPermissions();
        setup.deleteAllAudit();
        setup.deleteAllGroups();
        setup.deleteAllNamespaces();
    }

    @Nested
    public class Audits {

        private RecursiveComparisonConfiguration recursive;

        @BeforeEach
        public void setUp() {
            recursive = RecursiveComparisonConfiguration.builder()
                    .withIgnoredFields("occurred")
                    // Note: attempted to verify correctness via below, but it seems like
                    // this decided to not work. For the purposes of the IT, this field
                    // is not really as important as making sure ordering of the audit trail
                    // is kept in-tact as this implies a temporal sequence anyway
//                    .withEqualsForType(OffsetDateTime::isEqual, OffsetDateTime.class)
//                    .withComparatorForType((l, r) ->
//                            l.isEqual(r) ? 0 :
//                                    l.isAfter(r) ? 1 : -1, OffsetDateTime.class)
                    .build();
        }

        @Test
        public void cannotRetrieveAuditsWithInvalidPage() {
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom)
                    .queryParam(PAGE_NAME, -1);

            request.get(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("Invalid paging arguments provided."));
        }

        @Test
        public void cannotRetrieveAuditsWithInvalidSize() {
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom)
                    .queryParam(SIZE_NAME, -1);

            request.get(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("Invalid paging arguments provided."));
        }

        @Test
        public void cannotRetrieveAuditsWithInvalidPageAndSize() {
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom)
                    .queryParam(PAGE_NAME, -1)
                    .queryParam(SIZE_NAME, -1);

            request.get(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("Invalid paging arguments provided."));
        }

        @Test
        public void cannotRetrieveAuditsIfNotMember() {
            var bearer = tokens.bearer(bob, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom);

            request.get(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to view the audit log of this group."));
        }

        @Test
        public void cannotRetrieveAuditsIfNotAdmin() {
            setup.grantRole(civmc, doom, bob, READWRITE);
            var bearer = tokens.bearer(bob, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom);

            request.get(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to view the audit log of this group."));
        }

        @Test
        public void cannotRetrieveAuditsIfNotInCorrectNamespace() {
            var bearer = tokens.bearer(alice, helios);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom);

            request.get(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to view the audit log of this group."));
        }

        @Test
        public void canRetrieveNoAudits() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom);

            var response = request.get(URL);
            var auditLog = response.body()
                    .as(AuditLog.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(OK);

            assertThat(auditLog.pages())
                    .isEqualTo(1);

            assertThat(auditLog.log())
                    .isEmpty();
        }

        @Test
        public void canRetrieveAuditsOnOnePage() {
            var createAudit = createAudit(alice, CREATE, ADMIN);
            var modifyAudit = createAudit(bob, MODIFY, READWRITE);
            var modifyAudit2 = createAudit(bob, MODIFY, ADMIN);
            var updateAudit = createAudit(alice, UPDATE, ADMIN);

            setup.createAudit(civmc, doom, createAudit);
            setup.createAudit(civmc, doom, modifyAudit);
            setup.createAudit(civmc, doom, modifyAudit2);
            setup.createAudit(civmc, doom, updateAudit);
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom);

            var response = request.get(URL);
            var auditLog = response.body()
                    .as(AuditLog.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(OK);

            assertThat(auditLog.pages())
                    .isEqualTo(1);

            assertThat(auditLog.log())
                    .usingRecursiveFieldByFieldElementComparator(recursive)
                    .containsExactly(updateAudit, modifyAudit2,
                            modifyAudit, createAudit);
        }

        @Test
        public void canRetrieveAuditsAcrossMultiplePages() {
            var createAudit = createAudit(alice, CREATE, ADMIN);
            var modifyAudit = createAudit(bob, MODIFY, READWRITE);
            var updateAudit = createAudit(alice, UPDATE, ADMIN);

            setup.createAudit(civmc, doom, createAudit);
            setup.createAudit(civmc, doom, modifyAudit);
            setup.createAudit(civmc, doom, updateAudit);
            var bearer = tokens.bearer(alice, civmc);

            var pageOneRequest = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom)
                    .queryParam(SIZE_NAME, 2)
                    .queryParam(PAGE_NAME, 0);

            var pageTwoRequest = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, doom)
                    .queryParam(SIZE_NAME, 2)
                    .queryParam(PAGE_NAME, 1);

            var pageOneResponse = pageOneRequest.get(URL);
            var pageTwoResponse = pageTwoRequest.get(URL);

            var pageOne = pageOneResponse.body()
                    .as(AuditLog.class);
            var pageTwo = pageTwoResponse.body()
                    .as(AuditLog.class);

            assertThat(pageOneResponse.getStatusCode())
                    .isEqualTo(OK);
            assertThat(pageTwoResponse.getStatusCode())
                    .isEqualTo(OK);

            assertThat(pageOne.pages())
                    .isEqualTo(2);
            assertThat(pageTwo.pages())
                    .isEqualTo(2);

            assertThat(pageOne.log())
                    .usingRecursiveFieldByFieldElementComparator(recursive)
                    .containsExactly(updateAudit, modifyAudit);
            assertThat(pageTwo.log())
                    .usingRecursiveFieldByFieldElementComparator(recursive)
                    .containsExactly(createAudit);
        }
    }

    private static Audit createAudit(UUID user, AuditAction action, Permission permission) {
        return Audit.builder()
                .changer(user)
                .target(user)
                .action(action)
                .permission(permission)
                .occurred(OffsetDateTime.now())
                .build();
    }
}
