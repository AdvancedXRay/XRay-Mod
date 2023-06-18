package pro.mikey.xray.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

        this.addRenderableWidget(Button.builder(Component.translatable("xray.single.close"), btn -> {
                    this.onClose();
                    Minecraft.getInstance().setScreen(new GuiSelectionScreen());
            })
            .pos((getWidth() / 2) - 100, (getHeight() / 2) + 80)
            .size(200, 20)
            .build()
        );
    }

    @Override
    public void renderExtra(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        int lineY = (getHeight() / 2) - 85;
        for (LinedText linedText : areas) {
            for (String line : linedText.getLines()) {
                lineY += 12;
                guiGraphics.drawString(getFontRender(), line, (getWidth() / 2) - 176, lineY, Color.WHITE.getRGB());
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
