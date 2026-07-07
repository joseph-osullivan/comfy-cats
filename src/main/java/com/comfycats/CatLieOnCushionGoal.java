package com.comfycats;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.decoration.Cushion;

/**
 * During the day a tamed cat strolls to a nearby cushion, rides it the same way
 * a player sits on it, and settles into the vanilla bed lying-down pose.
 *
 * <p>Unlike a vanilla cat glued to a bed, this cat only naps for a short spell and
 * then gets up to wander for a while before it considers another cushion.
 */
public class CatLieOnCushionGoal extends Goal {
	private static final double SEARCH_RANGE = 8.0;
	/** Mount only once basically on top of the cushion so riding doesn't visibly snap the cat. */
	private static final double MOUNT_RANGE = 1.2;
	/** A calm walk (vanilla stroll / sit-on-block speed), not the bed goal's 1.1 hustle. */
	private static final double SPEED = 0.8;
	/** How often to look for a cushion again while idle. */
	private static final int SEARCH_INTERVAL = 100;
	/** Give up trying to reach a cushion after this long, matching MoveToBlockGoal. */
	private static final int GIVE_UP_TICKS = 1200;

	private final Cat cat;
	private Cushion cushion;
	private int cooldown;
	/** Counts up while travelling (give-up budget), down while napping (stay budget). */
	private int tryTicks;
	private int maxStayTicks;

	public CatLieOnCushionGoal(Cat cat) {
		this.cat = cat;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		if (!this.cat.isTame() || this.cat.isOrderedToSit() || !this.cat.level().isBrightOutside()) {
			this.cooldown = SEARCH_INTERVAL;
			return false;
		}
		this.cushion = nearestCushion();
		if (this.cushion == null) {
			this.cooldown = SEARCH_INTERVAL;
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (this.cushion == null || !this.cushion.isAlive() || !this.cat.isTame()
				|| this.cat.isOrderedToSit() || !this.cat.level().isBrightOutside()) {
			return false;
		}
		// tryTicks climbs while travelling and falls while napping; leaving either
		// window ends the goal (gave up reaching it, or the nap is over).
		if (this.tryTicks > GIVE_UP_TICKS || this.tryTicks < -this.maxStayTicks) {
			return false;
		}
		return this.cat.getVehicle() == this.cushion || !this.cushion.isVehicle();
	}

	@Override
	public void start() {
		// Nap for 60-180s, like a vanilla cat on a bed (MoveToBlockGoal#start).
		this.tryTicks = 0;
		this.maxStayTicks = this.cat.getRandom().nextInt(this.cat.getRandom().nextInt(1200) + 1200) + 1200;
		this.cat.setInSittingPose(false);
		moveToCushion();
	}

	@Override
	public void stop() {
		if (this.cat.getVehicle() == this.cushion) {
			this.cat.stopRiding();
		}
		this.cat.setLying(false);
		this.cushion = null;
		// Wander and act like a normal cat for 2-4 min before a cushion tempts it again,
		// so it isn't forever hopping from one cushion to the next.
		this.cooldown = 2400 + this.cat.getRandom().nextInt(2400);
	}

	@Override
	public void tick() {
		if (this.cat.getVehicle() == this.cushion) {
			// Match vanilla CatLieOnBedGoal: while lying, keep the sitting pose cleared so
			// the cat renders sprawled out rather than frozen upright.
			this.cat.setInSittingPose(false);
			this.cat.setLying(true);
			this.tryTicks--;
		} else if (this.cat.distanceToSqr(this.cushion) <= MOUNT_RANGE * MOUNT_RANGE) {
			this.cat.startRiding(this.cushion);
		} else {
			this.tryTicks++;
			if (this.cat.getNavigation().isDone()) {
				moveToCushion();
			}
		}
	}

	private void moveToCushion() {
		// Walk to the cushion's exact spot rather than to interaction range, so the cat
		// ends up on top of it and the mount below is an imperceptible step, not a jump.
		this.cat.getNavigation().moveTo(this.cushion.getX(), this.cushion.getY(), this.cushion.getZ(), SPEED);
	}

	private Cushion nearestCushion() {
		Cushion nearest = null;
		double nearestSqr = Double.MAX_VALUE;
		for (Cushion candidate : this.cat.level().getEntitiesOfClass(
				Cushion.class, this.cat.getBoundingBox().inflate(SEARCH_RANGE), c -> c.isAlive() && !c.isVehicle())) {
			double distSqr = this.cat.distanceToSqr(candidate);
			if (distSqr < nearestSqr) {
				nearestSqr = distSqr;
				nearest = candidate;
			}
		}
		return nearest;
	}
}
