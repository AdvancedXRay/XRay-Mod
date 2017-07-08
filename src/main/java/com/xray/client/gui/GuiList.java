package com.xray.client.gui;

import com.xray.client.OresSearch;
import com.xray.client.gui.helper.HelperGuiList;
import com.xray.client.render.ClientTick;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiList extends GuiContainer
{
	private List<HelperGuiList> listHelper = new ArrayList<>();
	private List<HelperGuiList> renderList = new ArrayList<>();
	private int pageCurrent, pageMax = 0;

	@Override
	public void initGui()
    {
		this.buttonList.clear();
		this.listHelper.clear();
		this.renderList.clear();
		int x = width / 2 - 100, y = height / 2 - 106, count = 0, page = 0;

		for( OreInfo ore : OresSearch.searchList ) {
			if( count % 9 == 0 && count != 0 )
			{
				page++;
				if( page > pageMax )
					pageMax++;

				x = width / 2 - 100;
				y = height / 2 - 106;
			}
			listHelper.add( new HelperGuiList( 10+count, page, x, y, ore) );
			y += 21.8;
			count ++;
		}

        // only draws the current page
		for (HelperGuiList item : listHelper ) {
			if (item.getPageId() != pageCurrent)
				continue; // skip the ones that are not on this page.

			this.renderList.add( item );
			this.buttonList.add( item.getButton() );
		}

		GuiButton aNextButton, aPrevButton;
		this.buttonList.add( new GuiButton(1, (width / 2) - 67, height / 2 + 86, 55, 20, "Add Ore" ) );
		this.buttonList.add( new GuiButton(2, (width / 2) - 10, height / 2 + 86, 82, 20, "Distance: "+ XRay.distStrings[ XRay.currentDist]) ); // Static button for printing the ore dictionary / searchList.
		this.buttonList.add( aNextButton = new GuiButton(2, width / 2 + 75, height / 2 + 86, 30, 20, ">") );
		this.buttonList.add( aPrevButton = new GuiButton(3, width / 2 - 100, height / 2 + 86, 30, 20, "<") );

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
			case 0: // Distance Button
				if (XRay.currentDist < XRay.distNumbers.length - 1)
					XRay.currentDist++;
				else
					XRay.currentDist = 0;
				ConfigHandler.update("searchdist", false);
				break;

			case 1: // New Ore button
				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiBlocks() );
				break;

		  case 2:
			  if( pageCurrent < pageMax )
				  pageCurrent ++;
			  break;

		  case 3:
			  if( pageCurrent > 0 )
				  pageCurrent --;
			  break;

		default:
			for ( HelperGuiList list : this.renderList ) {
				if( list.getButton().id == button.id ) {
					list.getOre().draw = !list.getOre().draw;
					ConfigHandler.update( list.getOre().getOreName(), list.getOre().draw );
					ClientTick.blockFinder( true );
				}
			}
			break;
		}

		this.initGui();
	}


	@Override
	public void mouseClicked( int x, int y, int mouse ) throws IOException
	{
		super.mouseClicked( x, y, mouse );

		for ( HelperGuiList list : this.renderList ) {
			if( list.getButton().mousePressed(this.mc, x, y) ) {
				if( mouse == 1 ) {
					mc.player.closeScreen();
					mc.displayGuiScreen( new GuiEditOre( list.getOre() ) );
				}
			}
		}
	}
	
	@Override
	public void drawScreen( int x, int y, float f ) {

		super.drawScreen(x, y, f);

		RenderHelper.enableGUIStandardItemLighting();
		for ( HelperGuiList item : this.renderList ) {
			ItemStack items = new ItemStack(Block.getBlockById(item.ore.getId()), 64);
			this.itemRender.renderItemAndEffectIntoGUI(items, item.x + 2, item.y + 2);
		}
		RenderHelper.disableStandardItemLighting();
	}
}