package com.xray.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xray.Configuration;
import com.xray.XRay;
import com.xray.xray.Controller;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XRay.MOD_ID, value = Dist.CLIENT)
public class GuiOverlay {
    private static final ResourceLocation circle = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void RenderGameOverlayEvent(RenderGameOverlayEvent event) {
        // Draw Indicator
        if(!Controller.isXRayActive() || !Configuration.general.showOverlay.get() || event.isCanceled() || event.getType() != RenderGameOverlayEvent.ElementType.TEXT )
            return;

        RenderSystem.color3f(0, 255, 0);
        XRay.mc.getTextureManager().bindTexture(circle);
        Screen.blit(5, 5, 0f, 0f, 5, 5, 5, 5);

        XRay.mc.fontRenderer.drawStringWithShadow(I18n.format("xray.overlay"), 15, 4, 0xffffffff);
    }
}
