package pro.mikey.xray.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.XRay;
import pro.mikey.xray.gui.manage.GuiAddBlock;
import pro.mikey.xray.gui.manage.GuiBlockList;
import pro.mikey.xray.gui.manage.GuiEdit;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.gui.utils.ScrollingList;
import pro.mikey.xray.gui.utils.SupportButton;
import pro.mikey.xray.keybinding.KeyBindings;
import pro.mikey.xray.store.BlockStore;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;
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
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
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
        if( ClientController.blockStore.created ) {
            List<BlockData.SerializableBlockData> blocks = ClientController.blockStore.populateDefault();
            Controller.getBlockStore().setStore(BlockStore.getFromSimpleBlockList(blocks));

            ClientController.blockStore.created = false;
        }

        this.itemList = new ArrayList<>(Controller.getBlockStore().getStore().values());
        this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));

        this.originalList = this.itemList;
    }

    @Override
    public void init() {
        if( getMinecraft().player == null )
            return;

        this.render = this.itemRenderer;
        this.buttons.clear();

		this.scrollList = new ScrollingBlockList((getWidth() / 2) - 37, getHeight() / 2 + 10, 203, 185, this.itemList, this);
        this.children.add(this.scrollList);

        this.search = new TextFieldWidget(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, StringTextComponent.EMPTY);
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
                getMinecraft().player.sendStatusMessage(new StringTextComponent("[XRay] " + I18n.format("xray.message.invalid_hand", handItem.getDisplayName().getString())), false);
                return;
            }

            getMinecraft().displayGuiScreen(new GuiAddBlock(((BlockItem) handItem.getItem()).getBlock(), GuiSelectionScreen::new));
        }));
        addButton(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 16, 120, 20, I18n.format("xray.input.add_look"), "xray.tooltips.add_block_looking_at", button -> {
            PlayerEntity player = getMinecraft().player;
            if( getMinecraft().world == null || player == null )
                return;

            this.closeScreen();
            try {
                Vector3d look = player.getLookVec(); // @mcp: func_233580_cy_ = getPosition (blockPos)
                Vector3d start = new Vector3d(player.getPosition().getX(), player.getPosition().getY() + player.getEyeHeight(), player.getPosition().getZ());
                Vector3d end = new Vector3d(player.getPosition().getX() + look.x * 100, player.getPosition().getY() + player.getEyeHeight() + look.y * 100, player.getPosition().getZ() + look.z * 100);

                RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player);
                BlockRayTraceResult result = getMinecraft().world.rayTraceBlocks(context);

                if (result.getType() == RayTraceResult.Type.BLOCK) {
                    BlockState state = getMinecraft().world.getBlockState(result.getPos());
                    Block lookingAt = getMinecraft().world.getBlockState(result.getPos()).getBlock();

                    ItemStack lookingStack = lookingAt.getPickBlock(state, result, getMinecraft().world, result.getPos(), getMinecraft().player);

                    player.closeScreen();
                    getMinecraft().displayGuiScreen(new GuiAddBlock(Block.getBlockFromItem(lookingStack.getItem()), GuiSelectionScreen::new));
                } else
                    player.sendStatusMessage(new StringTextComponent("[XRay] " + I18n.format("xray.message.nothing_infront")), false);
            } catch (NullPointerException ex) {
                player.sendStatusMessage(new StringTextComponent("[XRay] " + I18n.format("xray.message.thats_odd")), false);
            }
        }));

        addButton(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 6, 120, 20, I18n.format("xray.input.show-lava", Controller.isLavaActive()), "xray.tooltips.show_lava", button -> {
            Controller.toggleLava();
            button.setMessage(new TranslationTextComponent("xray.input.show-lava", Controller.isLavaActive()));
        }));

        addButton(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 36, 120, 20, I18n.format("xray.input.distance", Controller.getRadius()), "xray.tooltips.distance", button -> {
            Controller.incrementCurrentDist();
            button.setMessage(new TranslationTextComponent("xray.input.distance", Controller.getRadius()));
        }));
        addButton(new Button(getWidth() / 2 + 79, getHeight() / 2 + 58, 60, 20, new TranslationTextComponent("xray.single.help"), button -> {
            getMinecraft().player.closeScreen();
            getMinecraft().displayGuiScreen(new GuiHelp());
        }));
        addButton(new Button((getWidth() / 2 + 79) + 62, getHeight() / 2 + 58, 59, 20, new TranslationTextComponent("xray.single.close"), button -> {
            this.closeScreen();
        }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!search.isFocused() && keyCode == KeyBindings.toggleGui.getKeyBinding().getKey().getKeyCode()) {
            this.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
            this.setListener(search);

        if (mouse == 1 && distButtons.isMouseOver(x, y)) {
            Controller.decrementCurrentDist();
            distButtons.setMessage(new TranslationTextComponent("xray.input.distance", Controller.getRadius()));
            distButtons.playDownSound(Minecraft.getInstance().getSoundHandler());
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        this.search.render(stack, x, y, partialTicks);
        this.scrollList.render(stack, x, y, partialTicks );

        if (!search.isFocused() && search.getText().equals(""))
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(stack, I18n.format("xray.single.search"), (float) getWidth() / 2 - 130, (float) getHeight() / 2 - 101, Color.GRAY.getRGB());
    }

    @Override
    public void onClose() {
        Configuration.store.radius.save();
        ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

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
        public GuiSelectionScreen parent;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockData> blocks, GuiSelectionScreen parent) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.parent = parent;
        }

        public void setSelected(@Nullable BlockSlot entry, int mouse) {
            if (entry == null)
                return;

            if( GuiSelectionScreen.hasShiftDown() ) {
                Minecraft.getInstance().player.closeScreen();
                Minecraft.getInstance().displayGuiScreen( new GuiEdit(entry.block) );
                return;
            }

            Controller.getBlockStore().toggleDrawing(entry.block);
            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
        }

        void updateEntries(List<BlockData> blocks) {
            this.clearEntries ();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this))); // @mcp: func_230513_b_ = addEntry
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
            public void render(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockData blockData = this.block;

                FontRenderer font = Minecraft.getInstance().fontRenderer;

                font.drawString(stack, blockData.getEntryName(), left + 35, top + 7, 0xFFFFFF);
                font.drawString(stack, blockData.isDrawing() ? "Enabled" : "Disabled", left + 35, top + 17, blockData.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                RenderHelper.enableStandardItemLighting();
                Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(blockData.getItemStack(), left + 8, top + 7);
                RenderHelper.disableStandardItemLighting();
                if (mouseX > left && mouseX < (left + entryWidth) && mouseY > top && mouseY < (top + entryHeight) && mouseY < (this.parent.getTop() + this.parent.getHeight()) && mouseY > this.parent.getTop()) {
                    this.parent.parent.renderTooltip(
                            stack,
                            LanguageMap.getInstance().func_244260_a(Arrays.asList(new TranslationTextComponent("xray.tooltips.edit1"), new TranslationTextComponent("xray.tooltips.edit2"))),
                            left + 15,
                            (entryIdx == this.parent.getEventListeners().size() - 1 ? (top - (entryHeight - 20)) : (top + (entryHeight + 15))) // @mcp: func_231039_at__ = getEntries
                    );
                }

                RenderSystem.enableAlphaTest();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                Minecraft.getInstance().getRenderManager().textureManager.bindTexture(GuiSelectionScreen.CIRCLE);
                GuiBase.drawTexturedQuadFit(((left + entryWidth)) - 34, top + (entryHeight / 2f) - 9, 14, 14, blockData.getColor(), 50f);
                GuiBase.drawTexturedQuadFit(((left + entryWidth)) - 32, top + (entryHeight / 2f) - 7, 10, 10, blockData.getColor());
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}
