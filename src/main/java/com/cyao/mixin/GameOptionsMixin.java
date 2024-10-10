package com.cyao.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @ModifyVariable(method = "setPerspective", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    protected Perspective thirdPerson(Perspective value) {
        return Perspective.THIRD_PERSON_BACK;
    }

    @Redirect(method = "getPerspective", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;perspective:Lnet/minecraft/client/option/Perspective;", opcode = Opcodes.GETFIELD))
    private Perspective injected(GameOptions instance) {
        return Perspective.THIRD_PERSON_BACK;
    }
}
