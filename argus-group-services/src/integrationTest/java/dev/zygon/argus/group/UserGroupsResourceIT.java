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
import dev.zygon.argus.group.token.TokenGenerator;
import dev.zygon.argus.permission.GroupPermission;
import dev.zygon.argus.permission.GroupPermissions;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static dev.zygon.argus.permission.Permission.ADMIN;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class UserGroupsResourceIT {

    private final DataSetup setup;
    private final TokenGenerator tokens;

    public UserGroupsResourceIT(DataSetup setup,
                                TokenGenerator tokens) {
        this.setup = setup;
        this.tokens = tokens;
    }

    private String civmc;
    private String helios;
    private String volterra;
    private UUID alice;
    private UUID bob;

    @BeforeEach
    public void setUp() {
        civmc = "civmc";
        helios = "helios";
        volterra = "Volterra";
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
        setup.createNamespace(civmc);
        setup.createNamespace(helios);
        setup.createGroup(civmc, volterra, alice);
    }

    @AfterEach
    public void tearDown() {
        setup.deleteAllPermissions();
        setup.deleteAllAudit();
        setup.deleteAllGroups();
        setup.deleteAllNamespaces();
    }

    @Nested
    public class Membership {

        private static final String URL = "/groups";

        @Test
        public void whenUserIsNotInAnyGroupThenWillSeeNoGroups() {
            var bearer = tokens.bearer(bob, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer);

            var response = request.get(URL);

            var status = response.statusCode();
            var body = response.body()
                    .as(GroupPermissions.class);

            assertThat(status)
                    .isEqualTo(OK);
            assertThat(body.permissions())
                    .isEmpty();
        }

        @Test
        public void whenUserIsAccessingDifferentNamespaceWillNotSeeGroupsForOtherOnes() {
            var bearer = tokens.bearer(alice, helios);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer);

            var response = request.get(URL);

            var status = response.statusCode();
            var body = response.body()
                    .as(GroupPermissions.class);

            assertThat(status)
                    .isEqualTo(OK);
            assertThat(body.permissions())
                    .isEmpty();
        }

        @Test
        public void whenUserIsInGroupWillSeeGroupMembership() {
            var group = new Group(civmc, volterra);
            var permission = new GroupPermission(group, ADMIN);
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer);

            var response = request.get(URL);

            var status = response.statusCode();
            var body = response.body()
                    .as(GroupPermissions.class);

            assertThat(status)
                    .isEqualTo(OK);
            assertThat(body.permissions())
                    .containsExactly(permission);
        }
    }

    @Nested
    public class Leave {

        private static final String URL = "/groups/{groupName}";
        private static final String GROUP_NAME = "groupName";

        @Test
        public void whenUserIsNotInGroupCannotLeaveGroup() {
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, volterra);

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You are not a member of this group."));
        }

        @Test
        public void whenUserIsOwnerOfGroupCannotLeaveGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, volterra);

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You cannot leave a group you are the owner of."));
        }

        @Test
        public void canLeaveGroupIfMemberAndNotOwner() {
            setup.grantRole(civmc, volterra, bob, ADMIN);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, volterra);

            request.delete(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }
    }
}
