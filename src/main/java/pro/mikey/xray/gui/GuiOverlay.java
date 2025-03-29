package pro.mikey.xray.gui;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
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

        GuiGraphics guiGraphics = event.getGuiGraphics();

        GpuDevice gpuDevice = RenderSystem.tryGetDevice();
        boolean renderDebug = gpuDevice != null && gpuDevice.isDebuggingEnabled();

        int x = 5, y = 5;
        if (renderDebug) {
            x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 10;
            y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 10;
        }

        guiGraphics.blit(RenderType::guiTextured, CIRCLE, x, y, 0f, 0f, 5, 5, 5, 5, 0xFF00FF00);

        int width = Minecraft.getInstance().font.width(I18n.get("xray.overlay"));
        guiGraphics.drawString(Minecraft.getInstance().font, I18n.get("xray.overlay"), x + (!renderDebug ? 10 : -width - 5), y - (!renderDebug ? 1 : 2), 0xff00ff00);
    }
}
