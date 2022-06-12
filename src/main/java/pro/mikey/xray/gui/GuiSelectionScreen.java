package pro.mikey.xray.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSelectionScreen extends GuiBase {
    private static final ResourceLocation CIRCLE = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    private Button distButtons;
    private EditBox search;
    public ItemRenderer render;

    private String lastSearch = "";

    private ArrayList<BlockData> itemList, originalList;
    private ScrollingBlockList scrollList;

    public GuiSelectionScreen() {
        super(true);
        this.setSideTitle(I18n.get("xray.single.tools"));

        // Inject this hear as everything is loaded
        if (ClientController.blockStore.created) {
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
        if (getMinecraft().player == null)
            return;

        this.render = this.itemRenderer;
        this.children().clear();

        this.scrollList = new ScrollingBlockList((getWidth() / 2) - 37, getHeight() / 2 + 10, 203, 185, this.itemList, this);
        addRenderableWidget(this.scrollList);

        this.search = new EditBox(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);

        // side bar buttons
        addRenderableWidget(new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 - 60, 120, 20, I18n.get("xray.input.add"), "xray.tooltips.add_block", button -> {
            getMinecraft().player.closeContainer();
            getMinecraft().setScreen(new GuiBlockList());
        }));
        addRenderableWidget(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 38, 120, 20, I18n.get("xray.input.add_hand"), "xray.tooltips.add_block_in_hand", button -> {
            getMinecraft().player.closeContainer();
            ItemStack handItem = getMinecraft().player.getItemInHand(InteractionHand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof net.minecraft.world.item.BlockItem)) {
                getMinecraft().player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.invalid_hand", handItem.getHoverName().getString())), false);
                return;
            }

            getMinecraft().setScreen(new GuiAddBlock(((BlockItem) handItem.getItem()).getBlock(), GuiSelectionScreen::new));
        }));
        addRenderableWidget(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 16, 120, 20, I18n.get("xray.input.add_look"), "xray.tooltips.add_block_looking_at", button -> {
            Player player = getMinecraft().player;
            if (getMinecraft().level == null || player == null)
                return;

            this.onClose();
            try {
                Vec3 look = player.getLookAngle();
                Vec3 start = new Vec3(player.blockPosition().getX(), player.blockPosition().getY() + player.getEyeHeight(), player.blockPosition().getZ());
                Vec3 end = new Vec3(player.blockPosition().getX() + look.x * 100, player.blockPosition().getY() + player.getEyeHeight() + look.y * 100, player.blockPosition().getZ() + look.z * 100);

                ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult result = getMinecraft().level.clip(context);

                if (result.getType() == HitResult.Type.BLOCK) {
                    Block lookingAt = getMinecraft().level.getBlockState(result.getBlockPos()).getBlock();

                    player.closeContainer();
                    getMinecraft().setScreen(new GuiAddBlock(lookingAt, GuiSelectionScreen::new));
                } else
                    player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.nothing_infront")), false);
            } catch (NullPointerException ex) {
                player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.thats_odd")), false);
            }
        }));

        addRenderableWidget(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 6, 120, 20, I18n.get("xray.input.show-lava", Controller.isLavaActive()), "xray.tooltips.show_lava", button -> {
            Controller.toggleLava();
            button.setMessage(Component.translatable("xray.input.show-lava", Controller.isLavaActive()));
        }));

        addRenderableWidget(distButtons = new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 + 36, 120, 20, I18n.get("xray.input.distance", Controller.getVisualRadius()), "xray.tooltips.distance", button -> {
            Controller.incrementCurrentDist();
            button.setMessage(Component.translatable("xray.input.distance", Controller.getVisualRadius()));
        }));
        addRenderableWidget(new Button(getWidth() / 2 + 79, getHeight() / 2 + 58, 60, 20, Component.translatable("xray.single.help"), button -> {
            getMinecraft().player.closeContainer();
            getMinecraft().setScreen(new GuiHelp());
        }));
        addRenderableWidget(new Button((getWidth() / 2 + 79) + 62, getHeight() / 2 + 58, 59, 20, Component.translatable("xray.single.close"), button -> {
            this.onClose();
        }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!search.isFocused() && keyCode == KeyBindings.toggleGui.getKey().getValue()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateSearch() {
        if (lastSearch.equals(search.getValue()))
            return;

        if (search.getValue().equals("")) {
            this.itemList = this.originalList;
            this.scrollList.updateEntries(this.itemList);
            lastSearch = "";
            return;
        }

        this.itemList = this.originalList.stream()
                .filter(b -> b.getEntryName().toLowerCase().contains(search.getValue().toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));

        this.scrollList.updateEntries(this.itemList);
        lastSearch = search.getValue();
    }

    @Override
    public void tick() {
        super.tick();

        if (search != null) {
            search.tick();
        }

        updateSearch();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (search.mouseClicked(x, y, mouse))
            this.setFocused(search);

        if (mouse == 1 && distButtons.isMouseOver(x, y)) {
            Controller.decrementCurrentDist();
            distButtons.setMessage(Component.translatable("xray.input.distance", Controller.getVisualRadius()));
            distButtons.playDownSound(Minecraft.getInstance().getSoundManager());
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        this.search.render(stack, x, y, partialTicks);
        this.scrollList.render(stack, x, y, partialTicks);

        if (!search.isFocused() && search.getValue().equals(""))
            Minecraft.getInstance().font.drawShadow(stack, I18n.get("xray.single.search"), (float) getWidth() / 2 - 130, (float) getHeight() / 2 - 101, Color.GRAY.getRGB());
    }

    @Override
    public void removed() {
        Configuration.store.radius.save();
        ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

        Controller.requestBlockFinder(true);
        super.removed();
    }

    static final class SupportButtonInner extends SupportButton {
        public SupportButtonInner(int widthIn, int heightIn, int width, int height, String text, String i18nKey, OnPress onPress) {
            super(widthIn, heightIn, width, height, Component.literal(text), Component.translatable(i18nKey), onPress);
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

            if (GuiSelectionScreen.hasShiftDown()) {
                Minecraft.getInstance().player.closeContainer();
                Minecraft.getInstance().setScreen(new GuiEdit(entry.block));
                return;
            }

            Controller.getBlockStore().toggleDrawing(entry.block);
            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
        }

        void updateEntries(List<BlockData> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this))); // @mcp: addEntry = addEntry
        }

        public static class BlockSlot extends AbstractSelectionList.Entry<ScrollingBlockList.BlockSlot> {
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
            public void render(PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockData blockData = this.block;

                Font font = Minecraft.getInstance().font;

                font.draw(stack, blockData.getEntryName(), left + 35, top + 7, 0xFFFFFF);
                font.draw(stack, blockData.isDrawing() ? "Enabled" : "Disabled", left + 35, top + 17, blockData.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                Lighting.setupFor3DItems();
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(blockData.getItemStack(), left + 8, top + 7);
                Lighting.setupForFlatItems();

                if (mouseX > left && mouseX < (left + entryWidth) && mouseY > top && mouseY < (top + entryHeight) && mouseY < (this.parent.getTop() + this.parent.getHeight()) && mouseY > this.parent.getTop()) {
                    this.parent.parent.renderTooltip(
                            stack,
                            Language.getInstance().getVisualOrder(Arrays.asList(Component.translatable("xray.tooltips.edit1"), Component.translatable("xray.tooltips.edit2"))),
                            left + 15,
                            (entryIdx == this.parent.children().size() - 1 ? (top - (entryHeight - 20)) : (top + (entryHeight + 15))) // @mcp: children = getEntries
                    );
                }

                Color color = new Color(blockData.getColor());

                stack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderTexture(0, GuiSelectionScreen.CIRCLE);
                RenderSystem.setShaderColor(0, 0, 0, .5f);
                blit(stack, (left + entryWidth) - 35, (int) (top + (entryHeight / 2f) - 9), 0, 0, 14, 14, 14, 14);
                RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
                blit(stack, (left + entryWidth) - 33, (int) (top + (entryHeight / 2f) - 7), 0, 0, 10, 10, 10, 10);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.disableBlend();
                stack.popPose();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}
