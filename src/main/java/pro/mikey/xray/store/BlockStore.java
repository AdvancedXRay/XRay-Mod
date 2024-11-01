package pro.mikey.xray.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.Tags;
import pro.mikey.xray.XRay;
import pro.mikey.xray.utils.BlockData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import pro.mikey.xray.utils.SerializableBlockData;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Class managing the list of available ores/blocks to choose from / configure.
 */
public class BlockStore {

    private final HashMap<String, BlockData> store = new HashMap<>();

    public HashMap<String, BlockData> getStore(){
        return store;
    }

    public void put(BlockData data) {
        this.store.put(data.getBlockName(), data);
    }

    public void remove(String blockName) {
        this.store.remove(blockName);
    }

    public Optional<BlockData> get(String blockName){
        if(this.store.containsKey(blockName)) {
            return Optional.of(this.store.get(blockName));
        } else{
            return Optional.empty();
        }
    }


    public void toggleDrawing(BlockData data) {
        this.get(data.getBlockName())
                .ifPresent(
                        (blockData) -> blockData.setDrawing(!blockData.getDrawing())
                );
    }

    // -----------------------------------------------------------------
    //                          PERSISTANCE
    // -----------------------------------------------------------------

    private static final Path STORE_FILE = Minecraft.getInstance().gameDirectory.toPath().resolve(String.format("config/%s/block_store.json", XRay.MOD_ID));

    private static final Random RANDOM = new Random();
    private static final Gson PRETTY_JSON = new GsonBuilder().setPrettyPrinting().create();


    /**
     * Recover the last block store from the last use json file
     */
    public void recoverBlockStore(){
        XRay.logger.debug("Trying to recover block store from file");

        if (!Files.exists(STORE_FILE)) {
            // if there is no file, populate with default ore blocks
            populateWithDefaultOres();
            return;
        }

        try {
            // read the data from the json file
            Type type = new TypeToken<List<SerializableBlockData>>() {}.getType();
            BufferedReader reader = new BufferedReader(new FileReader(STORE_FILE.toFile()));
            List<SerializableBlockData> serializedData = PRETTY_JSON.fromJson(reader, type);


            serializedData.forEach((sd) -> this.store.put(sd.getBlockName(), new BlockData(sd.getName(), sd.getBlockName(), sd.getColor(),sd.getDrawing(), sd.getOrder())));
        } catch (IOException | JsonSyntaxException e) {
            XRay.logger.error("Failed to read json data from {}", STORE_FILE);
        }
    }

    /**
     * Save current hashmap state in the json file as a list
     */
    public void persistBlockStore(){
        XRay.logger.debug("Trying to persist block store to file");
        boolean createdPath = STORE_FILE.getParent().toFile().mkdirs();
        if (!createdPath) {
            XRay.logger.error("Failed to create dirs for {}", STORE_FILE);
            return;
        }

        // try to store hashmap values without keys
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STORE_FILE.toFile()))) {
            // get values and cast them as SerializableBlockData
            List<SerializableBlockData> serializableList = new ArrayList<>(this.store.values());
            PRETTY_JSON.toJson(serializableList, writer);
        } catch (IOException e) {
            XRay.logger.error("Failed to write json data to {}", STORE_FILE);
        }
    }

    /**
     * Populate the hashmap with blocks with the "ORES" tag
     */
    public void populateWithDefaultOres()
    {
        XRay.logger.info("Setting up default ores to the render list");
        var blocks = BuiltInRegistries.BLOCK.stream().toList();

        int orderTrack = 0;
        for (Block block : blocks) {
            if (block.defaultBlockState().is(Tags.Blocks.ORES)) {
                String itemId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).toString();
                this.store.put(itemId, new BlockData(Component.translatable(block.getDescriptionId()).getString(),
                        itemId,
                        (RANDOM.nextInt(255) << 16) + (RANDOM.nextInt(255) << 8) + RANDOM.nextInt(255),
                        new ItemStack(block.asItem()),
                        false,
                        orderTrack++));
            }
        }
    }
}
