package pro.mikey.xray.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.Utils;
import pro.mikey.xray.XRay;
import pro.mikey.xray.xray.Controller;

@EventBusSubscriber(modid = XRay.MOD_ID, value = Dist.CLIENT)
public class GuiOverlay {
    private static final ResourceLocation CIRCLE = Utils.rlFull(XRay.PREFIX_GUI + "circle.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void RenderGameOverlayEvent(RenderGuiEvent.Post event) {
        // Draw Indicator
        if(!Controller.isXRayActive() || !Configuration.general.showOverlay.get())
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
