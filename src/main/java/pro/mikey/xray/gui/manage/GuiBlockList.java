package pro.mikey.xray.gui.manage;

import com.mojang.blaze3d.matrix.MatrixStack;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.gui.utils.ScrollingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pro.mikey.xray.store.GameBlockStore;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private ScrollingBlockList blockList;
    private ArrayList<GameBlockStore.BlockWithItemStack> blocks;
    private TextFieldWidget search;
    private String lastSearched = "";

    public GuiBlockList() {
        super(false);
        this.blocks = ClientController.gameBlockStore.getStore();
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((getWidth() / 2) + 1, getHeight() / 2 - 12, 202, 185, this.blocks);
        this.children.add(this.blockList);

        search = new TextFieldWidget(getFontRender(), getWidth() / 2 - 100, getHeight() / 2 + 85, 140, 18, new StringTextComponent(""));
        search.changeFocus(true);
        this.setListener(search);

        addButton(new Button(getWidth() / 2 + 43, getHeight() / 2 + 84, 60, 20, new TranslationTextComponent("xray.single.cancel"), b -> {
            this.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiSelectionScreen());
        }));
    }

    @Override
    public void tick() {
        search.tick();
        if (!search.getText().equals(this.lastSearched))
            reloadBlocks();

        super.tick();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(search.getText()))
            return;

        this.blockList.updateEntries(
                search.getText().length() == 0
                        ? this.blocks
                        : this.blocks.stream()
                            .filter(e -> e.getItemStack().getDisplayName().getString().toLowerCase().contains(search.getText().toLowerCase()))
                            .collect(Collectors.toList())
        );

        lastSearched = search.getText();
        this.blockList.setScrollAmount(0);
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        search.render(stack, x, y, partialTicks);
        blockList.render(stack, x, y, partialTicks);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if( this.search.mouseClicked (x, y, button) )
            this.setListener(this.search);

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        blockList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height, List<GameBlockStore.BlockWithItemStack> blocks) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
        }

        @Override
        public void setSelected(@Nullable BlockSlot entry) {
            if (entry == null)
                return;

            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiAddBlock(entry.getBlock().getBlock(), GuiBlockList::new));
        }

        void updateEntries(List<GameBlockStore.BlockWithItemStack> blocks) {
            this.clearEntries(); // @mcp: func_230963_j_ = clearEntries
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        public static class BlockSlot extends AbstractList.AbstractListEntry<ScrollingBlockList.BlockSlot> {
            GameBlockStore.BlockWithItemStack block;
            ScrollingBlockList parent;

            BlockSlot(GameBlockStore.BlockWithItemStack block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public GameBlockStore.BlockWithItemStack getBlock() {
                return block;
            }

            @Override
            public void render(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                FontRenderer font = this.parent.minecraft.fontRenderer;

                ResourceLocation resource = this.block.getItemStack().getItem().getRegistryName();
                font.drawString(stack,this.block.getItemStack().getItem().getName().getString(), left + 40, top + 7, Color.WHITE.getRGB());
                font.drawString(stack, resource != null ? resource.getNamespace() : "", left + 40, top + 17, Color.WHITE.getRGB());
                // @mcp: func_240652_a_ = unknown... Code recommendation

                RenderHelper.enableStandardItemLighting();
                this.parent.minecraft.getItemRenderer().renderItemAndEffectIntoGUI(this.block.getItemStack(), left + 15, top + 7);
                RenderHelper.disableStandardItemLighting();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
