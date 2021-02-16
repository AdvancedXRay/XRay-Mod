package pro.mikey.xray.gui.utils;

import net.minecraft.client.gui.widget.GameSettingsSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SupportButton extends Button {
    private List<ITextProperties> support = new ArrayList<>();

    public SupportButton(int widthIn, int heightIn, int width, int height, ITextComponent text, TranslationTextComponent support, IPressable onPress) {
        super(widthIn, heightIn, width, height, text, onPress);

        for(String line : support.getString().split("\n")) {
            this.support.add(new StringTextComponent(line));
        }
    }

    public List<ITextProperties> getSupport() {
        return support;
    }
}
