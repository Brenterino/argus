package dev.zygon.argus.group;

import dev.zygon.argus.group.profile.IntegrationProfile;
import dev.zygon.argus.group.helper.DataSetup;
import dev.zygon.argus.group.token.TokenGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class AdminGroupsResourceIT {

    private static final String URL = "/groups/{groupName}/admin";
    private static final String GROUP_NAME = "groupName";

    private final DataSetup setup;
    private final TokenGenerator tokens;

    public AdminGroupsResourceIT(DataSetup setup,
                                 TokenGenerator tokens) {
        this.setup = setup;
        this.tokens = tokens;
    }

    private String civmc;
    private String helios;
    private String estalia;
    private String butternut;
    private UUID alice;
    private UUID bob;

    @BeforeEach
    public void setUp() {
        civmc = "civmc";
        helios = "helios";
        estalia = "Estalia";
        butternut = "Butternut";
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
        setup.createNamespace(civmc);
        setup.createNamespace(helios);
    }

    @AfterEach
    public void tearDown() {
        setup.deleteAllPermissions();
        setup.deleteAllAudit();
        setup.deleteAllGroups();
        setup.deleteAllNamespaces();
    }

    @Nested
    public class Create {

        @Test
        public void canCreateGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia);

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));
        }

        @Test
        public void cannotCreateGroupWithMalformedNamed() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, "_!_~)~)FOOOFO");

            request.post(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("Group name contains invalid characters."));
        }

        @Test
        public void cannotCreateGroupWithMalformedMetadata() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia);

            request.body("{")
                    .post(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is(""));
        }

        @Test
        public void canCreateGroupWithMetadata() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia);

            request.body(Map.of("test", "value"))
                    .post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));
        }

        @Test
        public void canCreateGroupWithSameNameInDifferentNamespaces() {
            var bearer = tokens.bearer(alice, civmc);
            var bearerTwo = tokens.bearer(alice, helios);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia);
            var requestTwo = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearerTwo)
                    .pathParam(GROUP_NAME, estalia);

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            requestTwo.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));
        }

        @Test
        public void cannotCreateSameGroupTwice() {
            var bearer = tokens.bearer(alice, civmc);
            var bearerTwo = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia);
            var requestTwo = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearerTwo)
                    .pathParam(GROUP_NAME, estalia);

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            requestTwo.post(URL)
                    .then()
                    .statusCode(CONFLICT)
                    .body(is("This group already exists."));
        }

        @Test
        public void cannotCreateMoreGroupsInOneNamespaceThanConfigured() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia);
            var requestTwo = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, butternut);

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            requestTwo.post(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You are not allowed to create any more groups."));
        }
    }

    @Nested
    public class Update {

        @Test
        public void canUpdateGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            request.body(Map.of("test", "entry"))
                    .put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @Test
        public void cannotUpdateGroupWithMalformedMetadata() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            request.body("{")
                    .put(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is(""));
        }

        @Test
        public void cannotUpdateGroupNotAdminOf() {
            var bearer = tokens.bearer(alice, civmc);
            var bearerTwo = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();
            var requestTwo = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearerTwo)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            requestTwo.body(Map.of("test", "entry"))
                    .put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to modify this group."));
        }
    }

    @Nested
    public class Delete {

        @Test
        public void canDeleteGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            request.delete(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @Test
        public void cannotDeleteNonExistingGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to delete this group."));
        }

        @Test
        public void cannotDeleteGroupNotOwnerOf() {
            var bearer = tokens.bearer(alice, civmc);
            var bearerTwo = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();
            var requestTwo = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearerTwo)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            requestTwo.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to delete this group."));
        }

        @Test
        public void cannotDeleteGroupInDifferentNamespaceEvenIfOwner() {
            var bearer = tokens.bearer(alice, civmc);
            var bearerTwo = tokens.bearer(alice, helios);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, estalia)
                    .with();
            var requestTwo = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearerTwo)
                    .pathParam(GROUP_NAME, estalia)
                    .with();

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));

            requestTwo.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to delete this group."));
        }
    }
}
