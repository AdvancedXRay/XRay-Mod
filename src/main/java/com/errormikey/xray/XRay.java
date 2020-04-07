package com.errormikey.xray;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(XRay.MOD_ID)
public class XRay {
    public static final String MOD_ID = "xray";
    public static final Logger LOGGER = LogManager.getLogger(XRay.MOD_ID);

    public XRay() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::onSetup);
        bus.addListener(this::onLoadComplete);
        bus.addListener(this::onExit);

    }

    private void onSetup(final FMLCommonSetupEvent event) {
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
    }

    private void onExit(final FMLServerStoppingEvent event) {
    }
}
