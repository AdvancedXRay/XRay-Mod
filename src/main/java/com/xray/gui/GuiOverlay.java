package com.xray.gui;

import com.xray.xray.Controller;
import com.xray.Configuration;
import com.xray.XRay;
import com.xray.reference.Reference;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class GuiOverlay {
    private static final ResourceLocation circle = new ResourceLocation(Reference.PREFIX_GUI + "circle.png");

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void RenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        // Draw Indicator
        if(!Controller.drawOres() || !Configuration.showOverlay || event.getType() != RenderGameOverlayEvent.ElementType.TEXT)
            return;

        GlStateManager.enableBlend();
        GlStateManager.color(0, 255, 0, 30);
        XRay.mc.renderEngine.bindTexture(circle);
        Gui.drawModalRectWithCustomSizedTexture(5, 5, 0f, 0f, 5, 5, 5, 5);
        GlStateManager.disableBlend();

        XRay.mc.fontRenderer.drawStringWithShadow(I18n.format("xray.overlay"), 15, 4, Color.getHSBColor(0f, 0f, 1f).getRGB() + (30 << 24));
    }

}
