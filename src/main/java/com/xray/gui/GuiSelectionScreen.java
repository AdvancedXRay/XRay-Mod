package com.xray.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xray.Configuration;
import com.xray.XRay;
import com.xray.gui.manage.GuiAddBlock;
import com.xray.gui.manage.GuiBlockList;
import com.xray.gui.manage.GuiEdit;
import com.xray.gui.utils.GuiBase;
import com.xray.gui.utils.ScrollingList;
import com.xray.gui.utils.SupportButton;
import com.xray.store.BlockStore;
import com.xray.utils.BlockData;
import com.xray.xray.Controller;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSelectionScreen extends GuiBase {
    private static final ResourceLocation CIRCLE = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    private Button distButtons;
    private TextFieldWidget search;
    public ItemRenderer render;

    private String lastSearch = "";

    private ArrayList<BlockData> itemList, originalList;
    private ScrollingBlockList scrollList;

    public GuiSelectionScreen() {
        super(true);
        this.setSideTitle(I18n.format("xray.single.tools"));

        // Inject this hear as everything is loaded
        if( XRay.blockStore.created ) {
            List<BlockData.SerializableBlockData> blocks = XRay.blockStore.populateDefault();
            Controller.getBlockStore().setStore(BlockStore.getFromSimpleBlockList(blocks));

            XRay.blockStore.created = false;
        }

        this.itemList = new ArrayList<>(Controller.getBlockStore().getStore().values());
        this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));

        this.originalList = this.itemList;
    }

    @Override // @mcp: func_231160_c_ = init
    public void func_231160_c_() {
        if( getMinecraft().player == null )
            return;

        this.render = this.field_230707_j_; // @mcp: field_230707_j_ = itemRender
        this.field_230710_m_.clear(); // @mcp: field_230710_m_ = buttons

		this.scrollList = new ScrollingBlockList((getWidth() / 2) - 37, getHeight() / 2 + 10, 203, 185, this.itemList);
        this.field_230705_e_.add(this.scrollList); // @mcp: field_230705_e_ = children

        this.search = new TextFieldWidget(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, StringTextComponent.field_240750_d_); // @mcp: field_240750_d_ = empty
        this.search.setCanLoseFocus(true);

        // side bar buttons
        addButton(new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 - 60, 120, 20, I18n.format("xray.input.add"), "xray.tooltips.add_block", button -> {
            getMinecraft().player.closeScreen();
            getMinecraft().displayGuiScreen(new GuiBlockList());
        }));
        addButton(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 38, 120, 20, I18n.format("xray.input.add_hand"), "xray.tooltips.add_block_in_hand", button -> {
            getMinecraft().player.closeScreen();
            ItemStack handItem = getMinecraft().player.getHeldItem(Hand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof net.minecraft.item.BlockItem)) {
                getMinecraft().player.sendStatusMessage(new StringTextComponent("[XRay] " + I18n.format("xray.message.invalid_hand", handItem.getDisplayName().getString())), false); // FIXME: 28/06/2020 Might be broke
                return;
            }

            getMinecraft().displayGuiScreen(new GuiAddBlock(((BlockItem) handItem.getItem()).getBlock()));
        }));
        addButton(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 16, 120, 20, I18n.format("xray.input.add_look"), "xray.tooltips.add_block_looking_at", button -> {
            PlayerEntity player = getMinecraft().player;
            if( getMinecraft().world == null || player == null )
                return;

            this.onClose();
            try {
                Vector3d look = player.getLookVec(); // @mcp: func_233580_cy_ = getPosition (blockPos)
                Vector3d start = new Vector3d(player.func_233580_cy_().getX(), player.func_233580_cy_().getY() + player.getEyeHeight(), player.func_233580_cy_().getZ());
                Vector3d end = new Vector3d(player.func_233580_cy_().getX() + look.x * 100, player.func_233580_cy_().getY() + player.getEyeHeight() + look.y * 100, player.func_233580_cy_().getZ() + look.z * 100);

                RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player);
                BlockRayTraceResult result = getMinecraft().world.rayTraceBlocks(context);

                if (result.getType() == RayTraceResult.Type.BLOCK) {
                    BlockState state = getMinecraft().world.getBlockState(result.getPos());
                    Block lookingAt = getMinecraft().world.getBlockState(result.getPos()).getBlock();

                    ItemStack lookingStack = lookingAt.getPickBlock(state, result, getMinecraft().world, result.getPos(), getMinecraft().player);

                    player.closeScreen();
                    getMinecraft().displayGuiScreen(new GuiAddBlock(Block.getBlockFromItem(lookingStack.getItem())));
                } else
                    player.sendStatusMessage(new StringTextComponent("[XRay] " + I18n.format("xray.message.nothing_infront")), false);
            } catch (NullPointerException ex) {
                player.sendStatusMessage(new StringTextComponent("[XRay] " + I18n.format("xray.message.thats_odd")), false);
            }
        }));

        addButton(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 6, 120, 20, I18n.format("xray.input.show-lava", Controller.isLavaActive()), "xray.tooltips.show_lava", button -> {
            Controller.toggleLava();
            button.func_238482_a_(new TranslationTextComponent("xray.input.show-lava", Controller.isLavaActive())); // @mcp: func_238482_a_  = setMessage
        }));

        addButton(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 36, 120, 20, I18n.format("xray.input.distance", Controller.getRadius()), "xray.tooltips.distance", button -> {
            Controller.incrementCurrentDist();
            button.func_238482_a_(new TranslationTextComponent("xray.input.distance", Controller.getRadius())); // @mcp: func_238482_a_  = setMessage
        }));
        addButton(new Button(getWidth() / 2 + 79, getHeight() / 2 + 58, 60, 20, new TranslationTextComponent("xray.single.help"), button -> {
            getMinecraft().player.closeScreen();
            getMinecraft().displayGuiScreen(new GuiHelp());
        }));
        addButton(new Button((getWidth() / 2 + 79) + 62, getHeight() / 2 + 58, 59, 20, new TranslationTextComponent("xray.single.close"), button -> {
            this.onClose();
        }));
    }

    private void updateSearch() {
        if (lastSearch.equals(search.getText()))
            return;

        if (search.getText().equals("")) {
            this.itemList = this.originalList;
			this.scrollList.updateEntries(this.itemList);
            lastSearch = "";
            return;
        }

        this.itemList = this.originalList.stream()
                .filter(b -> b.getEntryName().toLowerCase().contains(search.getText().toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));

		this.scrollList.updateEntries(this.itemList);
        lastSearch = search.getText();
    }

    @Override // @mcp: func_231023_e_ = tick
    public void func_231023_e_() {
        super.func_231023_e_();
        search.tick();

        updateSearch();
    }

    @Override // @mcp: func_231044_a_ = mouseClicked
    public boolean func_231044_a_(double x, double y, int mouse) {
        if( search.func_231044_a_(x, y, mouse) )
            this.func_231035_a_(search); // @mcp: func_231035_a_ = setFocused

        if (mouse == 1 && distButtons.func_231047_b_(x, y)) { // @mcp: func_231047_b_  = isMouseOver
            Controller.decrementCurrentDist();
            distButtons.func_238482_a_(new TranslationTextComponent("xray.input.distance", Controller.getRadius())); // @mcp: func_238482_a_ = setMessage
            distButtons.func_230988_a_(Minecraft.getInstance().getSoundHandler()); // @mcp: func_230988_a_ = PlayDownSound
        }

        return super.func_231044_a_(x, y, mouse);
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        this.search.func_230430_a_(stack, x, y, partialTicks); // @mcp: func_230430_a_ = render
        this.scrollList.func_230430_a_(stack, x, y, partialTicks ); // @mcp: func_230430_a_ = render
        // @mcp: func_238405_a_ = drawStringWithShadow
        if (!search.func_230999_j_() && search.getText().equals("")) // @mcp: func_230999_j_  = isFocused
            XRay.mc.fontRenderer.func_238405_a_(stack, I18n.format("xray.single.search"), (float) getWidth() / 2 - 130, (float) getHeight() / 2 - 101, Color.GRAY.getRGB());
    }

    @Override
    public void onClose() {
        Configuration.store.radius.save();
        XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

        Controller.requestBlockFinder(true);
        super.onClose();
    }

    static final class SupportButtonInner extends SupportButton {
        public SupportButtonInner(int widthIn, int heightIn, int width, int height, String text, String i18nKey, IPressable onPress) {
            super(widthIn, heightIn, width, height, new StringTextComponent(text), new TranslationTextComponent(i18nKey), onPress);
        }
    }

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockData> blocks) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
        }

        public void setSelected(@Nullable BlockSlot entry, int mouse) {
            if (entry == null)
                return;

            if( GuiSelectionScreen.func_231173_s_() ) { // @mcp: func_231173_s_  = hasShiftDown
                XRay.mc.player.closeScreen();
                XRay.mc.displayGuiScreen( new GuiEdit(entry.block) );
                return;
            }

            Controller.getBlockStore().toggleDrawing(entry.block);
            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
        }

        void updateEntries(List<BlockData> blocks) {
            this.func_230963_j_ (); // @mcp: func_230963_j_  = clearEntries
            blocks.forEach(block -> this.func_230513_b_(new BlockSlot(block, this))); // @mcp: func_230513_b_ = addEntry
        }

        public static class BlockSlot extends AbstractList.AbstractListEntry<ScrollingBlockList.BlockSlot> {
            BlockData block;
            ScrollingBlockList parent;

            BlockSlot(BlockData block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public BlockData getBlock() {
                return block;
            }

            @Override // @mcp: func_230432_a_  = render
            public void func_230432_a_(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockData blockData = this.block;

                FontRenderer font = Minecraft.getInstance().fontRenderer;
                // @mcp: func_238407_a_ = drawString
                font.func_238407_a_(stack, ITextProperties.func_240652_a_(blockData.getEntryName()), left + 40, top + 7, 0xFFFFFF);
                font.func_238407_a_(stack, ITextProperties.func_240652_a_(blockData.isDrawing() ? "Enabled" : "Disabled"), left + 40, top + 17, blockData.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB()); // FIXME: 28/06/2020 this might not work

                RenderHelper.enableStandardItemLighting();
                Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(blockData.getItemStack(), left + 15, top + 7);
                RenderHelper.disableStandardItemLighting();

                RenderSystem.enableAlphaTest();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                Minecraft.getInstance().getRenderManager().textureManager.bindTexture(GuiSelectionScreen.CIRCLE);
                GuiBase.drawTexturedQuadFit((left + entryWidth) - 37, top + (entryHeight / 2f) - 9, 14, 14, new int[]{255, 255, 255}, 50f);
                GuiBase.drawTexturedQuadFit((left + entryWidth) - 35, top + (entryHeight / 2f) - 7, 10, 10, blockData.getColor());
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
            }

            @Override // @mcp: func_231044_a_ = mouseClicked
            public boolean func_231044_a_(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}