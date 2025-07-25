package pro.mikey.xray.screens.helpers;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class SliderWidget extends AbstractSliderButton {
    private final String messageKey;

    public SliderWidget(int x, int y, int width, int height, String messageKey, double initialValue) {
        super(x, y, width, height, Component.translatable(messageKey, (int) (initialValue * 255)), initialValue);
        this.messageKey = messageKey;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.translatable(messageKey, (int) (this.value * 255)));
    }

    @Override
    protected void applyValue() {
    }

    public double getValue() {
        return this.value;
    }
}
