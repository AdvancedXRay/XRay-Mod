package pro.mikey.xray.utils;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface XPlatShim {
    TagKey<Block> oreTag();

    Supplier<Path> configPath();
}
