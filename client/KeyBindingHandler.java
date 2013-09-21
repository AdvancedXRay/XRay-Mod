package fgtXray.client;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import fgtXray.FgtXRay;

public class KeyBindingHandler extends KeyHandler {
	public static KeyBinding toggleFindIt = new KeyBinding("FindIt - Toggle", Keyboard.KEY_T);
	public static KeyBinding openFindIt = new KeyBinding("FindIt - Menu", Keyboard.KEY_Y);
	
	public static KeyBinding[] arrayOfKeys = new KeyBinding[] { toggleFindIt, openFindIt };
	public static boolean[] areRepeating = new boolean[] { false, false };
	private Minecraft mc = Minecraft.getMinecraft();
	
	public KeyBindingHandler() {
		super(arrayOfKeys, areRepeating);
	}

	@Override
	public String getLabel() {
		return "FindIt KeyBind";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {	
		if( !tickEnd && (mc.currentScreen == null) && (mc.theWorld != null) ){
			
			if(kb.keyCode == toggleFindIt.keyCode){
				FgtXRay.drawOres = !FgtXRay.drawOres;
				RenderTick.ores.clear();
			}else if(kb.keyCode == openFindIt.keyCode){
				mc.displayGuiScreen( new GuiSettings() );
			}
			
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}
	
}
