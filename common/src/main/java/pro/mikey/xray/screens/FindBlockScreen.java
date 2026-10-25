package pro.mikey.xray.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.screens.helpers.GuiBase;

import java.util.List;
import java.util.function.Predicate;

public class FindBlockScreen extends GuiBase {
    private static final int CONTENT_WIDTH = 202;
    private static final int HEADER_HEIGHT = 20;
    private static final int SPACING = 4;

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
        LinearLayout layout = LinearLayout.vertical().spacing(SPACING);
        LinearLayout header = layout.addChild(LinearLayout.horizontal().spacing(SPACING));

        this.search = header.addChild(new EditBox(getFontRender(), CONTENT_WIDTH - HEADER_HEIGHT - SPACING, HEADER_HEIGHT, Component.translatable("xray.single.search")));
        this.search.setHint(Component.translatable("xray.single.search_hint").withStyle(EditBox.SEARCH_HINT_STYLE));

        SpriteIconButton back = header.addChild(SpriteIconButton.builder(Component.translatable("xray.single.cancel"), b -> Minecraft.getInstance().setScreen(new ScanManageScreen()), true)
                .sprite(XRay.id("icon/close"), 12, 12)
                .size(HEADER_HEIGHT, HEADER_HEIGHT)
                .build());
        back.setTooltip(Tooltip.create(Component.translatable("xray.single.cancel")));

        this.blockList = layout.addChild(new ScrollingBlockList(CONTENT_WIDTH, 184));

        layout.setPosition(getWidth() / 2 - 100, getHeight() / 2 - 104);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);

        reloadBlocks();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.search);
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
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseXDelta, double mouseYDelta) {
        if (super.mouseScrolled(mouseX, mouseY, mouseXDelta, mouseYDelta)) {
            return true;
        }

        return blockList.mouseScrolled(mouseX, mouseY, mouseXDelta, mouseYDelta);
    }

    public class ScrollingBlockList extends ObjectSelectionList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 24;

        ScrollingBlockList(int width, int height) {
            super(FindBlockScreen.this.minecraft, width, height, 0, SLOT_HEIGHT);
        }

        @Override
        public int getRowLeft() {
            return this.getX();
        }

        @Override
        public int getRowWidth() {
            return this.getWidth() - this.scrollbarWidth() - 2;
        }

        @Override
        protected int scrollBarX() {
            return this.getRowRight() + 2;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
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

                int left = this.getX();
                int middle = this.getContentYMiddle();
                int textLeft = left + 16 + 6;
                int textRight = this.getContentRight() - 3;

                guiGraphics.item(this.entry.stack(), left, middle - 8);

                guiGraphics.textRenderer().acceptScrolling(Component.literal(this.entry.name()).withStyle(ChatFormatting.WHITE), textLeft, textLeft, textRight, middle - 10, middle);
                guiGraphics.text(font, this.entry.namespace(), textLeft, middle + 1, SECONDARY_TEXT_COLOR, false);
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
