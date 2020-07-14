package com.xray.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.xray.gui.utils.GuiBase;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

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
    public void init() {
        super.init();

        areas.clear();
        areas.add(new LinedText("xray.message.help.gui"));
        areas.add(new LinedText("xray.message.help.warning"));

        this.addButton(new Button((getWidth() / 2) - 100, (getHeight() / 2) + 80, 200, 20, new TranslationTextComponent("xray.single.close"), b -> this.onClose()));
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        float lineY = (getHeight() / 2f) - 85;
        for (LinedText linedText : areas) {
            for (String line : linedText.getLines()) {
                lineY += 12;
                this.getFontRender().drawStringWithShadow(stack, line,(getWidth() / 2f) - 176, lineY, Color.WHITE.getRGB());
            }
            lineY += 10;
        }
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public ResourceLocation getBackground() {
        return BG_LARGE;
    }

    @Override
    public String title() {
        return I18n.format("xray.single.help");
    }

    private static class LinedText {
        private String[] lines;

        LinedText(String key) {
            this.lines = I18n.format(key).split("\\R");
        }

        String[] getLines() {
            return lines;
        }
    }
}
