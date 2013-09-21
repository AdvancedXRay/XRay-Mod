package fgtXray.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fgtXray.FgtXRay;
import fgtXray.OreButtons;
import fgtXray.OreInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class GuiSettings extends GuiScreen {
	Map<String, OreButtons> buttons = new HashMap<String, OreButtons>();
	
	@Override
	public void initGui(){
		if( OresSearch.searchList.isEmpty() ){ return; }
		this.buttons = new HashMap<String, OreButtons>();
		this.buttonList.clear();
		
		int x = 2;
		int y = 2;
		int numButtons = 0;
		
		for( OreInfo ore : OresSearch.searchList ){
			if( buttons.get( ore.oreName ) != null ){
				buttons.get( ore.oreName ).ores.add( ore );
			} else {
				int id = Integer.parseInt( Integer.toString( ore.id ) + Integer.toString( ore.meta) );
				this.buttonList.add( new GuiButton( id, x, y, 100, 20, ore.oreName + ": " + (ore.draw ? "On" : "Off") ) );
				buttons.put( ore.oreName, new OreButtons( ore.oreName, id,  ore ) );
				numButtons += 1;
				y += 20;
				if( (numButtons % 10) == 0 ){ // The mod is maximum number of buttons in one column.
					x+=100; y = 2;
				}
			}
		}
		
		this.buttonList.add( new GuiButton(98, this.width-102, this.height-22, 100, 20, "Print OreDict") );
		this.buttonList.add( new GuiButton(99, 2, this.height-22, 100, 20, "Distance: "+FgtXRay.distStrings[ FgtXRay.distIndex ]) );
	}
	
	@Override
	public void actionPerformed( GuiButton button ){
		switch(button.id){
		case 98:
			for ( String name : OreDictionary.getOreNames() ){
				System.out.println( "[OreDict] "+name );
				for( ItemStack stack : OreDictionary.getOres( OreDictionary.getOreID( name ) ) ){
					System.out.println( String.format("\t%d:%d", stack.itemID, stack.getItemDamage() ) );
				}
			}
			if (!OresSearch.searchList.isEmpty()){
				for( OreInfo ore : OresSearch.searchList ){
					System.out.println( String.format("[FindIt] OreInfo( %s, %d, %d, 0x%x, %b )", ore.oreName, ore.id, ore.meta, ore.color, ore.draw ) );
				}
			}
			break;
		case 99:
			if (FgtXRay.distIndex < FgtXRay.distNumbers.length-1){
				FgtXRay.distIndex++;
			}else{
				FgtXRay.distIndex = 0;
			}
			break;
		default:
			for( Map.Entry<String, OreButtons> entry : buttons.entrySet() ){
				String key = entry.getKey();
				OreButtons value = entry.getValue();
				
				if( value.id == button.id ){
					for( OreInfo tempOre : value.ores ){
						for( OreInfo ore : OresSearch.searchList ){
							if( (tempOre.id == ore.id) && (tempOre.meta == ore.meta) ){
								ore.draw = !ore.draw;
								//System.out.println( String.format( "[FindIt] Setting %s %d:%d to %b", ore.oreName, ore.id, ore.meta, ore.draw ) );
							}
						}
					}
				}
			}
			break;
		}
		this.initGui();
	}
	
	@Override
	protected void keyTyped( char par1, int par2 ){
		if (par2 == 1 || par2 == mc.gameSettings.keyBindInventory.keyCode){
			mc.thePlayer.closeScreen();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
	
	@Override
	public void drawScreen( int x, int y, float f ){
		drawDefaultBackground();  // Draws the opaque black background
		super.drawScreen(x, y, f);
	}
}