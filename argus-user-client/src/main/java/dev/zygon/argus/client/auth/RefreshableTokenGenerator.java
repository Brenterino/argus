package dev.zygon.argus.client.auth;

public interface RefreshableTokenGenerator extends TokenGenerator {

    boolean isExpired();

    void refresh();
}
