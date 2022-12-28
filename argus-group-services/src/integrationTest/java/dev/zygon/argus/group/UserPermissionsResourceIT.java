package dev.zygon.argus.group;

import dev.zygon.argus.group.helper.DataSetup;
import dev.zygon.argus.group.profile.IntegrationProfile;
import dev.zygon.argus.group.token.TokenGenerator;
import dev.zygon.argus.permission.GroupPermission;
import dev.zygon.argus.permission.GroupPermissions;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.UserPermission;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static dev.zygon.argus.permission.Permission.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class UserPermissionsResourceIT {

    private final DataSetup setup;
    private final TokenGenerator tokens;

    public UserPermissionsResourceIT(DataSetup setup,
                                     TokenGenerator tokens) {
        this.setup = setup;
        this.tokens = tokens;
    }

    private String civmc;
    private String helios;
    private String mta;
    private UUID alice;
    private UUID bob;

    @BeforeEach
    public void setUp() {
        civmc = "civmc";
        helios = "helios";
        mta = "MTA";
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
        setup.createNamespace(civmc);
        setup.createNamespace(helios);
        setup.createGroup(civmc, mta, alice);
    }

    @AfterEach
    public void tearDown() {
        setup.deleteAllPermissions();
        setup.deleteAllAudit();
        setup.deleteAllGroups();
        setup.deleteAllNamespaces();
    }

    @Nested
    public class Elected {

        private static final String URL = "/permissions";

        @Test
        public void whenMemberOfNoGroupsNoElectedRolesAreAvailable() {
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
            assertThat(body)
                    .extracting(GroupPermissions::permissions)
                    .asInstanceOf(COLLECTION)
                    .isEmpty();
        }

        @Test
        public void whenMemberOfGroupInDifferentNamespaceNoElectedRolesAreAvailable() {
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
            assertThat(body)
                    .extracting(GroupPermissions::permissions)
                    .asInstanceOf(COLLECTION)
                    .isEmpty();
        }

        @Test
        public void whenMemberOfGroupElectedRoleIsSupplied() {
            var group = new Group(civmc, mta);
            var permission = new GroupPermission(group, ACCESS);
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
            assertThat(body)
                    .extracting(GroupPermissions::permissions)
                    .asInstanceOf(COLLECTION)
                    .containsExactly(permission);
        }
    }

    @Nested
    public class Elect {

        private static final String URL = "/permissions/{groupName}";
        private static final String GROUP_NAME = "groupName";

        @Test
        public void whenNotMemberOfGroupCannotElect() {
            var permission = new UserPermission(bob, ADMIN);
            var bearer = tokens.bearer(bob, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .body(permission)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, mta);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You cannot elect a permission that you do not have access to."));
        }

        @Test
        public void whenMemberInDifferentNamespaceCannotElect() {
            var permission = new UserPermission(alice, ADMIN);
            var bearer = tokens.bearer(alice, helios);

            var request = given()
                    .contentType(ContentType.JSON)
                    .body(permission)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, mta);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You cannot elect a permission that you do not have access to."));
        }

        @ParameterizedTest
        @EnumSource(
                value = Permission.class,
                names = { "ACCESS", "READ", "WRITE", "READWRITE" }
        )
        public void memberCanElectPermissionUpToReadWrite(Permission target) {
            setup.grantRole(civmc, mta, bob, READWRITE);
            var permission = new UserPermission(bob, target);
            var bearer = tokens.bearer(bob, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .body(permission)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, mta);

            request.put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @ParameterizedTest
        @EnumSource(
                value = Permission.class,
                names = { "ACCESS", "READ", "WRITE", "READWRITE", "ADMIN" }
        )
        public void adminCanElectPermissionUpToAdmin(Permission target) {
            var permission = new UserPermission(alice, target);
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .body(permission)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, mta);

            request.put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }
    }
}
