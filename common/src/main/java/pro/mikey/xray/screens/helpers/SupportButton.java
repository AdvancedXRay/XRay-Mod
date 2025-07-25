package pro.mikey.xray.screens.helpers;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;


public class SupportButton extends Button {
    private List<FormattedText> support = new ArrayList<>();

    public SupportButton(int widthIn, int heightIn, int width, int height, Component text, MutableComponent support, OnPress onPress) {
        super(widthIn, heightIn, width, height, text, onPress, DEFAULT_NARRATION);

        for(String line : support.getString().split("\n")) {
            this.support.add(Component.literal(line));
        }
    }

    public List<FormattedText> getSupport() {
        return support;
    }
}
