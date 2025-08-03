package pro.mikey.xray.core.scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.mikey.xray.XRay;
import pro.mikey.xray.utils.LazyValue;

import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;

public class ScanStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanStore.class);
    private static final String STORE_FILE = "scan-store.json";

    private static final LazyValue<Map<ResourceLocation, String>> BLOCK_TO_COLOR_DEFAULTS = LazyValue.of(() -> {
        Map<ResourceLocation, String> defaults = new HashMap<>();
        defaults.put(fromBlock(Blocks.DIAMOND_ORE), "rgb(0, 255, 0)"); // Green
        defaults.put(fromBlock(Blocks.DEEPSLATE_DIAMOND_ORE), "rgb(0, 255, 0)"); // Green (Deepslate Diamond)
        defaults.put(fromBlock(Blocks.GOLD_ORE), "rgb(255, 215, 0)"); // Gold
        defaults.put(fromBlock(Blocks.DEEPSLATE_GOLD_ORE), "rgb(255, 215, 0)"); // Gold (Deepslate Gold)
        defaults.put(fromBlock(Blocks.IRON_ORE), "rgb(192, 192, 192)"); // Silver
        defaults.put(fromBlock(Blocks.DEEPSLATE_IRON_ORE), "rgb(192, 192, 192)"); // Silver (Deepslate Iron)
        defaults.put(fromBlock(Blocks.COAL_ORE), "rgb(0, 0, 0)"); // Black
        defaults.put(fromBlock(Blocks.DEEPSLATE_COAL_ORE), "rgb(0, 0, 0)"); // Black (Deepslate Coal)
        defaults.put(fromBlock(Blocks.REDSTONE_ORE), "rgb(255, 0, 0)"); // Red
        defaults.put(fromBlock(Blocks.DEEPSLATE_REDSTONE_ORE), "rgb(255, 0, 0)"); // Red (Deepslate Redstone)
        defaults.put(fromBlock(Blocks.LAPIS_ORE), "rgb(0, 0, 255)"); // Blue
        defaults.put(fromBlock(Blocks.DEEPSLATE_LAPIS_ORE), "rgb(0, 0, 255)"); // Blue (Deepslate Lapis)
        defaults.put(fromBlock(Blocks.EMERALD_ORE), "rgb(0, 128, 0)"); // Emerald Green
        defaults.put(fromBlock(Blocks.DEEPSLATE_EMERALD_ORE), "rgb(0, 128, 0)"); // Emerald Green (Deepslate Emerald)
        defaults.put(fromBlock(Blocks.NETHER_GOLD_ORE), "rgb(255, 215, 0)"); // Nether Gold (Gold)
        defaults.put(fromBlock(Blocks.NETHER_QUARTZ_ORE), "rgb(255, 255, 255)"); // Quartz (White)
        defaults.put(fromBlock(Blocks.ANCIENT_DEBRIS), "rgb(128, 64, 0)"); // Ancient Debris (Brown)
        return defaults;
    });

    private static final Map<ScanType.Type, BiFunction<ScanType.Type, JsonObject, ScanType>> SCAN_TYPE_CREATORS = Map.of(
        ScanType.Type.BLOCK, BlockScanType::new
    );

    private final List<Category> categories = new ArrayList<>();

    // In memory holder of only the enabled scan targets
    private final Set<ActiveScanTarget> activeScanTargets = new HashSet<>();

    public ScanStore() {}

    public void createDefaultCategories() {
        List<ScanType> entries = new ArrayList<>();
        var oresTag = XRay.XPLAT.oreTag();

        var blockColorDefaults = BLOCK_TO_COLOR_DEFAULTS.get();

        BuiltInRegistries.BLOCK
                .stream()
                .filter(e -> e.defaultBlockState().is(oresTag))
                .map(e -> new BlockScanType(e, e.getName().getString(), blockColorDefaults.getOrDefault(e.builtInRegistryHolder().key().location(), ScanType.randomRgbColor()), 0))
                .forEach(entries::add);

        this.categories.add(createDefaultCategory(entries));

        this.save();
    }

    private static Category createDefaultCategory(List<ScanType> entries) {
        return new Category(
                new ItemStack(Blocks.DIAMOND_ORE),
                "Default",
                0x00FF00, // Green color
                0,
                entries
        );
    }

    // TODO: Support categories once the GUI can support it.
    public void addEntry(ScanType type) {
        if (this.categories.isEmpty()) {
            this.categories.add(createDefaultCategory(new ArrayList<>()));
        }

        Optional<Category> defaultCategory = this.categories.stream().findFirst();
        if (defaultCategory.isEmpty()) {
            return;
        }

        Category category = defaultCategory.get();
        category.entries.add(type);

        this.save();
    }

    // TODO: Support categories once the GUI can support it.
    public void removeEntry(ScanType type) {
        for (Category category : this.categories) {
            if (category.entries.remove(type)) {
                this.save();
                return; // Exit after removing the entry
            }
        }

        LOGGER.warn("Scan type not found in any category: {}", type);
    }

    public int getNextOrder() {
        var firstCategory = this.categories.stream().findFirst();
        if (firstCategory.isEmpty()) {
            return 0; // No categories, return 0
        }

        // Find the maximum order value in the first category
        return firstCategory.get().entries.stream()
                .mapToInt(ScanType::order)
                .max()
                .orElse(0) + 1; // Return max order + 1
    }

    public void load() {
        loadFromFile();
        updateActiveTargets();
    }

    private void loadFromFile() {
        this.categories.clear();

        JsonArray categories;
        try {
            var configPath = XRay.XPLAT.configPath().get().resolve(STORE_FILE);
            var parentDir = configPath.getParent();
            if (Files.notExists(parentDir)) {
                // If the config directory does not exist, create it
                Files.createDirectories(parentDir);
            }

            String jsonContent = Files.readString(configPath);
            categories = GSON.fromJson(jsonContent, JsonArray.class);
        } catch (Exception e) {
            LOGGER.error("Failed to load scan store from file: {}", e.getMessage());
            return;
        }

        for (var categoryObj : categories) {
            var category = Category.load(categoryObj.getAsJsonObject());
            this.categories.add(category);
        }
    }

    public void save() {
        updateActiveTargets();

        JsonArray categoriesArray = new JsonArray();
        for (Category category : this.categories) {
            JsonObject categoryObj = new JsonObject();
            category.save(categoryObj);
            categoriesArray.add(categoryObj);
        }

        try {
            var configPath = XRay.XPLAT.configPath().get().resolve(STORE_FILE);
            Files.writeString(configPath, GSON.toJson(categoriesArray));
        } catch (Exception e) {
            LOGGER.error("Failed to save scan store to file: {}", e.getMessage());
        }
    }

    public void updateActiveTargets() {
        this.activeScanTargets.clear();
        for (Category category : this.categories) {
            for (ScanType scanType : category.entries) {
                if (!scanType.enabled) {
                    continue; // Skip disabled scan types
                }

                var target = new ActiveScanTarget(scanType, scanType.colorInt);
                this.activeScanTargets.add(target);
            }
        }
    }

    public List<Category> categories() {
        return Collections.unmodifiableList(categories);
    }

    public Set<ActiveScanTarget> activeScanTargets() {
        return activeScanTargets;
    }

    public record Category(
            ItemStack icon,
            String name,
            int color,
            int order,
            List<ScanType> entries
    ) {
        public void save(JsonObject obj) {
            obj.addProperty("name", name);
            obj.addProperty("color", color);
            obj.addProperty("order", order);

            var entriesArray = new JsonArray();
            for (ScanType entry : entries) {
                entriesArray.add(entry.save());
            }

            // Use the ItemStack Codec to serialize the icon.
            try {
                var iconJson = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, icon).getOrThrow();
                obj.add("icon", iconJson.getAsJsonObject());
            } catch (Exception e) {
                LOGGER.warn("Failed to serialize icon for category '{}': {}", name, e.getMessage());
                obj.add("icon", new JsonObject()); // Add an empty object if serialization fails
            }

            obj.add("entries", entriesArray);
        }

        public static Category load(JsonObject obj) {
            var icon = obj.get("icon").getAsJsonObject();
            var name = obj.get("name").getAsString();
            var color = obj.get("color").getAsInt();
            var order = obj.get("order").getAsInt();
            var entriesArray = obj.getAsJsonArray("entries");

            var entries = new ArrayList<ScanType>();
            for (var entry : entriesArray) {
                var entryObj = entry.getAsJsonObject();
                try {
                    var type = ScanType.Type.fromId(ResourceLocation.tryParse(entryObj.get("type").getAsString()));
                    var creator = SCAN_TYPE_CREATORS.get(type);
                    if (creator == null) {
                        LOGGER.warn("No creator found for scan type: {}", type);
                        continue; // Skip unknown types
                    }

                    var scanType = creator.apply(type, entryObj);
                    entries.add(scanType);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Unknown scan type in store: {}, data: {}", entryObj.get("type").getAsString(), entryObj);
                }
            }

            // Use the ItemStack Codec to parse the icon.
            ItemStack iconStack = ItemStack.EMPTY;
            try {
                iconStack = ItemStack.CODEC.decode(JsonOps.INSTANCE, icon).getOrThrow().getFirst();
            } catch (Exception e) {
                LOGGER.warn("Failed to parse icon for category '{}': {}", name, e.getMessage());
            }

            return new Category(
                iconStack,
                name,
                color,
                order,
                entries
            );
        }
    }

    private static ResourceLocation fromBlock(Block block) {
        if (block == null) {
            return null;
        }

        return BuiltInRegistries.BLOCK.getKey(block);
    }
}
