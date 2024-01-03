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

import dev.zygon.argus.client.config.ArgusClientConfig;
import okhttp3.internal.Util;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public enum ArgusTrustManager implements X509TrustManager {

    INSTANCE;

    private final X509TrustManager delegate;

    ArgusTrustManager() {
        this.delegate = Util.platformTrustManager();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isVerifyCertificateEnabled()) {
            delegate.checkClientTrusted(chain, authType);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isVerifyCertificateEnabled()) {
            delegate.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isVerifyCertificateEnabled()) {
            return delegate.getAcceptedIssuers();
        } else {
            return new X509Certificate[0];
        }
    }
}
