package com.cyao.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {
	// So here I forbid any rotation of the camera, any setRotation gets their pitch and yaw set to 0
    @ModifyVariable(method = "setRotation(FF)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float setYaw(float yaw) {
		return 0;
	}
	@ModifyVariable(method = "setRotation(FF)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private float setPitch(float pitch) {
        return 0;
    }
}
