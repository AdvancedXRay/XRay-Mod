package pro.mikey.xray.neoforge;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.Tags;
import pro.mikey.xray.utils.XPlatShim;

import java.nio.file.Path;
import java.util.function.Supplier;

public class XPlatShimImpl implements XPlatShim {
    private final Supplier<Path> configPath = FMLPaths.CONFIGDIR::get;

    @Override
    public TagKey<Block> oreTag() {
        return Tags.Blocks.ORES;
    }

    @Override
    public Supplier<Path> configPath() {
        return configPath;
    }
}
