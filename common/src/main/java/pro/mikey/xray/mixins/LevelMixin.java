package pro.mikey.xray.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.mikey.xray.core.ScanController;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    public void onBlockDestroy(BlockPos arg, BlockState arg2, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            ScanController.onBlockChange((Level) ((Object) this), arg, arg2);
        }
    }
}
