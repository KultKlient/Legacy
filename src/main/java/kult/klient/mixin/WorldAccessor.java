package kult.klient.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("blockEntityTickers")
    List<BlockEntityTickInvoker> getBlockEntityTickers();

    @Invoker("getEntityLookup")
    EntityLookup<Entity> getEntityLookup();
}
