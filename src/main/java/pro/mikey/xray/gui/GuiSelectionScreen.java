package pro.mikey.xray.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.RenderType;
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
import pro.mikey.xray.Utils;
import pro.mikey.xray.XRay;
import pro.mikey.xray.gui.manage.BlockListScreen;
import pro.mikey.xray.gui.manage.GuiAddBlock;
import pro.mikey.xray.gui.manage.GuiEdit;
import pro.mikey.xray.gui.utils.GuiBase;
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
    private static final ResourceLocation CIRCLE = Utils.rlFull(XRay.PREFIX_GUI + "circle.png");

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

        this.render = Minecraft.getInstance().getItemRenderer();
        this.children().clear();

        this.scrollList = new ScrollingBlockList(((getWidth() / 2) - (203 / 2)) - 37, getHeight() / 2 + 10, 203, 185, this.itemList, this);
        addRenderableWidget(this.scrollList);

        this.search = new EditBox(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);
        addRenderableWidget(this.search);

        // side bar buttons
        addRenderableWidget(new SupportButtonInner((getWidth() / 2) + 79, getHeight() / 2 - 60, 120, 20, I18n.get("xray.input.add"), "xray.tooltips.add_block", button -> {
            getMinecraft().setScreen(new BlockListScreen());
        }));
        addRenderableWidget(new SupportButtonInner(getWidth() / 2 + 79, getHeight() / 2 - 38, 120, 20, I18n.get("xray.input.add_hand"), "xray.tooltips.add_block_in_hand", button -> {
            ItemStack handItem = getMinecraft().player.getItemInHand(InteractionHand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof BlockItem)) {
                getMinecraft().player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.invalid_hand", Utils.safeItemStackName(handItem).getString())), false);
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
        addRenderableWidget(
            Button.builder(Component.translatable("xray.single.help"), button -> {
                getMinecraft().setScreen(new GuiHelp());
            })
                    .pos(getWidth() / 2 + 79, getHeight() / 2 + 58)
                    .size(60, 20)
                    .build()
        );
        addRenderableWidget(
                Button.builder(Component.translatable("xray.single.close"), button -> {
                    this.onClose();
                })
                        .pos((getWidth() / 2 + 79) + 62, getHeight() / 2 + 58)
                        .size(59, 20)
                        .build()
        );
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

        if (search.getValue().isEmpty()) {
            this.itemList = this.originalList;
            this.scrollList.updateEntries(this.itemList);
            lastSearch = "";
            return;
        }

        // Special cases
        if (search.getValue().equals(":on") || search.getValue().equals(":off")) {
            var state = search.getValue().equals(":on");
            this.itemList = this.originalList.stream()
                    .filter(e -> e.isDrawing() == state)
                    .collect(Collectors.toCollection(ArrayList::new));

            this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));
            this.scrollList.updateEntries(this.itemList);
            lastSearch = search.getValue();
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
    public void renderExtra(GuiGraphics graphics, int x, int y, float partialTicks) {
        if (!search.isFocused() && search.getValue().equals(""))
            graphics.drawString(getFontRender(), I18n.get("xray.single.search"), getWidth() / 2 - 130, getHeight() / 2 - 101, Color.GRAY.getRGB());

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.getWidth() / 2f - 140, ((this.getHeight() / 2f) - 3) + 120, 0);
        pose.scale(0.75f, 0.75f, 0.75f);
        graphics.drawString(this.font, Component.translatable("xray.tooltips.edit1"), 0, 0, Color.GRAY.getRGB());
        pose.translate(0, 12, 0);
        graphics.drawString(this.font, Component.translatable("xray.tooltips.edit2"), 0, 0, Color.GRAY.getRGB());
        pose.popPose();
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

    class ScrollingBlockList extends ObjectSelectionList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;
        public GuiSelectionScreen parent;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockData> blocks, GuiSelectionScreen parent) {
            super(GuiSelectionScreen.this.minecraft, width - 2, height, (GuiSelectionScreen.this.height / 2) - (height / 2) + 10, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.parent = parent;

            this.setX(x + 2);
        }

        @Override
        public int getRowWidth() {
            return 188;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.getRowWidth() + 6;
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

        public static class BlockSlot extends ObjectSelectionList.Entry<ScrollingBlockList.BlockSlot> {
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
            public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockData blockData = this.block;

                Font font = Minecraft.getInstance().font;

                guiGraphics.drawString(font, blockData.getEntryName(), left + 25, top + 7, 0xFFFFFF);
                guiGraphics.drawString(font, blockData.isDrawing() ? "Enabled" : "Disabled", left + 25, top + 17, blockData.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                guiGraphics.renderItem(blockData.getItemStack(), left, top + 7);
                guiGraphics.renderItemDecorations(font, blockData.getItemStack(), left, top + 7); // TODO: verify

                var stack = guiGraphics.pose();
                stack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                guiGraphics.blit(RenderType::guiTextured, GuiSelectionScreen.CIRCLE, (left + entryWidth) - 23, (int) (top + (entryHeight / 2f) - 9), 0, 0, 14, 14, 14, 14, 0x7F000000);
                guiGraphics.blit(RenderType::guiTextured, GuiSelectionScreen.CIRCLE, (left + entryWidth) - 21, (int) (top + (entryHeight / 2f) - 7), 0, 0, 10, 10, 10, 10, 0xFF000000 | blockData.getColor());

                RenderSystem.disableBlend();
                stack.popPose();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.empty();
            }
        }
    }
}
