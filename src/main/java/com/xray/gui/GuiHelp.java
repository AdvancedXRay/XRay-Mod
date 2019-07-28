package com.xray.gui;

import com.xray.XRay;
import com.xray.gui.utils.GuiBase;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiHelp extends GuiBase {
    public GuiHelp() {
        super(false);
        this.setSize(380, 210);
    }

    private List<LinedText> areas = new ArrayList<>();

    @Override
    public void initGui() {
        super.initGui();

        areas.clear();
        areas.add(new LinedText("xray.message.help.state"));
        areas.add(new LinedText("xray.message.help.gui"));
        areas.add(new LinedText("xray.message.help.warning"));

        this.addButton(new GuiButton(1, (width / 2) - 100, (height / 2) + 80, I18n.format("xray.single.close")));
    }

    @Override
    public void drawScreen(int x, int y, float f) {
        super.drawScreen(x, y, f);

        float lineY = (height / 2f) - 85;
        for (LinedText linedText : areas) {
            for (String line : linedText.getLines()) {
                lineY += 12;
                this.getFontRender().drawStringWithShadow(line,(width / 2f) - 176, lineY, Color.WHITE.getRGB());
            }
            lineY += 10;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            XRay.mc.player.closeScreen();
            XRay.mc.displayGuiScreen(new GuiSelectionScreen());
        }
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public Color colorBackground() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public String title() {
        return I18n.format("xray.single.help");
    }

    private static class LinedText {
        private String[] lines;

        public LinedText(String key) {
            this.lines = I18n.format(key).split("\n");
        }

        public String[] getLines() {
            return lines;
        }
    }
}
