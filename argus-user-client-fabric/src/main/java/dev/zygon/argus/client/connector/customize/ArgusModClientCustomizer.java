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
package dev.zygon.argus.client.connector.customize;

import dev.zygon.argus.client.ArgusClientCustomizer;
import dev.zygon.argus.client.auth.TokenGenerator;
import lombok.SneakyThrows;
import okhttp3.internal.platform.Platform;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;

public enum ArgusModClientCustomizer implements ArgusClientCustomizer {

    INSTANCE;

    @Override
    public TokenGenerator tokenGenerator() {
        return ArgusMojangTokenGenerator.INSTANCE;
    }

    @Override
    public HostnameVerifier hostnameVerifier() {
        return ArgusHostnameVerifier.INSTANCE;
    }

    @Override
    @SneakyThrows
    public SSLSocketFactory sslSocketFactory() {
        var context = Platform.get().getSSLContext();
        context.init(null, new TrustManager[] { trustManager() }, new SecureRandom());
        return context.getSocketFactory();
    }

    @Override
    public X509TrustManager trustManager() {
        return ArgusTrustManager.INSTANCE;
    }
}
