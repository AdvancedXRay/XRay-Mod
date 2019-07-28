package com.xray.keybinding;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    // Key bindings
    // TODO: Refactor
    static final int keyIndex_toggleXray = 0;
    static final int keyIndex_showXrayMenu = 1;
    private static final int[] keyBind_keyValues = { GLFW.GLFW_KEY_BACKSLASH, GLFW.GLFW_KEY_G };
    private static final String[] keyBind_descriptions = { I18n.format("xray.config.toggle"), I18n.format("xray.config.open")};
    static KeyBinding[] keyBind_keys = null;

    public static void setup() {
        // Setup Key bindings
        keyBind_keys = new KeyBinding[ keyBind_descriptions.length ];
        for(int i = 0; i < keyBind_descriptions.length; ++i )
        {
            keyBind_keys[i] = new KeyBinding( keyBind_descriptions[i], keyBind_keyValues[i], "X-Ray" );
            ClientRegistry.registerKeyBinding( keyBind_keys[i] );
        }
    }
}
