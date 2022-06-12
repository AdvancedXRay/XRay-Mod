package pro.mikey.xray.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.mikey.xray.XRay;
import pro.mikey.xray.utils.BlockData;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DiscoveryStorage {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Path STORE_FILE = Minecraft.getInstance().gameDirectory.toPath().resolve(String.format("config/%s/block_store.json", XRay.MOD_ID));

    private static final Random RANDOM = new Random();
    private static final Gson PRETTY_JSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean created = false;

    // This should only be initialised once
    public DiscoveryStorage() {
        if (Files.exists(STORE_FILE)) {
            return;
        }

        boolean createdPath = STORE_FILE.getParent().toFile().mkdirs();
        if (!createdPath) {
            LOGGER.error("Failed to create dirs for {}", STORE_FILE);
            return;
        }

        this.created = true;

        // Create a file with nothing inside
        this.write(new ArrayList<BlockData.SerializableBlockData>());
        LOGGER.info("Created block store");
    }

    public void write(ArrayList<BlockData> blockData) {
        List<BlockData.SerializableBlockData> simpleBlockData = new ArrayList<>();
        blockData.forEach(e -> simpleBlockData.add(new BlockData.SerializableBlockData(e.getEntryName(), e.getBlockName(), e.getColor(), e.isDrawing(), e.getOrder())));

        this.write(simpleBlockData);
    }

    private void write(List<BlockData.SerializableBlockData> simpleBlockData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STORE_FILE.toFile()))) {
            PRETTY_JSON.toJson(simpleBlockData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write json data to {}", STORE_FILE);
        }
    }

    public List<BlockData.SerializableBlockData> read() {
        if (!Files.exists(STORE_FILE))
            return new ArrayList<>();

        try {
            Type type = new TypeToken<List<BlockData.SerializableBlockData>>() {
            }.getType();
            try (BufferedReader reader = new BufferedReader(new FileReader(STORE_FILE.toFile()))) {
                return PRETTY_JSON.fromJson(reader, type);
            } catch (JsonSyntaxException ex) {
                XRay.logger.log(Level.ERROR, "Failed to read json data from " + STORE_FILE);
            }
        } catch (IOException e) {
            XRay.logger.log(Level.ERROR, "Failed to read json data from " + STORE_FILE);
        }

        return new ArrayList<>();
    }

    /**
     * Populate the ore list / block list with any blocks from the ORES tag in forge.
     *
     * @return a list of ores found in the tag
     */
    public List<BlockData.SerializableBlockData> populateDefault() {
        List<BlockData.SerializableBlockData> oresData = new ArrayList<>();

        // No registry, not defaults
        ITagManager<Block> blockTags = ForgeRegistries.BLOCKS.tags();
        if (blockTags == null) {
            return List.of();
        }

        int orderTrack = 0;
        for (Block block : blockTags.getTag(Tags.Blocks.ORES)) {
            oresData.add(new BlockData.SerializableBlockData(Component.translatable(block.getDescriptionId()).getString(),
                    Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString(),
                    (RANDOM.nextInt(255) << 16) + (RANDOM.nextInt(255) << 8) + RANDOM.nextInt(255),
                    false,
                    orderTrack++)
            );
        }

        LOGGER.info("Setting up default ores to the render list");
        this.write(oresData);
        return oresData;
    }
}
