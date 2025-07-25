package pro.mikey.xray.screens;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.ScanController;

public class HudOverlay {
    private static final ResourceLocation CIRCLE = XRay.assetLocation("gui/circle.png");

    public static void renderGameOverlayEvent(GuiGraphics graphics) {
        // Draw Indicator
        if(!ScanController.INSTANCE.isXRayActive() || !XRay.config().showOverlay.get())
            return;

        GpuDevice gpuDevice = RenderSystem.tryGetDevice();
        boolean renderDebug = gpuDevice != null && gpuDevice.isDebuggingEnabled();

        int x = 5, y = 5;
        if (renderDebug) {
            x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 10;
            y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 10;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, CIRCLE, x, y, 0f, 0f, 5, 5, 5, 5, 0xFF00FF00);

        int width = Minecraft.getInstance().font.width(I18n.get("xray.overlay"));
        graphics.drawString(Minecraft.getInstance().font, I18n.get("xray.overlay"), x + (!renderDebug ? 10 : -width - 5), y - (!renderDebug ? 1 : 2), 0xff00ff00);
    }
}
