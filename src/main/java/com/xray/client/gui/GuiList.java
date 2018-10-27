package com.xray.client.gui;

import com.xray.client.xray.XrayController;
import com.xray.client.gui.helper.HelperGuiList;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.BlockData;
import com.xray.common.reference.OreInfo;
import com.xray.common.reference.OutlineColor;
import com.xray.common.reference.Reference;
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
import java.util.Map;

public class GuiList extends GuiContainer
{
	private final List<HelperGuiList> listHelper = new ArrayList<>();
	private final List<HelperGuiList> renderList = new ArrayList<>();
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

	public GuiList() {
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

		for( OreInfo ore : XrayController.searchList.getOres() ) {
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
		this.buttonList.add( distButtons = new GuiButton(BUTTON_RADIUS, (width / 2) - 108, height / 2 + 86, 140, 20, I18n.format("xray.input.distance")+": "+ XrayController.getRadius()) );
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
				XrayController.incrementCurrentDist();
				break;

			case BUTTON_ADD_BLOCK:
				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiBlocks() );
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
//				XrayController.toggleDrawCaves();
//				XRay.logger.debug( "Draw caves: " + XrayController.drawCaves() );
				XrayController.blockStore.store.clear();
				break;

			case BUTTON_ADD_HAND:
				mc.player.closeScreen();
				ItemStack handItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
				OreInfo handBlock = null;
				// Check if the hand item is a block or not
				if(!(handItem.getItem() instanceof ItemBlock)) {
					mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.invalid_hand", handItem.getDisplayName()) ));
					return;
				}
				handBlock = new OreInfo( handItem );
				mc.displayGuiScreen( new GuiAdd(handBlock) );
				break;

			case BUTTON_ADD_LOOK:
				mc.player.closeScreen();
				try {
					RayTraceResult ray = mc.player.rayTrace(100, 20);
					if( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK ) {
						IBlockState state = mc.world.getBlockState(ray.getBlockPos());
						Block lookingAt = mc.world.getBlockState(ray.getBlockPos()).getBlock();

						XrayController.blockStore.putBlock(
							state.getBlock().getLocalizedName(),
							new BlockData(state.getBlock().getRegistryName(), new OutlineColor(0, 0, 0), state.getBlock().getDefaultState() == state, state)
						);

						System.out.println(String.format("Block[%s] added", state.getBlock().getLocalizedName()));
						System.out.println("-> [Printing Block Store]");
						for (Map.Entry<String, List<BlockData>> data:
							 XrayController.blockStore.store.entrySet()) {

						    System.out.println(String.format("-> [%s]", data.getKey()));
						    for (BlockData block : data.getValue() ) {
							    System.out.println(String.format("---> [%b, %s, %s, %s]", block.isDefault(), block.getState().toString(), block.getOutline().getBlue(), block.getName()));
                            }
						}
						ItemStack lookingStack = lookingAt.getPickBlock(state, ray, mc.world, ray.getBlockPos(), mc.player);
//						OreInfo seeBlock = null;

//						// Double super check that we've got ourselves a block
//						if(!(lookingStack.getItem() instanceof ItemBlock)) {
//							mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.invalid_hand", lookingStack.getDisplayName()) ));
//							return;
//						}
//						seeBlock = new OreInfo( lookingStack );
//						mc.displayGuiScreen( new GuiAdd(seeBlock) );

						mc.player.closeScreen();
					}
					else
						mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.nothing_infront") ));
				}
				catch ( NullPointerException ex ) {
					mc.player.sendMessage( new TextComponentString( "[XRay] "+I18n.format("xray.message.thats_odd") ));
				}

				break;

			case BUTTON_CLOSE:
				mc.player.closeScreen();
				break;

			default:
				for ( HelperGuiList list : this.renderList ) {
					if( list.getButton().id == button.id ) {
						XrayController.searchList.toggleOreDrawable(list.getOre()); // no need to update list.getOre() as it is referenced in searchList
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
			for (HelperGuiList list : this.renderList) {
				if (list.getButton().mousePressed(this.mc, x, y)) {
					mc.player.closeScreen();
					mc.displayGuiScreen(new GuiEditOre(list.getOre()));
				}
			}

			if( distButtons.mousePressed(this.mc, x, y) )
			{
				XrayController.decrementCurrentDist();
				distButtons.displayString = I18n.format("xray.input.distance")+": "+ XrayController.getRadius();
			}
		}
	}

	@Override
	public void drawScreen( int x, int y, float f ) {

		super.drawScreen(x, y, f);

		RenderHelper.enableGUIStandardItemLighting();
		for ( HelperGuiList item : this.renderList ) {
			try {
				this.itemRender.renderItemAndEffectIntoGUI( item.getOre().getItemStack(), item.x + 2, item.y + 2 );
				this.renderColor(item.x, item.y, item.getOre().getColor());
			} catch ( Exception ignored ) {
			}
		}
		RenderHelper.disableStandardItemLighting();
	}

	private void renderColor(int x, int y, int[] color) {
		mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "circle.png"));
		GuiContainer.drawTexturedQuadFit(x, y, 8, 8, color);
	}

	@Override
	public void onGuiClosed()
	{
		// First, save all changes made to the config
		ConfigHandler.syncConfig();
		XRay.config.save();

		// And force a scan
		XrayController.requestBlockFinder( true );
	}
}