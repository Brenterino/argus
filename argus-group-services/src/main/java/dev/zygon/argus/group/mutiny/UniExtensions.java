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
