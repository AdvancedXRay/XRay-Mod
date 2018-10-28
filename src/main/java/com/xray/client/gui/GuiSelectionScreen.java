package com.xray.client.gui;

import com.xray.client.gui.manage.GuiAddBlock;
import com.xray.client.gui.manage.GuiBlockListScrollable;
import com.xray.client.gui.manage.GuiEdit;
import com.xray.client.gui.utils.GuiBase;
import com.xray.client.xray.XRayController;
import com.xray.client.gui.utils.GuiPaged;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.*;
import com.xray.common.reference.block.BlockItem;
import com.xray.common.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSelectionScreen extends GuiBase
{
	private final List<GuiPaged> listHelper = new ArrayList<>();
	private final List<GuiPaged> renderList = new ArrayList<>();
	private int pageCurrent, pageMax = 0;

	private GuiButton distButtons;
	private static final int BUTTON_RADIUS = 0;
	private static final int BUTTON_NEXT = 2;
	private static final int BUTTON_PREVIOUS = 3;
	private static final int BUTTON_ADD_BLOCK = 1;
	private static final int BUTTON_ADD_HAND = 4;
	private static final int BUTTON_ADD_LOOK = 5;
	private static final int BUTTON_CAVE_FINDER = 1500;
	private static final int BUTTON_CLOSE = 6;

	public GuiSelectionScreen() {
		super(true);
		this.setSideTitle( I18n.format("xray.single.tools") );
	}

	@Override
	public void initGui()
    {
		this.buttonList.clear();
		this.listHelper.clear();
		this.renderList.clear();
		int x = width / 2 - 140, y = height / 2 - 106, count = 0, page = 0;

		for( OreInfo ore : XRayController.searchList.getOres() ) {
			if( count % 9 == 0 && count != 0 )
			{
				page++;
				if( page > pageMax )
					pageMax++;

				x = width / 2 - 140;
				y = height / 2 - 106;
			}
			listHelper.add( new GuiPaged( 10+count, page, x, y, ore) );
			y += 21.8;
			count ++;
		}

        // only draws the current page
		for (GuiPaged item : listHelper ) {
			if (item.getPageId() != pageCurrent)
				continue; // skip the ones that are not on this page.

			this.renderList.add( item );
			this.buttonList.add( item.getButton() );
		}

		GuiButton aNextButton, aPrevButton;
		this.buttonList.add( distButtons = new GuiButton(BUTTON_RADIUS, (width / 2) - 108, height / 2 + 86, 140, 20, I18n.format("xray.input.distance")+": "+ XRayController.getRadius()) );
		this.buttonList.add( aNextButton = new GuiButton(BUTTON_NEXT, width / 2 + 35, height / 2 + 86, 30, 20, ">") );
		this.buttonList.add( aPrevButton = new GuiButton(BUTTON_PREVIOUS, width / 2 - 140, height / 2 + 86, 30, 20, "<") );

		// side bar buttons
		this.buttonList.add( new GuiButton(BUTTON_ADD_BLOCK, (width / 2) + 78, height / 2 - 60, 120, 20, I18n.format("xray.input.add") ) );
		this.buttonList.add( new GuiButton(BUTTON_ADD_HAND, width / 2 + 78, height / 2 - 38, 120, 20, I18n.format("xray.input.add_hand") ) );
		this.buttonList.add( new GuiButton(BUTTON_ADD_LOOK, width / 2 + 78, height / 2 - 16, 120, 20, I18n.format("xray.input.add_look") ) );
//		this.buttonList.add( new GuiButton(BUTTON_CAVE_FINDER, width / 2 + 78, height / 2 + 5, 120, 20, "Cave Finder") );
		this.buttonList.add( new GuiButton(BUTTON_CAVE_FINDER, width / 2 + 78, height / 2 + 5, 120, 20, "Clear block List") );
		this.buttonList.add( new GuiButton(BUTTON_CLOSE, width / 2 + 78, height / 2 + 58, 120, 20, I18n.format("xray.single.close") ) );

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
			case BUTTON_RADIUS:
				XRayController.incrementCurrentDist();
				break;

			case BUTTON_ADD_BLOCK:
				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiBlockListScrollable() );
				break;

			case BUTTON_NEXT:
				if( pageCurrent < pageMax )
					pageCurrent ++;
				break;

			case BUTTON_PREVIOUS:
				if( pageCurrent > 0 )
			  		pageCurrent --;
				break;

			case BUTTON_CAVE_FINDER:
//				mc.player.closeScreen();
//				XRayController.toggleDrawCaves();
//				XRay.logger.debug( "Draw caves: " + XRayController.drawCaves() );
				XRayController.blockStore.store.clear();
				XRayController.blockStore.defaultStore.clear();
				break;

			case BUTTON_ADD_HAND:
				mc.player.closeScreen();
				ItemStack handItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);

				// Check if the hand item is a block or not
				if(!(handItem.getItem() instanceof ItemBlock)) {
					mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.invalid_hand", handItem.getDisplayName()) ));
					return;
				}

				// Fake placement for correct meta
				// Might not work on things like a chest...
				IBlockState iBlockState = Utils.getStateFromPlacement(this.mc.world, this.mc.player, handItem);
				mc.displayGuiScreen( new GuiAddBlock( new BlockItem(Block.getStateId(iBlockState), handItem)) );
				break;

			case BUTTON_ADD_LOOK:
				mc.player.closeScreen();
				try {
					RayTraceResult ray = mc.player.rayTrace(100, 20);
					if( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK ) {
						IBlockState state = mc.world.getBlockState(ray.getBlockPos());
						Block lookingAt = mc.world.getBlockState(ray.getBlockPos()).getBlock();

						ItemStack lookingStack = lookingAt.getPickBlock(state, ray, mc.world, ray.getBlockPos(), mc.player);

						mc.player.closeScreen();
						mc.displayGuiScreen( new GuiAddBlock( new BlockItem(Block.getStateId(state), lookingStack) ) );
					}
					else
                        Utils.sendMessage(mc.player, "[XRay] "+I18n.format("xray.message.nothing_infront") );
				}
				catch ( NullPointerException ex ) {
                    Utils.sendMessage(mc.player, "[XRay] "+I18n.format("xray.message.thats_odd") );
				}

				break;

			case BUTTON_CLOSE:
				mc.player.closeScreen();
				break;

			default:
				for ( GuiPaged list : this.renderList ) {
					if( list.getButton().id == button.id ) {
						XRayController.searchList.toggleOreDrawable(list.getOre()); // no need to update list.getOre() as it is referenced in searchList
					}
				}
		}

		this.initGui();
	}


	@Override
	public void mouseClicked( int x, int y, int mouse ) throws IOException
	{
		super.mouseClicked( x, y, mouse );

		if( mouse == 1 ) {
			for (GuiPaged list : this.renderList) {
				if (list.getButton().mousePressed(this.mc, x, y)) {
					mc.player.closeScreen();
					mc.displayGuiScreen(new GuiEdit(list.getOre()));
				}
			}

			if( distButtons.mousePressed(this.mc, x, y) )
			{
				XRayController.decrementCurrentDist();
				distButtons.displayString = I18n.format("xray.input.distance")+": "+ XRayController.getRadius();
			}
		}
	}

	@Override
	public void drawScreen( int x, int y, float f ) {

		super.drawScreen(x, y, f);

		RenderHelper.enableGUIStandardItemLighting();
		for ( GuiPaged item : this.renderList ) {
			try {
				this.renderColor(item.x, item.y, item.getOre().getColor());
				this.itemRender.renderItemAndEffectIntoGUI( item.getOre().getItemStack(), item.x + 2, item.y + 2 );
			} catch ( Exception ignored ) {
			    // If this fails it's not the end of the world
			}
		}
		RenderHelper.disableStandardItemLighting();
	}

	private void renderColor(int x, int y, int[] color) {
		mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "circle.png"));
		GuiBase.drawTexturedQuadFit(x, y, 8, 8, color);
	}

	@Override
	public void onGuiClosed()
	{
		// First, save all changes made to the config
		ConfigHandler.syncConfig();
		XRay.config.save();

		// And force a scan
		XRayController.requestBlockFinder( true );
	}
}