package com.xray.client.gui;

import com.sun.istack.internal.NotNull;
import com.xray.client.gui.helper.HelperBlock;
import com.xray.client.gui.helper.HelperGuiList;
import com.xray.client.render.ClientTick;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.Sys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiList extends GuiContainer
{
	private List<HelperGuiList> listHelper = new ArrayList<>();
	private List<HelperGuiList> renderList = new ArrayList<>();
	private int pageCurrent, pageMax = 0;

	private GuiButton distButtons;

	public GuiList() {
		super(true);
		setSideTitle("Tools");
	}

	@Override
	public void initGui()
    {
		this.buttonList.clear();
		this.listHelper.clear();
		this.renderList.clear();
		int x = width / 2 - 140, y = height / 2 - 106, count = 0, page = 0;

		for( OreInfo ore : XRay.searchList ) {
			if( count % 9 == 0 && count != 0 )
			{
				page++;
				if( page > pageMax )
					pageMax++;

				x = width / 2 - 140;
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
		this.buttonList.add( distButtons = new GuiButton(0, (width / 2) - 108, height / 2 + 86, 140, 20, I18n.format("xray.input.distance")+": "+ XRay.distStrings[ XRay.currentDist]) ); // Static button for printing the ore dictionary / searchList.
		this.buttonList.add( aNextButton = new GuiButton(2, width / 2 + 35, height / 2 + 86, 30, 20, ">") );
		this.buttonList.add( aPrevButton = new GuiButton(3, width / 2 - 140, height / 2 + 86, 30, 20, "<") );

		// side bar buttons
		this.buttonList.add( new GuiButton(1, (width / 2) + 78, height / 2 - 60, 120, 20, I18n.format("xray.input.add") ) );
		this.buttonList.add( new GuiButton(4, width / 2 + 78, height / 2 - 38, 120, 20, I18n.format("xray.input.add_hand") ) );
		this.buttonList.add( new GuiButton(5, width / 2 + 78, height / 2 - 16, 120, 20, I18n.format("xray.input.add_looking") ) );

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

				ClientTick.blockFinder( true );
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

			case 4:
				mc.player.closeScreen();
				ItemStack handItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
				// Check if the hand item is a block or not
				if(!(handItem.getItem() instanceof ItemBlock)) {
					mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.invalid_hand", handItem.getDisplayName()) ));
					return;
				}

				// create a selected block from main hand item
				HelperBlock handBlock = new HelperBlock(handItem.getDisplayName(),
						Block.getBlockFromItem(handItem.getItem()),
						handItem,
						handItem.getItem(),
						handItem.getItem().getRegistryName());

				mc.displayGuiScreen( new GuiAdd(handBlock) );
				break;

			case 5:
				mc.player.closeScreen();
				try {
					RayTraceResult ray = mc.player.rayTrace(100, 20);
					if( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK ) {
						IBlockState state = mc.world.getBlockState(ray.getBlockPos());
						Block lookingAt = mc.world.getBlockState(ray.getBlockPos()).getBlock();

						ItemStack lookingStack = lookingAt.getPickBlock(state, ray, mc.world, ray.getBlockPos(), mc.player);

						// Double super check that we've got ourselves a block
						if(!(lookingStack.getItem() instanceof ItemBlock)) {
							mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.invalid_hand", lookingStack.getDisplayName()) ));
							return;
						}

						// create a selected block from main hand item
						HelperBlock seeBlock = new HelperBlock(lookingStack.getDisplayName(),
								Block.getBlockFromItem(lookingStack.getItem()),
								lookingStack,
								lookingStack.getItem(),
								lookingStack.getItem().getRegistryName());

						mc.displayGuiScreen( new GuiAdd(seeBlock) );
					}
					else
						mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.nothing_infront") ));
				}
				catch ( NullPointerException ex ) {
					mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.thats_odd") ));
				}

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

		if( mouse == 1 ) {
			for (HelperGuiList list : this.renderList) {
				if (list.getButton().mousePressed(this.mc, x, y)) {
					mc.player.closeScreen();
					mc.displayGuiScreen(new GuiEditOre(list.getOre()));
				}
			}

			if( distButtons.mousePressed(this.mc, x, y) ) {

				if (XRay.currentDist > 0)
					XRay.currentDist--;
				else
					XRay.currentDist = XRay.distNumbers.length - 1;

				distButtons.displayString = I18n.format("xray.input.distance")+": "+ XRay.distStrings[ XRay.currentDist];
				ClientTick.blockFinder( true );
				ConfigHandler.update("searchdist", false);
			}
		}
	}
	
	@Override
	public void drawScreen( int x, int y, float f ) {

		super.drawScreen(x, y, f);

		RenderHelper.enableGUIStandardItemLighting();
		for ( HelperGuiList item : this.renderList ) {
			NonNullList<ItemStack> tmpStack = NonNullList.create();
			Block tmpBlock = Block.getBlockById(item.ore.getId());
			tmpBlock.getSubBlocks(tmpBlock.getCreativeTabToDisplayOn(), tmpStack);

			try {
				this.itemRender.renderItemAndEffectIntoGUI(tmpStack.get(item.ore.getMeta()), item.x + 2, item.y + 2);
			} catch ( IndexOutOfBoundsException ignored ) {
			}
		}
		RenderHelper.disableStandardItemLighting();
	}
}