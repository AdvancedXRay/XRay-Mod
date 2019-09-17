package com.xray.gui;

import com.xray.Configuration;
import com.xray.XRay;
import com.xray.gui.manage.GuiAddBlock;
import com.xray.gui.manage.GuiBlockList;
import com.xray.gui.manage.GuiEdit;
import com.xray.gui.utils.GuiBase;
import com.xray.gui.utils.ScrollingList;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.BlockItem;
import com.xray.utils.Utils;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSelectionScreen extends GuiBase {
    private static final ResourceLocation CIRCLE = new ResourceLocation(Reference.PREFIX_GUI + "circle.png");

    private Button distButtons;
    private TextFieldWidget search;
    public ItemRenderer render;

    private String lastSearch = "";

    private ArrayList<BlockData> itemList, originalList;
    private ScrollingBlockList scrollList;

    public GuiSelectionScreen() {
        super(true);
        this.setSideTitle(I18n.format("xray.single.tools"));

        this.itemList = new ArrayList<>(Controller.getBlockStore().getStore().values());
        this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));

        this.originalList = this.itemList;
    }

    @Override
    public void init() {
        this.render = this.itemRenderer;
        this.buttons.clear();

		this.scrollList = new ScrollingBlockList((width / 2) - 37, height / 2 + 10, 203, 185, this.itemList);
        this.children.add(this.scrollList);

        this.search = new TextFieldWidget(getFontRender(), width / 2 - 137, height / 2 - 105, 202, 18, "");
        this.search.setCanLoseFocus(true);

        // side bar buttons
        addButton(new Button((width / 2) + 79, height / 2 - 60, 120, 20, I18n.format("xray.input.add"), button -> {
            getMinecraft().player.closeScreen();
            getMinecraft().displayGuiScreen(new GuiBlockList());
        }));
        addButton(new Button(width / 2 + 79, height / 2 - 38, 120, 20, I18n.format("xray.input.add_hand"), button -> {
            getMinecraft().player.closeScreen();
            ItemStack handItem = getMinecraft().player.getHeldItem(Hand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof net.minecraft.item.BlockItem)) {
                Utils.sendMessage(getMinecraft().player, "[XRay] " + I18n.format("xray.message.invalid_hand", handItem.getDisplayName().getFormattedText()));
                return;
            }

            BlockState state = Utils.getStateFromPlacement(getMinecraft().world, getMinecraft().player, handItem);
            getMinecraft().displayGuiScreen(new GuiAddBlock(new BlockItem(Block.getStateId(state), handItem), null));
        }));
        addButton(new Button(width / 2 + 79, height / 2 - 16, 120, 20, I18n.format("xray.input.add_look"), button -> {
            this.onClose();
            try {
                PlayerEntity player = getMinecraft().player;
                Vec3d look = player.getLookVec();
                Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
                Vec3d end = new Vec3d(player.posX + look.x * 100, player.posY + player.getEyeHeight() + look.y * 100, player.posZ + look.z * 100);

                RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player);
                BlockRayTraceResult result = getMinecraft().world.rayTraceBlocks(context);

                if (result.getType() == RayTraceResult.Type.BLOCK) {
                    BlockState state = getMinecraft().world.getBlockState(result.getPos());
                    Block lookingAt = getMinecraft().world.getBlockState(result.getPos()).getBlock();

                    ItemStack lookingStack = lookingAt.getPickBlock(state, result, getMinecraft().world, result.getPos(), getMinecraft().player);

                    getMinecraft().player.closeScreen();
                    getMinecraft().displayGuiScreen(new GuiAddBlock(new BlockItem(Block.getStateId(state), lookingStack), state));
                } else
                    Utils.sendMessage(getMinecraft().player, "[XRay] " + I18n.format("xray.message.nothing_infront"));
            } catch (NullPointerException ex) {
                Utils.sendMessage(getMinecraft().player, "[XRay] " + I18n.format("xray.message.thats_odd"));
            }
        }));

        addButton(distButtons = new Button((width / 2) + 79, height / 2 + 36, 120, 20, I18n.format("xray.input.distance") + ": " + Controller.getRadius(), button -> {
            Controller.incrementCurrentDist();
            button.setMessage(I18n.format("xray.input.distance") + ": " + Controller.getRadius());
        }));
        addButton(new Button(width / 2 + 79, height / 2 + 58, 60, 20, I18n.format("xray.single.help"), button -> {
            getMinecraft().player.closeScreen();
            getMinecraft().displayGuiScreen(new GuiHelp());
        }));
        addButton(new Button((width / 2 + 79) + 62, height / 2 + 58, 59, 20, I18n.format("xray.single.close"), button -> {
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

    @Override
    public void tick() {
        super.tick();
        search.tick();

        updateSearch();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if( search.mouseClicked(x, y, mouse) )
            this.setFocused(search);

        if (mouse == 1 && distButtons.isMouseOver(x, y)) {
            Controller.decrementCurrentDist();
            distButtons.setMessage(I18n.format("xray.input.distance") + ": " + Controller.getRadius());
            distButtons.playDownSound(Minecraft.getInstance().getSoundHandler());
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        super.render(x, y, partialTicks);

        this.search.render(x, y, partialTicks);
		this.scrollList.render( x, y, partialTicks );

        if (!search.isFocused() && search.getText().equals(""))
            XRay.mc.fontRenderer.drawStringWithShadow(I18n.format("xray.single.search"), (float) width / 2 - 130, (float) height / 2 - 101, Color.GRAY.getRGB());
    }

    @Override
    public void onClose() {
        Configuration.general.radius.save();
        XRay.blockStore.write(Controller.getBlockStore().getStore());

        Controller.requestBlockFinder(true);
        super.onClose();
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

            if( GuiSelectionScreen.hasShiftDown() ) {
                XRay.mc.player.closeScreen();
                XRay.mc.displayGuiScreen( new GuiEdit(entry.block.getEntryKey(), entry.block) );
                return;
            }

            Controller.getBlockStore().toggleDrawing(entry.block.getEntryKey());
            XRay.blockStore.write(Controller.getBlockStore().getStore());
        }

        void updateEntries(List<BlockData> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
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

            @Override
            public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockData blockData = this.block;

                FontRenderer font = Minecraft.getInstance().fontRenderer;

                font.drawString(blockData.getEntryName(), left + 30, top + 7, 0xFFFFFF);
                font.drawString(blockData.isDrawing() ? "Enabled" : "Disabled", left + 30, top + 17, blockData.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                RenderHelper.enableGUIStandardItemLighting();
                Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(blockData.getItemStack(), left + 5, top + 7);
                RenderHelper.disableStandardItemLighting();

                Minecraft.getInstance().getRenderManager().textureManager.bindTexture(GuiSelectionScreen.CIRCLE);
                GuiBase.drawTexturedQuadFit((left + entryWidth) - 22, top + (entryHeight / 2f) - 9, 14, 14, new int[]{255, 255, 255}, 80f);
                GuiBase.drawTexturedQuadFit((left + entryWidth) - 20, top + (entryHeight / 2f) - 7, 10, 10, blockData.getColor().getColor());
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}