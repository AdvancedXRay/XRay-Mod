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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.screens.helpers.GuiBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class FindBlockScreen extends GuiBase {
    private static final int CONTENT_WIDTH = 202;
    private static final int HEADER_HEIGHT = 20;
    private static final int SPACING = 4;

    // Built once per screen so searching doesn't recreate item stacks or re-translate names for every block
    private final Map<String, String> modNames = new HashMap<>();
    private final List<BlockEntry> allBlocks = BuiltInRegistries.BLOCK.stream()
            .filter(removeIgnoredBlocks())
            .map(block -> BlockEntry.of(block, this.modNames))
            .toList();

    private final Set<Block> addedBlocks = ScanController.INSTANCE.scanStore.addedBlocks();
    private final List<PinnedBlock> pinnedBlocks = this.findPinnedBlocks();

    private ScrollingBlockList blockList;
    private EditBox search;
    private String lastSearched = "";

    public FindBlockScreen() {
        super(false);
    }

    @Override
    public void init() {
        // Keep the search and scroll position when coming back from the configure screen or resizing
        String previousQuery = this.search != null ? this.search.getValue() : "";
        double previousScroll = this.blockList != null ? this.blockList.scrollAmount() : 0;

        LinearLayout layout = LinearLayout.vertical().spacing(SPACING);
        LinearLayout header = layout.addChild(LinearLayout.horizontal().spacing(SPACING));

        this.search = header.addChild(new EditBox(getFontRender(), CONTENT_WIDTH - HEADER_HEIGHT - SPACING, HEADER_HEIGHT, Component.translatable("xray.single.search")));
        this.search.setHint(Component.translatable("xray.single.search_hint").withStyle(EditBox.SEARCH_HINT_STYLE));
        this.search.setValue(previousQuery);

        SpriteIconButton back = header.addChild(SpriteIconButton.builder(Component.translatable("xray.single.cancel"), b -> this.onClose(), true)
                .sprite(XRay.id("icon/close"), 12, 12)
                .size(HEADER_HEIGHT, HEADER_HEIGHT)
                .build());
        back.setTooltip(Tooltip.create(Component.translatable("xray.single.cancel")));

        this.blockList = layout.addChild(new ScrollingBlockList(CONTENT_WIDTH, 184));

        layout.setPosition(getWidth() / 2 - 100, getHeight() / 2 - 104);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);

        reloadBlocks();
        this.blockList.setScrollAmount(previousScroll);
    }

    @Override
    public void onClose() {
        // Escape goes back to the block list rather than closing the whole UI
        minecraft.setScreen(new ScanManageScreen());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Enter in the search picks the top result that can still be added
        if (event.isConfirmation() && this.search.isFocused()) {
            this.blockList.children().stream()
                    .filter(slot -> !slot.alreadyAdded())
                    .findFirst()
                    .ifPresent(this.blockList::setSelected);
            return true;
        }

        return super.keyPressed(event);
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
        // Pinned blocks are hidden while searching so Enter still picks the top search result
        this.blockList.updateEntries(
                query.isEmpty() ? this.pinnedBlocks : List.of(),
                query.isEmpty()
                        ? this.allBlocks
                        : this.allBlocks.stream()
                            .filter(e -> e.searchName().contains(query))
                            .toList()
        );

        lastSearched = search.getValue();
        this.blockList.setScrollAmount(0);
    }

    private List<PinnedBlock> findPinnedBlocks() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }

        List<PinnedBlock> pinned = new ArrayList<>();
        if (player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            this.addPinned(pinned, blockItem.getBlock(), "xray.pinned.in_hand");
        }

        if (player.pick(100, 1.0F, false) instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            this.addPinned(pinned, player.level().getBlockState(hit.getBlockPos()).getBlock(), "xray.pinned.looking_at");
        }

        return pinned;
    }

    private void addPinned(List<PinnedBlock> pinned, Block block, String labelKey) {
        // Skip ignored blocks, and the same block twice when it's both in hand and looked at
        if (!removeIgnoredBlocks().test(block) || this.addedBlocks.contains(block) || pinned.stream().anyMatch(existing -> existing.entry().block() == block)) {
            return;
        }

        pinned.add(new PinnedBlock(BlockEntry.of(block, this.modNames), Component.translatable(labelKey)));
    }

    private static Predicate<Block> removeIgnoredBlocks() {
        return (block) -> !ScanController.blackList.contains(block);
    }

    @Override
    public void renderExtra(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        if (this.blockList.children().isEmpty()) {
            drawEmptyState(graphics, getFontRender(), Component.translatable("xray.empty.no_matches"), this.blockList.getX(), this.blockList.getY(), this.blockList.getWidth());
        }
    }

    static void drawEmptyState(GuiGraphicsExtractor graphics, Font font, Component message, int x, int y, int width) {
        List<FormattedCharSequence> lines = font.split(message, width - 20);
        int lineY = y + 20;
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, x + (width - font.width(line)) / 2, lineY, SECONDARY_TEXT_COLOR, false);
            lineY += font.lineHeight + 2;
        }
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
            if (entry == null || entry.alreadyAdded()) {
                return;
            }

            Minecraft.getInstance().setScreen(new ScanConfigureScreen(entry.getBlock(), () -> FindBlockScreen.this));
        }

        void updateEntries(List<PinnedBlock> pinned, List<BlockEntry> blocks) {
            this.clearEntries();

            for (int i = 0; i < pinned.size(); i++) {
                this.addEntry(new BlockSlot(pinned.get(i).entry(), pinned.get(i).label(), i == pinned.size() - 1, this));
            }

            blocks.forEach(block -> this.addEntry(new BlockSlot(block, null, false, this)));
        }

        public class BlockSlot extends ObjectSelectionList.Entry<ScrollingBlockList.BlockSlot> {
            private static final int SEPARATOR_COLOR = 0xFF8B8B8B;
            // Panel background colour at half opacity, drawn over the icon to fade it
            private static final int FADE_COLOR = 0x80C6C6C6;

            private final BlockEntry entry;
            private final ScrollingBlockList parent;
            // Set for pinned rows, e.g. "In hand"
            @Nullable
            private final Component pinLabel;
            private final boolean lastPinned;

            public BlockSlot(BlockEntry entry, @Nullable Component pinLabel, boolean lastPinned, ScrollingBlockList parent) {
                this.entry = entry;
                this.pinLabel = pinLabel;
                this.lastPinned = lastPinned;
                this.parent = parent;
            }

            public Block getBlock() {
                return entry.block();
            }

            boolean alreadyAdded() {
                return FindBlockScreen.this.addedBlocks.contains(this.entry.block());
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTicks) {
                Font font = this.parent.minecraft.font;

                int left = this.getX();
                int middle = this.getContentYMiddle();
                int textLeft = left + 16 + 6;
                int textRight = this.getContentRight() - 3;

                boolean added = this.alreadyAdded();
                Component label = added ? Component.translatable("xray.find.added") : this.pinLabel;
                if (label != null) {
                    int labelX = textRight - font.width(label);
                    guiGraphics.text(font, label, labelX, middle - 4, SECONDARY_TEXT_COLOR, false);
                    textRight = labelX - 6;
                }

                if (this.lastPinned) {
                    // Divider between the pinned blocks and the full list
                    int bottom = this.getY() + this.getHeight();
                    guiGraphics.fill(left, bottom - 1, this.getContentRight(), bottom, SEPARATOR_COLOR);
                }

                guiGraphics.item(this.entry.stack(), left, middle - 8);
                if (added) {
                    guiGraphics.fill(left, middle - 8, left + 16, middle + 8, FADE_COLOR);
                }

                Component name = added
                        ? Component.literal(this.entry.name()).withColor(SECONDARY_TEXT_COLOR).withoutShadow()
                        : Component.literal(this.entry.name()).withStyle(ChatFormatting.WHITE);
                guiGraphics.textRenderer().acceptScrolling(name, textLeft, textLeft, textRight, middle - 10, middle);
                guiGraphics.textRenderer().acceptScrolling(Component.literal(this.entry.modName()).withColor(SECONDARY_TEXT_COLOR).withoutShadow(), textLeft, textLeft, textRight, middle, middle + 10);

                if (!hovering) {
                    return;
                }

                if (!this.entry.stack().isEmpty() && isWithin(mouseX, mouseY, left, middle - 8, 16, 16)) {
                    guiGraphics.setTooltipForNextFrame(font, this.entry.stack(), mouseX, mouseY);
                } else if (isWithin(mouseX, mouseY, textLeft, middle, Math.min(font.width(this.entry.modName()), textRight - textLeft), 10)) {
                    guiGraphics.setTooltipForNextFrame(font, Component.literal(this.entry.namespace()).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
                }
            }

            private static boolean isWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
                return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
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

    private record PinnedBlock(BlockEntry entry, Component label) {
    }

    private record BlockEntry(Block block, ItemStack stack, String name, String searchName, String namespace, String modName) {
        static BlockEntry of(Block block, Map<String, String> modNames) {
            String name = block.getName().getString();
            String namespace = BuiltInRegistries.BLOCK.getKey(block).getNamespace();
            String modName = modNames.computeIfAbsent(namespace, XRay.XPLAT::modDisplayName);

            return new BlockEntry(block, block.asItem().getDefaultInstance(), name, name.toLowerCase(), namespace, modName);
        }
    }
}
