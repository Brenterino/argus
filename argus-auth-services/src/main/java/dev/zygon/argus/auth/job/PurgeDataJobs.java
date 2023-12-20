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
package dev.zygon.argus.auth.job;

import dev.zygon.argus.auth.repository.ArgusOneTimePasswordRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PurgeDataJobs {

    private final ArgusOneTimePasswordRepository passwords;

    public PurgeDataJobs(ArgusOneTimePasswordRepository passwords) {
        this.passwords = passwords;
    }

    @Scheduled(cron = "{argus.auth.purge.otp.cron}")
    void purgeExpiredOneTimePasswords() {
        log.info("Starting purge of expired one time passwords");
        try {
            var success = passwords.deleteExpiredPasswords()
                    .await()
                    .indefinitely();
            log.info("Purging of expired one time passwords deleted rows? {}", success);
        } catch (Exception e) {
            log.error("Purging of expired one times passwords failed", e);
        }
        log.info("Ended purge of expired one time passwords.");
    }
}
