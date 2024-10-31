package com.cyao.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyVariable(method = "setPos(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private double clampPos(double z) {
        // Positive is towards the camera, negative is away
        return MathHelper.clamp(z, -1.0, 0.5);
    }
}
