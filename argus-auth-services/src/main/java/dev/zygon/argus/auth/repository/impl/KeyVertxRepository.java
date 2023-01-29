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
package dev.zygon.argus.auth.repository.impl;

import dev.zygon.argus.auth.configuration.AuthConfiguration;
import dev.zygon.argus.auth.repository.KeyRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.file.AsyncFile;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeyVertxRepository implements KeyRepository {

    private final Vertx vertx;
    private final AuthConfiguration configuration;

    public KeyVertxRepository(Vertx vertx,
                              AuthConfiguration configuration) {
        this.vertx = vertx;
        this.configuration = configuration;
    }

    @Override
    public Uni<AsyncFile> publicKey() {
        var openOptions = new OpenOptions()
                .setCreate(false)
                .setWrite(false);
        return vertx.fileSystem()
                .open(configuration.publicKey(), openOptions);
    }
}
