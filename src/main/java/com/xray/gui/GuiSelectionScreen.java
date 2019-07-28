package com.xray.gui;

import com.xray.XRay;
import com.xray.gui.manage.GuiAddBlock;
import com.xray.gui.manage.GuiBlockListScrollable;
import com.xray.gui.utils.GuiBase;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.BlockItem;
import com.xray.utils.Utils;
import com.xray.xray.Controller;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GuiSelectionScreen extends GuiBase
{
	private int pageCurrent, pageMax = 0;

	private GuiButton distButtons;
	private GuiTextField search;
	public RenderItem render;

	private String lastSearch = "";

	private static final int BUTTON_RADIUS = 0;
	private static final int BUTTON_NEXT = 2;
	private static final int BUTTON_PREVIOUS = 3;
	private static final int BUTTON_ADD_BLOCK = 1;
	private static final int BUTTON_ADD_HAND = 4;
	private static final int BUTTON_ADD_LOOK = 5;
	private static final int BUTTON_HELP = 6;
	private static final int BUTTON_CLOSE = 7;

	private ArrayList<BlockData> itemList, originalList;
	private GuiActiveBlockList scrollList;

	public GuiSelectionScreen() {
		super(true);
		this.setSideTitle( I18n.format("xray.single.tools") );

		this.itemList = new ArrayList<>(Controller.getBlockStore().getStore().values());
		this.originalList = this.itemList;
	}

	@Override
	public void initGui()
    {
    	this.render = this.itemRender;
		this.buttonList.clear();

		this.scrollList = new GuiActiveBlockList(this, width / 2 - 140, height / 2 - 80, this.itemList);

		this.search = new GuiTextField(150, getFontRender(), width / 2 - 140,  height / 2 - 106, 200, 18);
		this.search.setCanLoseFocus(true);

		GuiButton aNextButton, aPrevButton;
		this.buttonList.add( distButtons = new GuiButton(BUTTON_RADIUS, (width / 2) - 108, height / 2 + 86, 140, 20, I18n.format("xray.input.distance")+": "+ Controller.getRadius()) );
		this.buttonList.add( aNextButton = new GuiButton(BUTTON_NEXT, width / 2 + 35, height / 2 + 86, 30, 20, ">") );
		this.buttonList.add( aPrevButton = new GuiButton(BUTTON_PREVIOUS, width / 2 - 140, height / 2 + 86, 30, 20, "<") );

		// side bar buttons
		this.buttonList.add( new GuiButton(BUTTON_ADD_BLOCK, (width / 2) + 78, height / 2 - 60, 120, 20, I18n.format("xray.input.add") ) );
		this.buttonList.add( new GuiButton(BUTTON_ADD_HAND, width / 2 + 78, height / 2 - 38, 120, 20, I18n.format("xray.input.add_hand") ) );
		this.buttonList.add( new GuiButton(BUTTON_ADD_LOOK, width / 2 + 78, height / 2 - 16, 120, 20, I18n.format("xray.input.add_look") ) );

		this.buttonList.add( new GuiButton(BUTTON_HELP, width / 2 + 78, height / 2 + 38, 120, 20, I18n.format("xray.single.help") ) );
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
				Controller.incrementCurrentDist();
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

			case BUTTON_ADD_HAND:
				mc.player.closeScreen();
				ItemStack handItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);

				// Check if the hand item is a block or not
				if(!(handItem.getItem() instanceof ItemBlock)) {
					Utils.sendMessage(mc.player,"[XRay] "+I18n.format("xray.message.invalid_hand", handItem.getDisplayName()));
					return;
				}

				// Fake placement for correct meta
				// Might not work on things like a chest...
				IBlockState iBlockState = Utils.getStateFromPlacement(this.mc.world, this.mc.player, handItem);
				mc.displayGuiScreen( new GuiAddBlock( new BlockItem(Block.getStateId(iBlockState), handItem), null) );
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
						mc.displayGuiScreen( new GuiAddBlock( new BlockItem(Block.getStateId(state), lookingStack), state ) );
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
		}

		this.initGui();
	}

	@Override
	protected void keyTyped( char charTyped, int hex ) throws IOException
	{
		super.keyTyped(charTyped, hex);
		search.textboxKeyTyped(charTyped, hex);

		updateSearch();
	}

	private void updateSearch() {
		if( search.getText().equals("") ) {
			this.itemList = this.originalList;
			this.scrollList.setItemList(this.itemList);
			return;
		}

		if( lastSearch.equals(search.getText()) )
			return;

		this.itemList = this.originalList.stream()
				.filter(b -> b.getEntryName().toLowerCase().contains(search.getText().toLowerCase()))
				.collect(Collectors.toCollection(ArrayList::new));

		this.scrollList.setItemList(this.itemList);
	}

	@Override
	public void updateScreen()
	{
		search.updateCursorCounter();
	}

	@Override
	public void mouseClicked( int x, int y, int mouse ) throws IOException
	{
		super.mouseClicked( x, y, mouse );
		search.mouseClicked(x, y, mouse );

		if( mouse == 1 ) {
			if( distButtons.mousePressed(this.mc, x, y) )
			{
				Controller.decrementCurrentDist();
				distButtons.displayString = I18n.format("xray.input.distance")+": "+ Controller.getRadius();
			}
		}
	}

	@Override
	public void drawScreen( int x, int y, float f ) {

		super.drawScreen(x, y, f);

		this.search.drawTextBox();
		this.scrollList.drawScreen( x, y, f );

		if( !search.isFocused() && search.getText().equals(""))
			XRay.mc.fontRenderer.drawStringWithShadow(I18n.format("xray.single.search"), (float) width / 2 - 130, (float) height / 2 - 101, Color.GRAY.getRGB());
	}

	@Override
	public void onGuiClosed()
	{
		ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		XRay.blockStore.write( Controller.getBlockStore().getStore() );

		Controller.requestBlockFinder( true );
	}
}