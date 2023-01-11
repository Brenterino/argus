package dev.zygon.argus.client;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;

@Slf4j(topic = "Argus-ArgusFabricClient")
public class ArgusFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        log.info("Argus is loading.");
    }
}
