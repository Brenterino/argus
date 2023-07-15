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
package dev.zygon.argus.client.status;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.status.EffectStatus;
import dev.zygon.argus.status.ItemStatus;
import dev.zygon.argus.status.UserStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public enum UserStatusChecker {

    INSTANCE;

    private Instant lastCheck;
    private final Map<Integer, EffectStatus> effects;
    private final Map<Integer, ItemStatus> items;
    @Getter private UserStatus userStatus;

    UserStatusChecker() {
        this.lastCheck = Instant.now();
        this.effects = new HashMap<>();
        this.items = new HashMap<>();
    }

    public void onTick(MinecraftClient client) {
        var player = client.player;
        var config = ArgusClientConfig.getActiveConfig();
        var duration = Duration.between(lastCheck, Instant.now());
        if (player != null && duration.toMillis() >= config.getStatusCheckerIntervalMillis()) {
            checkInventory(player);
            checkStatusEffects(player);
            updateStatus();
            effects.clear();
            items.clear();
            lastCheck = Instant.now();
        }
    }

    private void checkInventory(ClientPlayerEntity player) {
        var inventory = player.getInventory();
        for (var stackIndex = 0; stackIndex < inventory.size(); stackIndex++) {
            var stack = inventory.getStack(stackIndex);
            var item = stack.getItem();
            var potion = PotionUtil.getPotion(stack);
            if (item.isFood()) {
                checkFood(stack);
            } else if (potion != Potions.EMPTY) {
                checkPotion(stack, potion);
            }
        }
    }

    private void checkFood(ItemStack stack) {
        final var gappleSymbol = "●"; // TODO make config?
        final var gappleColor = 0xFFD700; // TODO make config?
        var item = stack.getItem();
        var food = item.getFoodComponent();
        if (food == FoodComponents.GOLDEN_APPLE || food == FoodComponents.ENCHANTED_GOLDEN_APPLE) {
            var status = new ItemStatus(gappleColor, gappleSymbol, stack.getCount());
            checkAndMergeItem(status);
        }
    }

    private void checkPotion(ItemStack stack, Potion potion) {
        final var potionSymbol = "⚗"; // TODO make config?
        var color = PotionUtil.getColor(potion);
        var status = new ItemStatus(color, potionSymbol, stack.getCount());
        checkAndMergeItem(status);
    }

    private void checkAndMergeItem(ItemStatus status) {
        int key = status.color();
        if (items.containsKey(key)) {
            var current = items.get(key);
            var updated = new ItemStatus(current.color(), current.symbol(),
                    current.count() + status.count());
            items.put(key, updated);
        } else {
            items.put(key, status);
        }
    }

    private void checkStatusEffects(ClientPlayerEntity player) {
        final var buffSymbol = "↑"; // TODO make config?
        final var debuffSymbol = "↓"; // TODO make config?
        var stati = player.getStatusEffects();
        for (var status : stati) {
            var type = status.getEffectType();
            var color = type.getColor();
            var symbol = !type.isBeneficial() ? debuffSymbol : buffSymbol;
            var duration = status.getDuration();
            var effect = new EffectStatus(color, symbol, duration);
            effects.put(color, effect);
        }
    }

    private void updateStatus() {
        var itemStatuses = items.values()
                .stream()
                .sorted()
                .toList();
        var effectStatuses = effects.values()
                .stream()
                .sorted()
                .toList();
        this.userStatus = new UserStatus(itemStatuses, effectStatuses);
    }
}
