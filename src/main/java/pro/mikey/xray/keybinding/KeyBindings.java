package pro.mikey.xray.keybinding;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.xray.Controller;


public class KeyBindings {
    private static final String CATEGORY = "X-Ray";

    public static KeyMapping toggleXRay = new KeyMapping(I18n.get("xray.config.toggle"), GLFW.GLFW_KEY_BACKSLASH, CATEGORY);
    public static KeyMapping toggleGui = new KeyMapping(I18n.get("xray.config.open"), GLFW.GLFW_KEY_G, CATEGORY);

    public static void setup() {
    }

    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(toggleXRay);
        event.register(toggleGui);
    }

    @SubscribeEvent
    public static void eventInput(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || Minecraft.getInstance().screen != null || Minecraft.getInstance().level == null)
            return;

        if (toggleXRay.consumeClick()) {
            Controller.toggleXRay();
        }

        if (toggleGui.consumeClick()) {
            Minecraft.getInstance().setScreen(new GuiSelectionScreen());
        }
    }
}
