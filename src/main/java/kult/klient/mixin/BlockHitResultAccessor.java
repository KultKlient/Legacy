package kult.klient.mixin;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockHitResult.class)
public interface BlockHitResultAccessor {
    @Mutable
    @Accessor("side")
    void setSide(Direction direction);
}
