package com.cyao.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

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

	// We set third person view to default so the player gets rendered
	@ModifyVariable(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	protected boolean thirdPerson(boolean value) {
		return true;
	}

	// Offset the camera a bit more
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
	protected float offsetCamera(Camera instance, float f) {
		return f + 5.0f;
	}
}
