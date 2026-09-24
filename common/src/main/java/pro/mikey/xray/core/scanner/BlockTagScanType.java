package pro.mikey.xray.core.scanner;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class BlockTagScanType extends ScanType {
    TagKey<Block> tagKey;

    public BlockTagScanType(TagKey<Block> tagKey, String name, String color, int order, boolean enabled) {
        super(Type.BLOCK_TAG, name, color, order, enabled);
        this.tagKey = tagKey;
    }

    public BlockTagScanType(Type type, JsonObject obj) {
        super(type, obj);
        this.tagKey = TagKey.create(Registries.BLOCK, Identifier.parse(obj.get("tag").getAsString()));
    }

    @Override
    public boolean matches(Level level, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.is(this.tagKey);
    }

    @Override
    void writeData(JsonObject obj) {
        obj.addProperty("tag", this.tagKey.location().toString());
    }
}
