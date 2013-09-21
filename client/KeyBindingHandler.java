package fgtXray.client;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import fgtXray.FgtXRay;

public class KeyBindingHandler extends KeyHandler {
	public static KeyBinding toggleXray = new KeyBinding("Fgt XRay - Toggle", Keyboard.KEY_T);
	public static KeyBinding openXrayMenu = new KeyBinding("Fgt XRay - Menu", Keyboard.KEY_Y);
	
	public static KeyBinding[] arrayOfKeys = new KeyBinding[] { toggleXray, openXrayMenu };
	public static boolean[] areRepeating = new boolean[] { false, false };
	private Minecraft mc = Minecraft.getMinecraft();
	
	public KeyBindingHandler() {
		super(arrayOfKeys, areRepeating);
	}

	@Override
	public String getLabel() {
		return "Fgt XRay KeyBind";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) { // Add the default keybinds to the forge keybind.
		if( !tickEnd && (mc.currentScreen == null) && (mc.theWorld != null) ){
			if(kb.keyCode == toggleXray.keyCode){
				FgtXRay.drawOres = !FgtXRay.drawOres;
				RenderTick.ores.clear();
			}else if(kb.keyCode == openXrayMenu.keyCode){
				mc.displayGuiScreen( new GuiSettings() );
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}
	
}
