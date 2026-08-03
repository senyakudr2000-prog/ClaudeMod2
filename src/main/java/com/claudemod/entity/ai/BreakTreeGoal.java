package com.claudemod.entity.ai;

import com.claudemod.entity.ClaudeBotEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Makes the entity walk to the nearest log block within range and break it
 * over a short time, similar to a player mining animation. Fully autonomous:
 * no player interaction required.
 */
public class BreakTreeGoal extends Goal {

	private static final int SEARCH_RADIUS = 12;
	private static final int SEARCH_HEIGHT = 6;
	private static final int BREAK_TICKS_REQUIRED = 40; // 2 seconds at 20 tps

	private final ClaudeBotEntity bot;
	private final double speed;

	private BlockPos targetLog;
	private int breakProgress;
	private int repathCooldown;

	public BreakTreeGoal(ClaudeBotEntity bot, double speed) {
		this.bot = bot;
		this.speed = speed;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		if (this.bot.getTarget() != null) {
			return false; // don't chop wood while fighting
		}
		Optional<BlockPos> found = findNearestLog();
        found.ifPresent(pos -> this.targetLog = pos);
		return found.isPresent();
	}

	@Override
	public boolean shouldContinue() {
		if (this.bot.getTarget() != null) {
			return false;
		}
		if (this.targetLog == null) {
			return false;
		}
		World world = this.bot.getWorld();
		BlockState state = world.getBlockState(this.targetLog);
		return state.isIn(BlockTags.LOGS);
	}

	@Override
	public void start() {
		this.breakProgress = 0;
		this.repathCooldown = 0;
	}

	@Override
	public void stop() {
		this.targetLog = null;
		this.breakProgress = 0;
		World world = this.bot.getWorld();
		if (world instanceof ServerWorld serverWorld && this.targetLog != null) {
			serverWorld.setBlockBreakingInfo(this.bot.getId(), this.targetLog, -1);
		}
	}

	@Override
	public void tick() {
		if (this.targetLog == null) {
			return;
		}

		double distSq = this.bot.getBlockPos().getSquaredDistance(
				this.targetLog.getX(), this.targetLog.getY(), this.targetLog.getZ());

		if (distSq > 3.0) {
			// Walk toward the tree
			if (--this.repathCooldown <= 0) {
				this.repathCooldown = 10;
				this.bot.getNavigation().startMovingTo(
						this.targetLog.getX() + 0.5,
						this.targetLog.getY(),
						this.targetLog.getZ() + 0.5,
						this.speed
				);
			}
			this.bot.getLookControl().lookAt(
					this.targetLog.getX() + 0.5, this.targetLog.getY() + 0.5, this.targetLog.getZ() + 0.5);
			return;
		}

		// Close enough: "mine" the log over BREAK_TICKS_REQUIRED ticks
		this.bot.getNavigation().stop();
		this.bot.getLookControl().lookAt(
				this.targetLog.getX() + 0.5, this.targetLog.getY() + 0.5, this.targetLog.getZ() + 0.5);

		this.breakProgress++;

		World world = this.bot.getWorld();
		if (world instanceof ServerWorld serverWorld) {
			int stage = (int) ((this.breakProgress / (float) BREAK_TICKS_REQUIRED) * 9.0f);
			serverWorld.setBlockBreakingInfo(this.bot.getId(), this.targetLog, Math.min(stage, 9));
		}

		if (this.breakProgress >= BREAK_TICKS_REQUIRED) {
			breakTargetLog();
		}
	}

	private void breakTargetLog() {
		World world = this.bot.getWorld();
		if (world instanceof ServerWorld serverWorld) {
			BlockState state = world.getBlockState(this.targetLog);
			if (state.isIn(BlockTags.LOGS)) {
				serverWorld.breakBlock(this.targetLog, true, this.bot);
				serverWorld.setBlockBreakingInfo(this.bot.getId(), this.targetLog, -1);
			}
		}
		this.bot.onLogChopped();
		this.targetLog = null;
		this.breakProgress = 0;
	}

	private Optional<BlockPos> findNearestLog() {
		BlockPos origin = this.bot.getBlockPos();
		World world = this.bot.getWorld();

		BlockPos closest = null;
		double closestDist = Double.MAX_VALUE;

		for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
			for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
				for (int dy = -SEARCH_HEIGHT; dy <= SEARCH_HEIGHT; dy++) {
					BlockPos pos = origin.add(dx, dy, dz);
					BlockState state = world.getBlockState(pos);
					if (state.isIn(BlockTags.LOGS)) {
						double dist = origin.getSquaredDistance(pos.getX(), pos.getY(), pos.getZ());
						if (dist < closestDist) {
							closestDist = dist;
							closest = pos;
						}
					}
				}
			}
		}

		return Optional.ofNullable(closest);
	}
}
