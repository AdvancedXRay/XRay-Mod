package com.xray.client.gui;

import com.xray.client.xray.XRayController;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.Reference;
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

public class GuiOverlay extends Gui {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void RenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {

        // Draw Indicator
        if(!XRayController.drawOres() || !ConfigHandler.showOverlay)
            return;

        GlStateManager.enableAlpha();
        GlStateManager.color(0, 255, 0, 30);
        XRay.mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "circle.png"));
        drawModalRectWithCustomSizedTexture(5, 5, 0f, 0f, 5, 5, 5, 5);
        GlStateManager.disableAlpha();

        XRay.mc.fontRenderer.drawStringWithShadow(I18n.format("xray.overlay"), 15, 4, Color.getHSBColor(0f, 0f, 1f).getRGB() + (30 << 24));

    }

}
