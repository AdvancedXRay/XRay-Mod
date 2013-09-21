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
	public void initGui(){ // Called when the gui should be (re)created.
		if( OresSearch.searchList.isEmpty() ){ return; } // This shouldnt happen. But return if it does.
		this.buttons = new HashMap<String, OreButtons>(); // String id for the button. Same as the button text. (Diamond / Iron ect.)
		this.buttonList.clear();
		
		int x = 2;
		int y = 2;
		int numButtons = 0;
		
		for( OreInfo ore : OresSearch.searchList ){
			if( buttons.get( ore.oreName ) != null ){ // Button already created for this ore.
				buttons.get( ore.oreName ).ores.add( ore ); // Add this new OreInfo to the internal ArrayList for this button.
			} else {	// Create the new button for this ore.
				int id = Integer.parseInt( Integer.toString( ore.id ) + Integer.toString( ore.meta) ); // Unique button id. int( str(id) + str(meta) )
				this.buttonList.add( new GuiButton( id, x, y, 100, 20, ore.oreName + ": " + (ore.draw ? "On" : "Off") ) ); // create new button and set the text to Name: On||Off 
				buttons.put( ore.oreName, new OreButtons( ore.oreName, id,  ore ) ); // Add this new button to the buttons hashmap.
				numButtons += 1; // Button counter for dynamic placement of buttons.
				y += 20; // Next button should be placed down from this one.
				if( (numButtons % 10) == 0 ){ // The mod is maximum number of buttons in one column.
					x+=100; y = 2; // Move next button to the right and reset the y.
				}
			}
		}
		
		this.buttonList.add( new GuiButton(98, this.width-102, this.height-22, 100, 20, "Print OreDict") ); // Static button for search distance.
		this.buttonList.add( new GuiButton(99, 2, this.height-22, 100, 20, "Distance: "+FgtXRay.distStrings[ FgtXRay.distIndex ]) ); // Static button for printing the ore dictionary / searchList.
	}
	
	@Override
	public void actionPerformed( GuiButton button ){
		switch(button.id){
		case 98:		// Print OreDict
			for ( String name : OreDictionary.getOreNames() ){ // Print the ore dictionary.
				System.out.println( "[OreDict] "+name );
				/*for( ItemStack stack : OreDictionary.getOres( OreDictionary.getOreID( name ) ) ){ // Uncomment this to print the id:meta for each ore dictionary.
					System.out.println( String.format("\t%d:%d", stack.itemID, stack.getItemDamage() ) );
				}*/
			}
			if (!OresSearch.searchList.isEmpty()){ // Print out the searchList.
				for( OreInfo ore : OresSearch.searchList ){
					System.out.println( String.format("[Fgt XRay] OreInfo( %s, %d, %d, 0x%x, %b )", ore.oreName, ore.id, ore.meta, ore.color, ore.draw ) );
				}
			}
			break;
		case 99:		// Distance Button
			if (FgtXRay.distIndex < FgtXRay.distNumbers.length-1){
				FgtXRay.distIndex++;
			}else{
				FgtXRay.distIndex = 0;
			}
			break;
		default:
			for( Map.Entry<String, OreButtons> entry : buttons.entrySet() ){ // Iterate through the buttons map and check what ores need to be toggled
				String key = entry.getKey(); // Block name (Diamond)
				OreButtons value = entry.getValue(); // OreButtons structure
				
				if( value.id == button.id ){ // Matched the butons unique id. 
					for( OreInfo tempOre : value.ores ){ // Iterate through the ores that this button should toggle.
						for( OreInfo ore : OresSearch.searchList ){ // Match this ore with the one in the searchList.
							if( (tempOre.id == ore.id) && (tempOre.meta == ore.meta) ){
								ore.draw = !ore.draw; // Invert searchList.ore.draw
								//System.out.println( String.format( "[Fgt XRay] Setting %s %d:%d to %b", ore.oreName, ore.id, ore.meta, ore.draw ) );
							}
						}
					}
				}
			}
			break;
		}
		this.initGui(); // Redraw the gui.
	}
	
	@Override
	protected void keyTyped( char par1, int par2 ){
		if (par2 == 1 || par2 == mc.gameSettings.keyBindInventory.keyCode){ // Close on esc or inventory key (e)
			mc.thePlayer.closeScreen();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame(){ // Dont pause the game in single player.
		return false;
	}
	
	@Override
	public void drawScreen( int x, int y, float f ){
		drawDefaultBackground();  // Draws the opaque black background
		super.drawScreen(x, y, f);
	}
}