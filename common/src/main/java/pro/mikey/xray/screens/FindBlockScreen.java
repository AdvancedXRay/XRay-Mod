package pro.mikey.xray.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.screens.helpers.GuiBase;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class FindBlockScreen extends GuiBase {
    // Built once per screen so searching doesn't recreate item stacks or re-translate names for every block
    private final List<BlockEntry> allBlocks = BuiltInRegistries.BLOCK.stream()
            .filter(removeIgnoredBlocks())
            .map(BlockEntry::of)
            .toList();

    private ScrollingBlockList blockList;
    private EditBox search;
    private String lastSearched = "";

    public FindBlockScreen() {
        super(false);
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((getWidth() / 2) + 1, getHeight() / 2 - 12, 202, 182);
        addRenderableWidget(this.blockList);

        search = new EditBox(getFontRender(), getWidth() / 2 - 100, getHeight() / 2 + 85, 140, 18, Component.literal(""));
        search.setFocused(true);
        this.setFocused(search);

        addRenderableWidget(Button.builder(Component.translatable("xray.single.cancel"), b -> Minecraft.getInstance().setScreen(new ScanManageScreen()))
                .pos(getWidth() / 2 + 43, getHeight() / 2 + 84)
                .size(60, 20)
                .build());

        reloadBlocks();
    }

    @Override
    public void tick() {
        if (!search.getValue().equals(this.lastSearched)) {
            reloadBlocks();
        }

        super.tick();
    }

    private void reloadBlocks() {
        String query = search.getValue().toLowerCase();
        this.blockList.updateEntries(
                query.isEmpty()
                        ? this.allBlocks
                        : this.allBlocks.stream()
                            .filter(e -> e.searchName().contains(query))
                            .toList()
        );

        lastSearched = search.getValue();
        this.blockList.setScrollAmount(0);
    }

    private static Predicate<Block> removeIgnoredBlocks() {
        return (block) -> !ScanController.blackList.contains(block);
    }

    @Override
    public void renderExtra(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        search.extractRenderState(graphics, x, y, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if( this.search.mouseClicked(mouseButtonEvent, bl) )
            this.setFocused(this.search);

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseXDelta, double mouseYDelta) {
        blockList.mouseScrolled(mouseX, mouseY, mouseXDelta, mouseYDelta);
        return super.mouseScrolled(mouseX, mouseY, mouseXDelta, mouseYDelta);
    }

    public class ScrollingBlockList extends ObjectSelectionList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height) {
            super(FindBlockScreen.this.minecraft, width, height, (FindBlockScreen.this.height / 2) - (height / 2) - 10, SLOT_HEIGHT);
            this.setX((FindBlockScreen.this.getWidth() / 2) - (width / 2) + 1);
        }

        @Override
        public int getRowWidth() {
            return 188;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.getRowWidth() + 7;
        }

        @Override
        public int getNextY() {
            return this.getY() + 2 - (int) this.scrollAmount() + this.children().size() * SLOT_HEIGHT;
        }

        @Override
        public void setSelected(@Nullable BlockSlot entry) {
            if (entry == null) {
                return;
            }

            Minecraft.getInstance().setScreen(new ScanConfigureScreen(entry.getBlock(), FindBlockScreen::new));
        }

        void updateEntries(List<BlockEntry> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        public class BlockSlot extends ObjectSelectionList.Entry<ScrollingBlockList.BlockSlot> {
            private final BlockEntry entry;
            private final ScrollingBlockList parent;

            public BlockSlot(BlockEntry entry, ScrollingBlockList parent) {
                this.entry = entry;
                this.parent = parent;
            }

            public Block getBlock() {
                return entry.block();
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTicks) {
                Font font = this.parent.minecraft.font;

                guiGraphics.text(font, this.entry.name(), this.getContentX() + 25, this.getContentY() + 7, Color.WHITE.getRGB());
                guiGraphics.text(font, this.entry.namespace(), this.getContentX() + 25, this.getContentY() + 17, Color.GRAY.getRGB());

                guiGraphics.item(this.entry.stack(), this.getContentX(), this.getContentY() + 7);
            }

            @Override
            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }

    private record BlockEntry(Block block, ItemStack stack, String name, String searchName, String namespace) {
        static BlockEntry of(Block block) {
            String name = block.getName().getString();
            return new BlockEntry(block, block.asItem().getDefaultInstance(), name, name.toLowerCase(), BuiltInRegistries.BLOCK.getKey(block).getNamespace());
        }
    }
}
