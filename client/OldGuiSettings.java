// http://docs.larry1123.net/forge/1133/
package fgtXray.client;

import java.awt.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;

import fgtXray.FgtXRay;
import fgtXray.OreButtons;
import fgtXray.OreInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.oredict.OreDictionary;


public class OldGuiSettings extends GuiScreen {
	Map<String, OreButtons> buttons = new HashMap<String, OreButtons>();
	
	@Override
	public void initGui(){ // Called when the gui should be (re)created.
		if( OresSearch.searchList.isEmpty() ){ // This shouldnt happen. But return if it does.
			System.out.println( "[Fgt XRay] Error: searchList is empty inside initGui call!" );
			return;
		}
		this.buttons = new HashMap<String, OreButtons>(); // String id for the button. Same as the button text. (Diamond / Iron ect.)
		this.buttonList.clear();
		
		int x = 2;
		int y = 2;
		
		for( OreInfo ore : OresSearch.searchList ){
			if( buttons.get( ore.oreName ) != null ){ // Button already created for this ore.
				buttons.get( ore.oreName ).ores.add( ore ); // Add this new OreInfo to the internal ArrayList for this button.
				
			} else {	// Create the new button for this ore.
				int id = Integer.parseInt( Integer.toString( ore.id ) + Integer.toString( ore.meta) ); // Unique button id. int( str(id) + str(meta) )
				this.buttonList.add( new GuiButton( id, x, y, 100, 20, ore.oreName + ": " + (ore.draw ? "On" : "Off") ) ); // create new button and set the text to Name: On||Off 
				buttons.put( ore.oreName, new OreButtons( ore.oreName, id,  ore ) ); // Add this new button to the buttons hashmap.
				y += 20; // Next button should be placed down from this one.
				if( y > (this.height * 0.7) ){
					x+=100; y = 2; // Move next button to the right and reset the y.
				}
			}
		}
		
		this.buttonList.add( new GuiButton(97, 2, this.height-42, 100, 20, "Add Ore" ) );
		this.buttonList.add( new GuiButton(98, 2, this.height-22, 100, 20, "Distance: "+FgtXRay.distStrings[ FgtXRay.distIndex ]) ); // Static button for printing the ore dictionary / searchList.
		this.buttonList.add( new GuiButton(99, this.width-102, this.height-22, 100, 20, "Print OreDict") ); // Static button for search distance.
	}
	
	@Override
	public void actionPerformed( GuiButton button ){ // Called on left click of GuiButton
	  switch(button.id){
		case 99: // Print OreDict
			for ( String name : OreDictionary.getOreNames() ){ // Print the ore dictionary.
				ArrayList<ItemStack> oreStack = OreDictionary.getOres( OreDictionary.getOreID( name ) );
				System.out.print( String.format("[OreDict] %-40.40s [%d types] ( ", name, oreStack.size() ) );
				StringBuilder idMetaCsv = new StringBuilder();
				if( oreStack.size() < 1 ){
					idMetaCsv.append( " )" );
				}
				for( ItemStack stack : oreStack ){
					if( stack == oreStack.get( oreStack.size() - 1 ) ){
						idMetaCsv.append( String.format( "%d:%d )", Item.getIdFromItem( stack.getItem() ), stack.getItemDamage() ) );
					} else {
						idMetaCsv.append( String.format( "%d:%d, ", Item.getIdFromItem( stack.getItem() ), stack.getItemDamage() ) );
					}
				}
				System.out.println( idMetaCsv.toString() );
			}
			if (!OresSearch.searchList.isEmpty()){ // Print out the searchList.
				for( OreInfo ore : OresSearch.searchList ){
					System.out.println( String.format("[Fgt XRay] OreInfo( %s, %d, %d, 0x%x, %b )", ore.oreName, ore.id, ore.meta, ore.color, ore.draw ) );
				}
			}
			break;
			
		case 98: // Distance Button
			if (FgtXRay.distIndex < FgtXRay.distNumbers.length - 1){
				FgtXRay.distIndex++;
			}else{
				FgtXRay.distIndex = 0;
			}
			ConfigHandler.update("searchdist", false);
			break;
			
		case 97: // New Ore button
			mc.thePlayer.closeScreen();
			if( !FgtXRay.hasBeenNagged ){
				String notify = "[§aFgt XRay§r] §cThe in-game add ore system is not finished yet! §rIt doesn't save to disk or support the ore dictionary yet. I'm tired and just want to goto bed.";
				ChatComponentText chat = new ChatComponentText( notify );
				mc.ingameGUI.getChatGUI().printChatMessage( chat );
				FgtXRay.hasBeenNagged = true;
			}
			mc.displayGuiScreen( new GuiNewOre() );
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
								ConfigHandler.update( ore.oreName, ore.draw );
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
		super.keyTyped( par1, par2 );
		if( (par2 == 1) || (par2 == mc.gameSettings.keyBindInventory.getKeyCode()) || 
		 par2 == FgtXRay.keyBind_keys[ FgtXRay.keyIndex_showXrayMenu ].getKeyCode() ){ // Close on esc, inventory key or keybind
			/*if( distChanged ){ // Save the config file when we exit.
				ConfigHandler.update("searchdist", false);
				distChanged=false;
			}*/
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
	
	@Override
	public void mouseClicked( int x, int y, int mouse ){
		super.mouseClicked( x, y, mouse );
		if( mouse == 1 ){ // Right clicked
			for( int i = 0; i < this.buttonList.size(); i++ ){
				GuiButton button = (GuiButton)this.buttonList.get( i );
				if( button.func_146115_a() ){ //func_146115_a() returns true if the button is being hovered
					//mc.theWorld.playSoundAtEntity( mc.thePlayer, "minecraft.sound.random.click", 1.0F, 1.0F ); TODO: click sound...
					if( button.id == 98 ){
						if( FgtXRay.distIndex > 0 ){
							FgtXRay.distIndex--;
						} else {
							FgtXRay.distIndex = FgtXRay.distNumbers.length - 1;
						}
						ConfigHandler.update("searchdist", false);
						this.initGui();
					}
				}
			}
		}
		
		//System.out.println( String.format( "mouseClicked( %d, %d, %d )", x, y, mouse ) );
	}
}