package dev.zygon.argus.auth.service.impl;

import dev.zygon.argus.auth.OneTimePassword;
import dev.zygon.argus.auth.repository.ArgusBannedUserRepository;
import dev.zygon.argus.auth.repository.ArgusOneTimePasswordRepository;
import dev.zygon.argus.auth.service.ArgusOneTimePasswordService;
import dev.zygon.argus.namespace.Namespace;
import dev.zygon.argus.user.NamespaceUser;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import static dev.zygon.argus.mutiny.UniExtensions.failIfFalse;
import static dev.zygon.argus.mutiny.UniExtensions.failIfTrue;

@ApplicationScoped
public class ArgusOneTimePasswordRepositoryService implements ArgusOneTimePasswordService {

    private final ArgusBannedUserRepository bannedUsers;
    private final ArgusOneTimePasswordRepository passwords;

    public ArgusOneTimePasswordRepositoryService(ArgusBannedUserRepository bannedUsers,
                                                 ArgusOneTimePasswordRepository passwords) {
        this.bannedUsers = bannedUsers;
        this.passwords = passwords;
    }

    @Override
    public Uni<OneTimePassword> generate(NamespaceUser namespaceUser) {
        var namespace = new Namespace(namespaceUser.namespace());
        var user = namespaceUser.user();
        var password = OneTimePassword.builder()
                .uuid(user.uuid())
                .namespace(namespace)
                .password(passwords.generate())
                .build();
        return bannedUsers.isUserBanned(user)
                .plug(failIfTrue(new IllegalArgumentException("Cannot generate One Time Password as this user is banned from the system.")))
                .replaceWith(passwords.storePassword(password));
    }

    @Override
    public Uni<OneTimePassword> verify(OneTimePassword password) {
        return passwords.verifyPassword(password)
                .plug(failIfFalse(new IllegalArgumentException("Specified One Time Password was not valid.")))
                .replaceWith(passwords.deletePassword(password))
                .plug(failIfFalse(new IllegalStateException("Could not evict validated one-time-password from storage.")))
                .replaceWith(password);
    }
}
