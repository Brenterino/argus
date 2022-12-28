package dev.zygon.argus.group.mutiny;

import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UniExtensions {

    private record Fork<O>(Uni<O> ifTrue, Uni<O> ifFalse) implements Function<Uni<Boolean>, Uni<O>> {

        @Override
        public Uni<O> apply(Uni<Boolean> condition) {
            return condition.flatMap(value -> value ? ifTrue : ifFalse);
        }
    }

    public static <T> Fork<T> fork(Uni<T> ifTrue, Uni<T> ifFalse) {
        return new Fork<>(ifTrue, ifFalse);
    }

    public static Fork<Void> failIfTrue(Throwable throwable) {
        return fork(
                Uni.createFrom().failure(throwable),
                Uni.createFrom().voidItem()
        );
    }

    public static Fork<Void> failIfFalse(Throwable throwable) {
        return fork(
                Uni.createFrom().voidItem(),
                Uni.createFrom().failure(throwable)
        );
    }

    public static Fork<Boolean> checkIfTrue(Uni<Boolean> ifTrue) {
        return fork(
                ifTrue,
                Uni.createFrom().item(true));
    }

    public static Fork<Boolean> checkIfFalse(Uni<Boolean> ifFalse) {
        return fork(
                Uni.createFrom().item(true),
                ifFalse
        );
    }
}
