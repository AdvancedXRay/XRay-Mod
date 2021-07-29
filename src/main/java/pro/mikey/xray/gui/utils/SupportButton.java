package pro.mikey.xray.gui.utils;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;


public class SupportButton extends Button {
    private List<FormattedText> support = new ArrayList<>();

    public SupportButton(int widthIn, int heightIn, int width, int height, Component text, TranslatableComponent support, OnPress onPress) {
        super(widthIn, heightIn, width, height, text, onPress);

        for(String line : support.getString().split("\n")) {
            this.support.add(new TextComponent(line));
        }
    }

    public List<FormattedText> getSupport() {
        return support;
    }
}
