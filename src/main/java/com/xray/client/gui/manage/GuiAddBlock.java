package com.xray.client.gui.manage;

import com.xray.client.gui.GuiSelectionScreen;
import com.xray.client.gui.utils.GuiBase;
import com.xray.client.gui.utils.GuiColorSelect;
import com.xray.client.gui.utils.GuiSlider;
import com.xray.client.xray.Controller;
import com.xray.common.reference.ColorName;
import com.xray.common.reference.block.BlockData;
import com.xray.common.reference.block.BlockItem;
import com.xray.common.utils.OutlineColor;
import com.xray.common.utils.PredefinedColors;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuiAddBlock extends GuiBase {
	private static final int BUTTON_ADD = 98;
	private static final int BUTTON_CANCEL = 99;

	private GuiTextField oreName;
	private GuiSlider redSlider;
	private GuiSlider greenSlider;
	private GuiSlider blueSlider;
	private BlockItem selectBlock;
	private boolean oreNameCleared  = false;
	private IBlockState state;

	private OutlineColor selectedColor = new OutlineColor(0, 0, 0);

	private List<GuiColorSelect> colorSelects = new ArrayList<>();

	public GuiAddBlock(BlockItem selectedBlock, @Nullable IBlockState state) {
		super(false);
		this.selectBlock = selectedBlock;
		this.state = state;
	}

	@Override
	public void initGui()
	{
		// Called when the gui should be (re)created
		this.buttonList.add( new GuiButton( BUTTON_ADD, width / 2 - 100, height / 2 + 85, 128, 20, I18n.format("xray.single.add") ));
		this.buttonList.add( new GuiButton( BUTTON_CANCEL, width / 2 + 30, height / 2 + 85, 72, 20, I18n.format("xray.single.cancel") ) );

		this.buttonList.add(
				redSlider = new GuiSlider( 3, width / 2 - 100, height / 2 + 7, I18n.format("xray.color.red"), 0, 255 )
		);
		this.buttonList.add(
				greenSlider = new GuiSlider( 2, width / 2 - 100, height / 2 + 30, I18n.format("xray.color.green"), 0, 255 )
		);
		this.buttonList.add(
				blueSlider = new GuiSlider( 1, width / 2 - 100, height / 2 + 53, I18n.format("xray.color.blue"), 0, 255 )
		);

		redSlider.sliderValue   = 0.0F;
		greenSlider.sliderValue = 0.654F;
		blueSlider.sliderValue  = 1.0F;

		oreName = new GuiTextField( 1, this.fontRenderer, width / 2 - 100 ,  height / 2 - 70, 202, 20 );
		oreName.setText( this.selectBlock.getItemStack().getDisplayName() );

		int col = 0, row = 0;

		for (ColorName colors: PredefinedColors.getColors()) {
			colorSelects.add(new GuiColorSelect(colors.getName(), colors.getColor(), (width / 2 - 100) + (20 * col), (height / 2 - 15) + (15 * row)));
			col ++;
			if( col > 9 ) {
				col = 0;
				row ++;
			}
		}
	}

	@Override
	public void actionPerformed( GuiButton button )
	{
		switch(button.id)
		{
			case BUTTON_ADD:
				mc.player.closeScreen();

				if( this.state == null )
                	this.state = Block.getBlockFromItem(this.selectBlock.getItemStack().getItem()).getDefaultState();

				// Push the block to the render stack
				Controller.getBlockStore().put(
					this.state.toString(),

					new BlockData(
						this.state.getBlock().getRegistryName(),
						oreName.getText(),
						this.selectedColor,
						this.state.getBlock().getDefaultState() == this.state,
						this.state,
						selectBlock.getItemStack(),
						true
					)
				);

				mc.displayGuiScreen( new GuiSelectionScreen() );

				break;

			case BUTTON_CANCEL:
				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiSelectionScreen() );
				break;

			default:
				break;
		}
	}

	@Override
	protected void keyTyped( char par1, int par2 ) throws IOException // par1 is char typed, par2 is ascii hex (tab=15 return=28)
	{
		super.keyTyped( par1, par2 );

		if( oreName.isFocused() )
			oreName.textboxKeyTyped( par1, par2 );
		else
		{
			// Change focus to oreName on focus-less tab
			if (par2 == 15) {
				if (!oreNameCleared)
					oreName.setText("");
				oreName.setFocused(true);
			}
		}
	}

	@Override
	public void updateScreen()
	{
		oreName.updateCursorCounter();
	}

	@Override
	public void drawScreen( int x, int y, float f )
	{
		super.drawScreen(x, y, f);
		getFontRender().drawStringWithShadow(selectBlock.getItemStack().getDisplayName(), width / 2f - 100, height / 2f - 90, 0xffffff);

		oreName.drawTextBox();

		renderPreview(width / 2 - 100, height / 2 - 40, redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue);

		RenderHelper.enableGUIStandardItemLighting();
		this.itemRender.renderItemAndEffectIntoGUI( selectBlock.getItemStack(), width / 2 + 85, height / 2 - 105 );
		RenderHelper.disableStandardItemLighting();

		// Color select
		getFontRender().drawStringWithShadow(I18n.format("xray.gui.select_color"), width / 2f - 100, height / 2f - 35, 0xffffff);

		for (GuiColorSelect select: this.colorSelects)
			select.drawSelect();

		for (GuiColorSelect select: this.colorSelects) {
			if( select.isMouseOver(x, y) )
				this.drawHoveringText( select.getName(), x, y );
		}
	}

	static void renderPreview(int x, int y, float r, float g, float b) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellate = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(r, g, b, 1);
		tessellate.begin(7, DefaultVertexFormats.POSITION);
		tessellate.pos(x, y, 0.0D).endVertex();
		tessellate.pos(x, y + 45, 0.0D).endVertex();
		tessellate.pos(x + 202, y + 45, 0.0D).endVertex();
		tessellate.pos(x+ 202, y, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	@Override
	public void mouseClicked( int x, int y, int mouse ) throws IOException
	{
		super.mouseClicked( x, y, mouse );
		oreName.mouseClicked( x, y, mouse );

		if( oreName.isFocused() && !oreNameCleared )
		{
			oreName.setText( "" );
			oreNameCleared = true;
		}

		if( !oreName.isFocused() && oreNameCleared && Objects.equals(oreName.getText(), ""))
		{
			oreNameCleared = false;
			oreName.setText( I18n.format("xray.input.gui") );
		}

		if( mouse == 0 ) {
			for (GuiColorSelect select : this.colorSelects) {
				if (select.isMouseOver(x, y)) {
					this.selectedColor = select.getColor();
					break;
				}
			}
		}
	}

	@Override
	public boolean hasTitle() {
		return true;
	}

	@Override
	public String title() {
		return I18n.format("xray.title.config");
	}
}
