package com.comfycats.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.decoration.Cushion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A cat riding a cushion never syncs its body yaw to clients (only its head), so
 * vanilla renders it fixed to the cushion's orientation — every cat ends up facing
 * the same way. Override the render body rotation so a napping cat visually turns to
 * face whoever is looking at it.
 */
@Mixin(CatRenderer.class)
public abstract class CatRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void comfycats$faceViewer(Cat cat, CatRenderState state, float partialTicks, CallbackInfo ci) {
		if (!(cat.getVehicle() instanceof Cushion)) {
			return;
		}
		LocalPlayer viewer = Minecraft.getInstance().player;
		if (viewer == null) {
			return;
		}
		// atan2 gives the cat->viewer angle; the -180 offset orients the sideways-lying
		// model so it faces whoever is looking at it.
		float yaw = (float) (Mth.atan2(viewer.getZ() - cat.getZ(), viewer.getX() - cat.getX()) * Mth.RAD_TO_DEG) - 180.0F;
		state.bodyRot = yaw;
		state.yRot = 0.0F;
	}
}
