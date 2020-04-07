package com.v2.gui.utils;

import net.minecraft.client.gui.widget.button.Button;

import java.util.List;

public class SupportButton extends Button {
    private List<String> support;

    public SupportButton(int widthIn, int heightIn, int width, int height, String text, List<String> support, IPressable onPress) {
        super(widthIn, heightIn, width, height, text, onPress);

        this.support = support;
    }

    public List<String> getSupport() {
        return support;
    }
}
