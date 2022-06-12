package pro.mikey.xray.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.gui.utils.GuiBase;

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

        this.addRenderableWidget(new Button((getWidth() / 2) - 100, (getHeight() / 2) + 80, 200, 20, Component.translatable("xray.single.close"), b -> {
            this.onClose();
            Minecraft.getInstance().setScreen(new GuiSelectionScreen());
        }));
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        float lineY = (getHeight() / 2f) - 85;
        for (LinedText linedText : areas) {
            for (String line : linedText.getLines()) {
                lineY += 12;
                this.getFontRender().drawShadow(stack, line,(getWidth() / 2f) - 176, lineY, Color.WHITE.getRGB());
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
        return I18n.get("xray.single.help");
    }

    private static class LinedText {
        private String[] lines;

        LinedText(String key) {
            this.lines = I18n.get(key).split("\\R");
        }

        String[] getLines() {
            return lines;
        }
    }
}
