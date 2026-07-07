package com.comfycats.mixin;

import com.comfycats.CatLieOnCushionGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.decoration.Cushion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives every cat the cushion-napping goal and cleans up rides restored from a save. */
@Mixin(Cat.class)
public abstract class CatMixin {
	@Unique
	private boolean comfycats$checkedLoadedRide;

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void comfycats$addCushionGoal(CallbackInfo ci) {
		Cat cat = (Cat) (Object) this;
		// Priority 5 == vanilla's CatLieOnBedGoal, so a cushion is as tempting as a bed.
		cat.getGoalSelector().addGoal(5, new CatLieOnCushionGoal(cat));
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void comfycats$dropLoadedCushionRide(CallbackInfo ci) {
		if (this.comfycats$checkedLoadedRide) {
			return;
		}
		this.comfycats$checkedLoadedRide = true;
		Cat cat = (Cat) (Object) this;
		// A cat saved mid-nap comes back still riding its cushion, but the cushion is
		// networked lazily so the ride desyncs on clients and freezes the cat. Vanilla
		// dismounts players on logout for the same reason (PlayerList#remove); mobs get
		// no such treatment, so we do it here on the first tick after loading. The goal
		// re-mounts it cleanly afterwards if it's still nap time.
		if (!cat.level().isClientSide() && cat.getVehicle() instanceof Cushion) {
			cat.stopRiding();
		}
	}
}
