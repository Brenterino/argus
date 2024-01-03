package dev.zygon.argus.client;

import dev.zygon.argus.client.command.ArgusForgeClientCommands;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.event.KeyPressHandler;
import dev.zygon.argus.client.status.UserStatusChecker;
import lombok.extern.slf4j.Slf4j;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Slf4j
@Mod("argus")
public class ArgusForgeClient {

    private KeyPressHandler keyPressHandler;

    public ArgusForgeClient() {
        FMLJavaModLoadingContext.get()
                        .getModEventBus()
                        .addListener(this::init);
    }

    public void init(FMLClientSetupEvent event) {
        log.info("[ARGUS] Argus Forge is loading.");
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get() // mod config
                .registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
                        new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> AutoConfig.getConfigScreen(ArgusClientConfig.class, parent).get()));
        keyPressHandler = new KeyPressHandler(keyBinding -> {
            ClientRegistry.registerKeyBinding(keyBinding);
            return keyBinding;
        });
        var holder = AutoConfig.register(ArgusClientConfig.class,
                JanksonConfigSerializer::new);
        holder.registerSaveListener((save, x) -> {
            ArgusClientConfig.setActiveConfig(save.getConfig());
            return ActionResult.SUCCESS;
        });
        ArgusClientConfig.setActiveConfig(holder.getConfig());
        ArgusForgeClientCommands.registerCommands();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        var client = MinecraftClient.getInstance();
        if (client == null) return; // shouldn't happen, but guard case
        if (keyPressHandler != null) {
            keyPressHandler.onTick(client);
        }
        UserStatusChecker.INSTANCE.onTick(client);
    }
}
