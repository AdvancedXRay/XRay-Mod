package pro.mikey.xray.keybinding;

import net.minecraftforge.fmlclient.registry.ClientRegistry;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyBindings {
    private static final String CATEGORY = "X-Ray";
    private static List<KeyActionable> keyBindings = new ArrayList<>();

    public static KeyActionable toggleXRay = new KeyActionable(GLFW.GLFW_KEY_BACKSLASH, I18n.get("xray.config.toggle"), Controller::toggleXRay);
    public static KeyActionable toggleGui = new KeyActionable(GLFW.GLFW_KEY_G, I18n.get("xray.config.open"), () -> Minecraft.getInstance().setScreen( new GuiSelectionScreen() ));

    public static void setup() {
        keyBindings.add(toggleXRay);
        keyBindings.add(toggleGui);

        keyBindings.forEach(e -> ClientRegistry.registerKeyBinding(e.getKeyBinding()));
    }

    @SubscribeEvent
    public static void eventInput(TickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == TickEvent.Phase.START || Minecraft.getInstance().screen != null || Minecraft.getInstance().level == null)
            return;

        keyBindings.forEach( e -> {
            if( e.keyBinding.consumeClick() )
                e.onPress.run();
        });
    }

    public static final class KeyActionable {
        private KeyMapping keyBinding;
        private Runnable onPress;

        KeyActionable(int key, String description, Runnable onPress) {
            this.onPress = onPress;
            this.keyBinding = new KeyMapping(description, key, CATEGORY);
        }

        public KeyMapping getKeyBinding() {
            return keyBinding;
        }
    }
}
