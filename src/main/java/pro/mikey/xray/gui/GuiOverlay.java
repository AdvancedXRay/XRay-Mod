package pro.mikey.xray.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.XRay;
import pro.mikey.xray.xray.Controller;

@Mod.EventBusSubscriber(modid = XRay.MOD_ID, value = Dist.CLIENT)
public class GuiOverlay {
    private static final ResourceLocation CIRCLE = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void RenderGameOverlayEvent(RenderGuiOverlayEvent event) {
        // Draw Indicator
        if(!Controller.isXRayActive() || !Configuration.general.showOverlay.get() || event.isCanceled())
            return;

        RenderSystem.setShaderColor(0, 1F, 0, 1F);
        RenderSystem.setShaderTexture(0, CIRCLE);
        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.blit(CIRCLE, 5, 5, 0f, 0f, 5, 5, 5, 5);

        guiGraphics.drawString(Minecraft.getInstance().font, I18n.get("xray.overlay"), 15, 4, 0xffffffff);

        // Reset color
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
}
