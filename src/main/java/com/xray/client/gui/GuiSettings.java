package com.xray.client.gui;

import com.xray.client.OresSearch;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.OreButtons;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiSettings extends GuiContainer
{
	private Map<String, OreButtons> buttons = new HashMap<>();
    private List<GuiPage> pageIndex = new ArrayList<>();
    private List<GuiList> listInfo = new ArrayList<>();

	private int pageCurrent, pageMax = 0;

	@Override
	public void initGui()
    {
        // Called when the gui should be (re)created.
		if( OresSearch.searchList.isEmpty() )
        {
            // This shouldnt happen. But return if it does.
			System.out.println( "[XRay] Error: searchList is empty inside initGui call!" );
			return;
		}

		this.buttons = new HashMap<>(); // String id for the button. Same as the button text. (Diamond / Iron ect.)
		this.buttonList.clear();
		this.listInfo.clear();
        pageIndex.clear();

        int x = width / 2 - 100, y = height / 2 - 106;
        int Count = 0, Page = 0;

		for( OreInfo ore : OresSearch.searchList )
        {
			if( buttons.get( ore.oreName ) != null )
			{
                // Button already created for this ore.
				buttons.get( ore.oreName ).ores.add( ore ); // Add this new OreInfo to the internal ArrayList for this button
			}
            else
            {
                // Create the new button for this ore.
				int id = Integer.parseInt( Integer.toString( ore.id ) + Integer.toString( ore.meta) );                     // Unique button id. int( str(id) + str(meta) )
                // very hacky... Need to keep an eye on it.
                if( Count % 9 == 0 && Count != 0 )
                {
                    Page++;
                    if( Page > pageMax )
                        pageMax++;

                    x = width / 2 - 100;
                    y = height / 2 - 106;
				}
                GuiButton tmpButton = new GuiButton(id, x+25, y, 160, 20, ore.oreName + ": " + (ore.draw ? "On" : "Off"));
                pageIndex.add(new GuiPage(x, y, Page, tmpButton, ore)); // create new button and set the text to Name: On||Off
                buttons.put( ore.oreName, new OreButtons( ore.oreName, id,  ore ) ); // Add this new button to the buttons hashmap.
				y += 21.8; // Next button should be placed down from this one.

                Count++;
			}
		}

        // only draws the current page
		for (GuiPage page : pageIndex) {
			if (page.getPage() != pageCurrent)
				continue; // skip the ones that are not on this page.

			this.buttonList.add(page.getButton());
			this.listInfo.add( new GuiList( page.x, page.y, page.ore, page.getButton(), page.getButton()) );
		}

		GuiButton aNextButton, aPrevButton;
		this.buttonList.add( new GuiButton(97, (width / 2) - 67, height / 2 + 86, 55, 20, "Add Ore" ) );
		this.buttonList.add( new GuiButton(98, (width / 2) - 10, height / 2 + 86, 82, 20, "Distance: "+ XRay.distStrings[ XRay.currentDist]) ); // Static button for printing the ore dictionary / searchList.
		this.buttonList.add( aNextButton = new GuiButton(-150, width / 2 + 75, height / 2 + 86, 30, 20, ">") );
		this.buttonList.add( aPrevButton = new GuiButton(-151, width / 2 - 100, height / 2 + 86, 30, 20, "<") );

        if( pageMax < 1 )
        {
            aNextButton.enabled = false;
            aPrevButton.enabled = false;
        }

        if( pageCurrent == 0 )
        	aPrevButton.enabled = false;
        
        if( pageCurrent == pageMax )
            aNextButton.enabled = false;
    }
	
	@Override
	public void actionPerformed( GuiButton button )
	{
		// Called on left click of GuiButton
		switch(button.id)
		{
			case 98: // Distance Button
				if (XRay.currentDist < XRay.distNumbers.length - 1)
					XRay.currentDist++;
				else
					XRay.currentDist = 0;
				ConfigHandler.update("searchdist", false);
				break;

			case 97: // New Ore button
				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiBlocks() );
				break;

		  case -150:
			  if( pageCurrent < pageMax )
				  pageCurrent ++;
			  break;

		  case -151:
			  if( pageCurrent > 0 )
				  pageCurrent --;
			  break;

		default:
			for( Map.Entry<String, OreButtons> entry : buttons.entrySet() )
			{
				// Iterate through the buttons map and check what ores need to be toggled
				OreButtons value = entry.getValue();    // OreButtons structure

				if( value.id == button.id )
				{
					// Matched the buttons unique id.
					for( OreInfo tempOre : value.ores )
					{
						// Iterate through the ores that this button should toggle.
						for( OreInfo ore : OresSearch.searchList )
						{
							// Match this ore with the one in the searchList.
							if( (tempOre.id == ore.id) && (tempOre.meta == ore.meta) )
							{
								ore.draw = !ore.draw; // Invert searchList.ore.draw
								ConfigHandler.update( ore.oreName, ore.draw );
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
	public void drawScreen( int x, int y, float f ) {

		super.drawScreen(x, y, f);

		RenderHelper.enableGUIStandardItemLighting();

		for ( GuiList item : this.listInfo ) {
			ItemStack items = new ItemStack(Block.getBlockById( item.ore.getId() ), 64);
			this.itemRender.renderItemAndEffectIntoGUI(items, item.x+2, item.y+2);
		}

		RenderHelper.disableStandardItemLighting();
	}
}