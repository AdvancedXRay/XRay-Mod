package com.xray.keybinding;

import com.xray.XRay;
import com.xray.gui.GuiSelectionScreen;
import com.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyBindings {
    private static final String CATEGORY = "X-Ray";
    private static List<KeyActionable> keyBindings = new ArrayList<>();

    public static void setup() {
        keyBindings.add(new KeyActionable(GLFW.GLFW_KEY_BACKSLASH, I18n.format("xray.config.toggle"), Controller::toggleDrawOres));
        keyBindings.add(new KeyActionable(GLFW.GLFW_KEY_G, I18n.format("xray.config.open"), () -> XRay.mc.displayGuiScreen( new GuiSelectionScreen() )));

        keyBindings.forEach(e -> ClientRegistry.registerKeyBinding(e.getKeyBinding()));
    }

    @SubscribeEvent
    public static void eventInput(TickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == TickEvent.Phase.START || XRay.mc.currentScreen != null || XRay.mc.world == null)
            return;

        keyBindings.forEach( e -> {
            if( e.keyBinding.isPressed() )
                e.onPress.run();
        });
    }

    private static final class KeyActionable {
        private KeyBinding keyBinding;
        private Runnable onPress;

        KeyActionable(int key, String description, Runnable onPress) {
            this.onPress = onPress;
            this.keyBinding = new KeyBinding(description, key, CATEGORY);
        }

        KeyBinding getKeyBinding() {
            return keyBinding;
        }
    }
}
