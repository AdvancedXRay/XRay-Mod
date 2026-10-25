package pro.mikey.xray.screens.helpers;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

/// Checkbox that can represent three states: none selected, some selected, all selected. The "some selected" state is represented by a dash in the middle of the checkbox.
public class TriStateCheckbox extends AbstractButton {
    private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");
    private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX_SELECTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");

    public enum State {
        NONE,
        SOME,
        ALL
    }

    private final Supplier<State> state;
    private final Consumer<Boolean> onToggle;

    public TriStateCheckbox(int x, int y, int size, Component message, Supplier<State> state, Consumer<Boolean> onToggle) {
        super(x, y, size, size, message);
        this.state = state;
        this.onToggle = onToggle;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.onToggle.accept(this.state.get() != State.ALL);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        State current = this.state.get();

        Identifier sprite = current == State.ALL
                ? (this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE)
                : (this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        // Just manually draw the dash instead of using a sprite for it, since it's just a simple rectangle
        if (current == State.SOME) {
            int dashWidth = this.getWidth() / 2;
            int dashX = this.getX() + (this.getWidth() - dashWidth) / 2;
            int dashY = this.getY() + this.getHeight() / 2 - 1;
            graphics.fill(dashX, dashY, dashX + dashWidth, dashY + 2, 0xFFFFFFFF);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
