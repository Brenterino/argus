package dev.zygon.argus.client.scheduler;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.util.concurrent.ScheduledFuture;
import net.minecraft.network.ClientConnection;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public enum ClientScheduler {

    INSTANCE;

    public <R> ScheduledFuture<R> invoke(Callable<R> task) {
        return eventGroup()
                .schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable task, long frequency, TimeUnit unit) {
        return eventGroup()
                .scheduleAtFixedRate(task, 0, frequency, unit);
    }

    // Utilize ClientConnection I/O pool for scheduling tasks
    private EventLoopGroup eventGroup() {
        var lazyIoGroup = Epoll.isAvailable() ?
                ClientConnection.EPOLL_CLIENT_IO_GROUP :
                ClientConnection.CLIENT_IO_GROUP;
        return lazyIoGroup.get();
    }
}
