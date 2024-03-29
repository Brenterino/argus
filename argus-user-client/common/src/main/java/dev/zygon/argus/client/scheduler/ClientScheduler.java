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
package dev.zygon.argus.client.scheduler;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.ClientConnection;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public enum ClientScheduler {

    INSTANCE;

    public <R> ScheduledFuture<R> invoke(Callable<R> task) {
        return eventGroup()
                .schedule(new SafeCallable<>(task), 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable task, long frequency, TimeUnit unit) {
        return registerWithDelay(task, 0, frequency, unit);
    }

    public ScheduledFuture<?> registerWithDelay(Runnable task, long delay,
                                                long frequency, TimeUnit unit) {
        return eventGroup()
                .scheduleAtFixedRate(new SafeRunnable(task), delay, frequency, unit);
    }

    // Utilize ClientConnection I/O pool for scheduling tasks
    private EventLoopGroup eventGroup() {
        var lazyIoGroup = Epoll.isAvailable() ?
                ClientConnection.EPOLL_CLIENT_IO_GROUP :
                ClientConnection.CLIENT_IO_GROUP;
        return lazyIoGroup.get();
    }

    private record SafeCallable<V>(@NonNull Callable<V> callable) implements Callable<V> {

        @Override
        public V call() {
            try {
                return callable.call();
            } catch (Throwable e) {
                log.warn("[ARGUS] Caught exception from callable, swallowing...", e);
                return null;
            }
        }
    }

    private record SafeRunnable(@NonNull Runnable delegate) implements Runnable {

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (Throwable e) {
                log.warn("[ARGUS] Caught exception from runnable, swallowing...", e);
            }
        }
    }
}
