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
package dev.zygon.argus.client.connector;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.zygon.argus.client.exception.ArgusClientException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;

import java.math.BigInteger;

public enum MojangAuthConnector {

    INSTANCE;

    public String connectMojang() throws ArgusClientException {
        var client = MinecraftClient.getInstance();
        var session = client.getSession();
        var profile = session.getProfile();
        var token = session.getAccessToken();
        var sessionService = client.getSessionService();
        var joinHash = generateHash();
        try {
            sessionService.joinServer(profile, token, joinHash);
            return joinHash;
        } catch (AuthenticationException e) {
            throw new ArgusClientException("Could not connect to Mojang in preparation for authorization.", e);
        }
    }

    private String generateHash() {
        try {
            var keyPair = NetworkEncryptionUtils.generateServerKeyPair();
            var key = NetworkEncryptionUtils.generateKey();
            var publicKey = keyPair.getPublic();
            var digest = NetworkEncryptionUtils
                    .generateServerId("", publicKey, key);
            return new BigInteger(digest).toString(16);
        } catch (NetworkEncryptionException e) {
            throw new IllegalStateException("Could not generate new hash.", e);
        }
    }
}
