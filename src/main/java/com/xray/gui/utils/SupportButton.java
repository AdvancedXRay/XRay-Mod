package com.xray.gui.utils;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;

public class SupportButton extends Button {
    private List<StringTextComponent> support;

    public SupportButton(int widthIn, int heightIn, int width, int height, ITextComponent text, TranslationTextComponent support, IPressable onPress) {
        super(widthIn, heightIn, width, height, text, onPress);

        for(String line : support.toString().split("\n")) {
            this.support.add(new StringTextComponent(line));
        }
    }

    public List<StringTextComponent> getSupport() {
        return support;
    }
}
