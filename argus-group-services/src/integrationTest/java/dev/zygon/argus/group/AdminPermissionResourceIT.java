package dev.zygon.argus.group;

import dev.zygon.argus.group.helper.DataSetup;
import dev.zygon.argus.group.profile.IntegrationProfile;
import dev.zygon.argus.group.token.TokenGenerator;
import dev.zygon.argus.permission.UserPermission;
import dev.zygon.argus.permission.UserPermissions;
import dev.zygon.argus.user.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static dev.zygon.argus.permission.Permission.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;

@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class AdminPermissionResourceIT {

    private static final String URL = "/groups/permissions/{groupName}/admin";
    private static final String GROUP_NAME = "groupName";

    private static final String PAGE_NAME = "page";
    private static final String SIZE_NAME = "size";

    private final DataSetup setup;
    private final TokenGenerator tokens;

    public AdminPermissionResourceIT(DataSetup setup,
                                     TokenGenerator tokens) {
        this.setup = setup;
        this.tokens = tokens;
    }

    private String civmc;
    private String helios;
    private String pavia;
    private UUID alice;
    private UUID bob;
    private UUID james;

    @BeforeEach
    public void setUp() {
        civmc = "civmc";
        helios = "helios";
        pavia = "Pavia";
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
        james = UUID.randomUUID();
        setup.createNamespace(civmc);
        setup.createNamespace(helios);
        setup.createGroup(civmc, pavia, alice);
    }

    @AfterEach
    public void tearDown() {
        setup.deleteAllPermissions();
        setup.deleteAllAudit();
        setup.deleteAllGroups();
        setup.deleteAllNamespaces();
    }

    @Nested
    public class Members {

        @Test
        public void cannotRetrieveAuditsWithInvalidPage() {
            var bearer = tokens.bearer(alice, civmc);

            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia)
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
                    .pathParam(GROUP_NAME, pavia)
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
                    .pathParam(GROUP_NAME, pavia)
                    .queryParam(PAGE_NAME, -1)
                    .queryParam(SIZE_NAME, -1);

            request.get(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("Invalid paging arguments provided."));
        }

        @Test
        public void cannotViewMembersToGroupThatDoesNotExist() {
            var bearer = tokens.bearer(alice, helios);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.get(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to view members of this group."));
        }

        @Test
        public void cannotViewMembersWithoutBeingMember() {
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.get(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to view members of this group."));
        }

        @Test
        public void cannotViewMembersWithoutBeingAdmin() {
            setup.grantRole(civmc, pavia, bob, READWRITE);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.get(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to view members of this group."));
        }

        @Test
        public void canViewMembersAsAdmin() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            var permission = new UserPermission(alice, ADMIN);
            var bobPermission = new UserPermission(bob, ADMIN);

            var response = request.get(URL);
            var code = response.statusCode();
            var body = response.body()
                    .as(UserPermissions.class);

            assertThat(code)
                    .isEqualTo(OK);
            assertThat(body.permissions())
                    .containsExactlyInAnyOrder(permission, bobPermission);
        }

        @Test
        public void canViewMembersAsOwner() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            var permission = new UserPermission(alice, ADMIN);

            var response = request.get(URL);
            var code = response.statusCode();
            var body = response.body()
                    .as(UserPermissions.class);

            assertThat(code)
                    .isEqualTo(OK);
            assertThat(body.permissions())
                    .containsExactly(permission);
        }


        @Test
        public void canRetrieveAuditsAcrossMultiplePages() {
            var harvey = UUID.randomUUID();
            var dominic = UUID.randomUUID();

            setup.grantRole(civmc, pavia, bob, READWRITE);
            setup.grantRole(civmc, pavia, james, READWRITE);
            setup.grantRole(civmc, pavia, harvey, ADMIN);
            setup.grantRole(civmc, pavia, dominic, READ);

            var alicePermission = new UserPermission(alice, ADMIN);
            var bobPermission = new UserPermission(bob, READWRITE);
            var jamesPermission = new UserPermission(james, READWRITE);
            var harveyPermission = new UserPermission(harvey, ADMIN);
            var dominicPermission = new UserPermission(dominic, READ);

            var bearer = tokens.bearer(alice, civmc);

            var pageOneRequest = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia)
                    .queryParam(SIZE_NAME, 2)
                    .queryParam(PAGE_NAME, 0);

            var pageTwoRequest = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia)
                    .queryParam(SIZE_NAME, 2)
                    .queryParam(PAGE_NAME, 1);

            var pageThreeRequest = given()
                    .contentType(ContentType.JSON)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia)
                    .queryParam(SIZE_NAME, 2)
                    .queryParam(PAGE_NAME, 2);

            var pageOneResponse = pageOneRequest.get(URL);
            var pageTwoResponse = pageTwoRequest.get(URL);
            var pageThreeResponse = pageThreeRequest.get(URL);

            var pageOne = pageOneResponse.body()
                    .as(UserPermissions.class);
            var pageTwo = pageTwoResponse.body()
                    .as(UserPermissions.class);
            var pageThree = pageThreeResponse.body()
                    .as(UserPermissions.class);

            assertThat(pageOneResponse.getStatusCode())
                    .isEqualTo(OK);
            assertThat(pageTwoResponse.getStatusCode())
                    .isEqualTo(OK);
            assertThat(pageThreeResponse.getStatusCode())
                    .isEqualTo(OK);

            assertThat(pageOne.pages())
                    .isEqualTo(3);
            assertThat(pageTwo.pages())
                    .isEqualTo(3);
            assertThat(pageThree.pages())
                    .isEqualTo(3);

            assertThat(pageOne.permissions())
                    .containsExactly(alicePermission, harveyPermission);
            assertThat(pageTwo.permissions())
                    .containsExactly(bobPermission, jamesPermission);
            assertThat(pageThree.permissions())
                    .containsExactly(dominicPermission);
        }
    }

    @Nested
    public class Invite {

        @Test
        public void cannotInviteSelfToGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(alice, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("You cannot invite yourself to a group."));
        }

        @Test
        public void cannotInviteMembersToNonExistentGroup() {
            var bearer = tokens.bearer(alice, helios);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(bob, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to invite members to this group."));
        }

        @Test
        public void cannotInviteMembersIfNotMember() {
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to invite members to this group."));
        }

        @Test
        public void cannotInviteMembersIfNotAdmin() {
            setup.grantRole(civmc, pavia, bob, READWRITE);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to invite members to this group."));
        }

        @Test
        public void cannotInviteUserThatIsAlreadyAMember() {
            setup.grantRole(civmc, pavia, bob, READWRITE);
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(bob, READ))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(CONFLICT)
                    .body(is("This user is already a member of this group."));
        }

        @Test
        public void cannotInviteUserAsAdminRoleIfNotGroupOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("Only the owner of this group can invite users as admin."));
        }

        @Test
        public void canInviteUserToBeNonAdminIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, READWRITE))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));
        }

        @Test
        public void canInviteUserToBeAdminIfOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.post(URL)
                    .then()
                    .statusCode(CREATED)
                    .body(is(""));
        }
    }

    @Nested
    public class Modify {

        @Test
        public void cannotModifyOwnPermissions() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(alice, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("You cannot modify your own permissions."));
        }

        @Test
        public void cannotModifyMembersOfNonExistentGroup() {
            var bearer = tokens.bearer(alice, helios);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(bob, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to modify members of this group."));
        }

        @Test
        public void cannotModifyMembersIfRequesterIsNotGroupMember() {
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to modify members of this group."));
        }

        @Test
        public void cannotModifyMembersIfNotAdmin() {
            setup.grantRole(civmc, pavia, bob, READWRITE);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to modify members of this group."));
        }

        @Test
        public void cannotModifyUserIfTargetIsNotMember() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(bob, READ))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(NOT_FOUND)
                    .body(is("The user being modified is not a member of this group."));
        }

        @Test
        public void cannotDemoteUsersFromAdminsIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(alice, READWRITE))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("Only the owner of this group can promote or demote admins."));
        }

        @Test
        public void cannotPromoteUsersToAdminIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            setup.grantRole(civmc, pavia, james, READWRITE);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("Only the owner of this group can promote or demote admins."));
        }

        @Test
        public void canPromoteUsersWithinNonAdminIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            setup.grantRole(civmc, pavia, james, READ);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, READWRITE))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @Test
        public void canDemoteUsersWithinNonAdminIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            setup.grantRole(civmc, pavia, james, READWRITE);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(james, READ))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @Test
        public void canPromoteUsersToAdminIfOwner() {
            setup.grantRole(civmc, pavia, bob, READWRITE);
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(bob, ADMIN))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @Test
        public void canDemoteUsersFromAdminIfOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(new UserPermission(bob, READWRITE))
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.put(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }
    }

    @Nested
    public class Kick {

        private User aliceUser;
        private User bobUser;
        private User jamesUser;

        @BeforeEach
        public void setUp() {
            aliceUser = new User(alice, "Alice");
            bobUser = new User(bob, "Bob");
            jamesUser = new User(james, "James");
        }

        @Test
        public void cannotKickSelfFromGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(aliceUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(BAD_REQUEST)
                    .body(is("You cannot kick yourself from a group."));
        }

        @Test
        public void cannotKickMembersOfNonExistentGroup() {
            var bearer = tokens.bearer(alice, helios);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(bobUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to kick members from this group."));
        }

        @Test
        public void cannotKickMembersIfRequesterIsNotGroupMember() {
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(jamesUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to kick members from this group."));
        }

        @Test
        public void cannotKickMembersIfNotAdmin() {
            setup.grantRole(civmc, pavia, bob, READWRITE);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(jamesUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("You do not have permissions to kick members from this group."));
        }

        @Test
        public void cannotKickUserThatIsNotMemberOfGroup() {
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(bobUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(NOT_FOUND)
                    .body(is("The user being kicked is not a member of this group."));
        }

        @Test
        public void cannotKickAdminsIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            setup.grantRole(civmc, pavia, james, ADMIN);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(jamesUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(FORBIDDEN)
                    .body(is("Only the owner of this group can kick admins."));
        }

        @Test
        public void canKickUsersWithinNonAdminIfNotOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            setup.grantRole(civmc, pavia, james, READ);
            var bearer = tokens.bearer(bob, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(jamesUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }

        @Test
        public void canKickAdminsIfOwner() {
            setup.grantRole(civmc, pavia, bob, ADMIN);
            var bearer = tokens.bearer(alice, civmc);
            var request = given()
                    .contentType(ContentType.JSON)
                    .body(bobUser)
                    .header(AUTHORIZATION, bearer)
                    .pathParam(GROUP_NAME, pavia);

            request.delete(URL)
                    .then()
                    .statusCode(NO_CONTENT)
                    .body(is(""));
        }
    }
}
