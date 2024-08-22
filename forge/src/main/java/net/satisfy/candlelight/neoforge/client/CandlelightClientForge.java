package net.satisfy.candlelight.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.client.CandlelightClient;
import net.satisfy.candlelight.client.gui.LetterGui;
import net.satisfy.candlelight.registry.ScreenHandlerTypeRegistry;

@EventBusSubscriber(modid = Candlelight.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class CandlelightClientForge {
    @SubscribeEvent
    public static void beforeClientSetup(RegisterEvent event) {
        CandlelightClient.preInitClient();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CandlelightClient.initClient();
    }

    @SubscribeEvent
    public static void clientSetup(RegisterMenuScreensEvent event) {
        event.register(ScreenHandlerTypeRegistry.LETTER_SCREEN_HANDLER.get(), LetterGui::new);
    }
}
