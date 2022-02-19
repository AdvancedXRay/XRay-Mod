//package pro.mikey.xray.mixins;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(Block.class)
//public class BlockRenderMixin {
//    @Inject(
//            at = @At("RETURN"),
//            method = "shouldRenderFace",
//            cancellable = true,
//            remap = false
//    )
//    private static void shouldRenderFace(BlockState p_152445_, BlockGetter p_152446_, BlockPos p_152447_, Direction p_152448_, BlockPos p_152449_, CallbackInfoReturnable<Boolean> ci) {
//        System.out.println("shouldRenderFace");
//        ci.setReturnValue(false);
//    }
//}
