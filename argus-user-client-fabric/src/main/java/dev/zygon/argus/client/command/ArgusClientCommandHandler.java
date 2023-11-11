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
package dev.zygon.argus.client.command;

import dev.zygon.argus.auth.OneTimePassword;
import dev.zygon.argus.client.api.ArgusAuthApi;
import dev.zygon.argus.client.config.ArgusClientConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

@Slf4j
public enum ArgusClientCommandHandler {

    INSTANCE;

    @Setter private ArgusAuthApi auth;

    public void generateOneTimePassword() {
        auth.generateOTP()
                .enqueue(new OneTimePasswordCallback<>());
    }

    @EverythingIsNonNull
    private static class OneTimePasswordCallback<E> implements Callback<E> {

        @Override
        public void onResponse(Call<E> call, Response<E> response) {
            if (response.isSuccessful()) {
                acceptOneTimePassword(response.body());
            } else {
                onFailure();
            }
        }

        private void acceptOneTimePassword(E response) {
            if (response instanceof OneTimePassword password) {
                onSuccess(password);
            } else {
                onFailure();
            }
        }

        @Override
        public void onFailure(Call<E> call, Throwable t) {
            log.warn("Could not retrieve one-time-password from authentication services.", t);
            onFailure();
        }

        private void onSuccess(OneTimePassword password) {
            var minecraft = MinecraftClient.getInstance();
            var websiteText = createWebsiteText(password);
            var uuidText = createLabeledCopyText("text.command.argus.otp.success.uuid", password.uuid().toString());
            var otpText = createLabeledCopyText("text.command.argus.otp.success.pass", password.password());
            minecraft.inGameHud.addChatMessage(MessageType.SYSTEM,
                    websiteText, Util.NIL_UUID);
            minecraft.inGameHud.addChatMessage(MessageType.SYSTEM,
                    uuidText, Util.NIL_UUID);
            minecraft.inGameHud.addChatMessage(MessageType.SYSTEM,
                    otpText, Util.NIL_UUID);
        }

        private Text createWebsiteText(OneTimePassword password) {
            var config = ArgusClientConfig.getActiveConfig();
            var url = config.getArgusHost() + "/ui/login?namespace=" +
                    password.namespace().name() +
                    "&id=" +
                    password.uuid() +
                    "&otp=" + password.password();
            var clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            var urlText = new LiteralText(url)
                    .setStyle(Style.EMPTY
                            .withClickEvent(clickEvent)
                            .withColor(0x54FCFC)
                            .withUnderline(true));
            return new TranslatableText("text.command.argus.otp.success.webui")
                    .append(" ")
                    .append(urlText);
        }

        private Text createLabeledCopyText(String label, String value) {
            var clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value);
            var clickText = new LiteralText(value)
                    .setStyle(Style.EMPTY
                            .withClickEvent(clickEvent)
                            .withUnderline(true));
            return new TranslatableText(label)
                    .append(" ")
                    .append(clickText);
        }

        private void onFailure() {
            var minecraft = MinecraftClient.getInstance();
            var text = new TranslatableText("text.command.argus.otp.failure");
            minecraft.inGameHud.addChatMessage(MessageType.SYSTEM,
                    text, Util.NIL_UUID);
        }
    }
}
