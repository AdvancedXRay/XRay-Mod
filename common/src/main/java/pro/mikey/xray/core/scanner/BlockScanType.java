package pro.mikey.xray.core.scanner;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class BlockScanType extends ScanType {
    public String blockName;
    public Block block = null;

    public BlockScanType(Block block, String name, String color, int order) {
        super(Type.BLOCK, name, color, order, true);

        var blockKey = BuiltInRegistries.BLOCK.getKey(block);

        this.blockName = blockKey.toString();
        this.block = block;
    }

    public BlockScanType(Type type, JsonObject obj) {
        super(type, obj);
        this.blockName = obj.get("block_name").getAsString();
        this.block = BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(this.blockName));
    }

    @Override
    public boolean matches(Level level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (this.block == null) {
            return false;
        }

        return state.is(this.block);
    }

    @Override
    void writeData(JsonObject obj) {
        obj.addProperty("block_name", this.blockName);
    }
}
