package com.xray.gui;

import com.xray.XRay;
import com.xray.gui.manage.GuiAddBlock;
import com.xray.gui.utils.GuiBase;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.BlockItem;
import com.xray.utils.Utils;
import com.xray.xray.Controller;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GuiSelectionScreen extends GuiBase
{
	private Button distButtons;
	private TextFieldWidget search;
	public ItemRenderer render;

	private String lastSearch = "";

	private ArrayList<BlockData> itemList, originalList;
	private GuiActiveBlockList scrollList;

	public GuiSelectionScreen() {
		super(true);
		this.setSideTitle( I18n.format("xray.single.tools") );

		this.itemList = new ArrayList<>(Controller.getBlockStore().getStore().values());
		this.originalList = this.itemList;
	}

	@Override
	public void init()
    {
    	this.render = this.itemRenderer;
		this.buttons.clear();

//		this.scrollList = new GuiActiveBlockList(this, width / 2 - 138, height / 2 - 80, this.itemList);

		this.search = new TextFieldWidget(getFontRender(), width / 2 - 137,  height / 2 - 105, 202, 18, "");
		this.search.setCanLoseFocus(true);

		// side bar buttons
		addButton( new Button((width / 2) + 79, height / 2 - 60, 120, 20, I18n.format("xray.input.add"), button -> {
			getMinecraft().player.closeScreen();
			getMinecraft().displayGuiScreen( new GuiAddBlock(new BlockItem(Block.getStateId(Blocks.GRASS.getDefaultState()), new ItemStack(Blocks.GRASS)), Blocks.GRASS.getDefaultState()) );
		}));
		addButton( new Button(width / 2 + 79, height / 2 - 38, 120, 20, I18n.format("xray.input.add_hand"), button -> {
			getMinecraft().player.closeScreen();
			ItemStack handItem = getMinecraft().player.getHeldItem(Hand.MAIN_HAND);

			// Check if the hand item is a block or not
			if(!(handItem.getItem() instanceof net.minecraft.item.BlockItem)) {
				Utils.sendMessage(getMinecraft().player,"[XRay] "+I18n.format("xray.message.invalid_hand", handItem.getDisplayName().getFormattedText()));
				return;
			}

			BlockState state = Utils.getStateFromPlacement(getMinecraft().world, getMinecraft().player, handItem);
			getMinecraft().displayGuiScreen( new GuiAddBlock( new BlockItem(Block.getStateId(state), handItem), null) );
		}));
		addButton( new Button(width / 2 + 79, height / 2 - 16, 120, 20, I18n.format("xray.input.add_look"), button -> {
			this.onClose();
			try {
				PlayerEntity player = getMinecraft().player;
				Vec3d look = player.getLookVec();
				Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3d end = new Vec3d(player.posX + look.x * 100, player.posY + player.getEyeHeight() + look.y * 100, player.posZ + look.z * 100);

				RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player);
				BlockRayTraceResult result = getMinecraft().world.rayTraceBlocks(context);

				if(result.getType() == RayTraceResult.Type.BLOCK) {
					BlockState state = getMinecraft().world.getBlockState(result.getPos());
					Block lookingAt = getMinecraft().world.getBlockState(result.getPos()).getBlock();

					ItemStack lookingStack = lookingAt.getPickBlock(state, result, getMinecraft().world, result.getPos(), getMinecraft().player);

					getMinecraft().player.closeScreen();
					getMinecraft().displayGuiScreen( new GuiAddBlock( new BlockItem(Block.getStateId(state), lookingStack), state ) );
				}
				else
					Utils.sendMessage(getMinecraft().player, "[XRay] "+I18n.format("xray.message.nothing_infront") );
			}
			catch ( NullPointerException ex ) {
				Utils.sendMessage(getMinecraft().player, "[XRay] "+I18n.format("xray.message.thats_odd") );
			}
		}));

		addButton( distButtons = new Button((width / 2) + 79, height / 2 + 36, 120, 20, I18n.format("xray.input.distance")+": "+ Controller.getRadius(), button -> Controller.incrementCurrentDist()));
		addButton( new Button(width / 2 + 79, height / 2 + 58, 60, 20, I18n.format("xray.single.help"), button -> {
			getMinecraft().player.closeScreen();
			getMinecraft().displayGuiScreen( new GuiHelp() );
		}));
		addButton( new Button((width / 2 + 79) + 62, height / 2 + 58, 59, 20, I18n.format("xray.single.close"), button -> {
			this.onClose();
		}));
    }

	@Override
	public boolean charTyped(char keyTyped, int __unknown) {
		search.charTyped(keyTyped, __unknown);
		updateSearch();
		return super.charTyped(keyTyped, __unknown);
	}

	private void updateSearch() {
		if( search.getText().equals("") ) {
			this.itemList = this.originalList;
//			this.scrollList.setItemList(this.itemList);
			lastSearch = "";
			return;
		}

		if( lastSearch.equals(search.getText()) )
			return;

		this.itemList = this.originalList.stream()
				.filter(b -> b.getEntryName().toLowerCase().contains(search.getText().toLowerCase()))
				.collect(Collectors.toCollection(ArrayList::new));

//		this.scrollList.setItemList(this.itemList);
		lastSearch = search.getText();
	}

	@Override
	public void tick() {
		super.tick();
//		search.updateCursorCounter();
	}

	@Override
	public boolean mouseClicked( double x, double y, int mouse )
	{
		search.mouseClicked(x, y, mouse );

		if( mouse == 1 ) {
			if( distButtons.mouseClicked(x, y, mouse) )
			{
				Controller.decrementCurrentDist();
				distButtons.setMessage(I18n.format("xray.input.distance")+": "+ Controller.getRadius());
			}
		}

		return super.mouseClicked( x, y, mouse );
	}

	@Override
	public void render(int x, int y, float partialTicks) {
		super.render(x, y, partialTicks);

		this.search.render(x, y, partialTicks);
//		this.scrollList.render( x, y, partialTicks );

		if( !search.isFocused() && search.getText().equals(""))
			XRay.mc.fontRenderer.drawStringWithShadow(I18n.format("xray.single.search"), (float) width / 2 - 130, (float) height / 2 - 101, Color.GRAY.getRGB());
	}

	@Override
	public void onClose() {
//		ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		XRay.blockStore.write( Controller.getBlockStore().getStore() );

		Controller.requestBlockFinder( true );
		super.onClose();
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
		return false;
	}

	// todo: reimplement if required
//	@Override
//	public void handleMouseInput() throws IOException {
//		super.handleMouseInput();
//
//		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
//		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//		this.scrollList.handleMouseInput(mouseX, mouseY);
//	}
}