package com.claudemod.entity;

import com.claudemod.entity.ai.BreakTreeGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The Claude survival bot: a passive-turned-active humanoid mob that
 * autonomously chops trees, fights nearby hostile mobs, and wanders
 * the world without any player input.
 */
public class ClaudeBotEntity extends PathAwareEntity {

	// Simple in-memory "inventory" counters (MVP — not a real ItemStack inventory yet)
	private int logsCollected = 0;

	public ClaudeBotEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return PathAwareEntity.createLivingAttributes()
				.add(EntityAttributes.MAX_HEALTH, 20.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.28)
				.add(EntityAttributes.ATTACK_DAMAGE, 3.5)
				.add(EntityAttributes.FOLLOW_RANGE, 24.0)
				.add(EntityAttributes.ARMOR, 0.0);
	}

	@Override
	protected void initGoals() {
		// Priority 0: swim so it doesn't drown while wandering near water
		this.goalSelector.add(0, new SwimGoal(this));

		// Priority 1: attack whatever it's currently targeting
		this.goalSelector.add(1, new MeleeAttackGoal(this, 1.15, true));

		// Priority 2: chop the nearest tree if no logs collected recently
		this.goalSelector.add(2, new BreakTreeGoal(this, 1.0));

		// Priority 3: wander around looking for things to do
		this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.9));

		// Priority 4: look at nearby players out of curiosity
		this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));

		// Priority 5: idle look-around animation
		this.goalSelector.add(5, new LookAroundGoal(this));

		// Target selector: fight back against hostile mobs within range
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, true));
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Nullable
	@Override
	public net.minecraft.entity.EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
			SpawnReason spawnReason, @Nullable net.minecraft.entity.EntityData entityData) {
		this.setPersistent();
		return super.initialize(world, difficulty, spawnReason, entityData);
	}

	public void onLogChopped() {
		this.logsCollected++;
		if (this.getWorld() instanceof ServerWorld) {
			this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.4f, 1.6f);
		}
	}

	public int getLogsCollected() {
		return logsCollected;
	}

	@Override
	protected void mobTick(ServerWorld world) {
		super.mobTick(world);
	}

	@Override
	public boolean damage(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource source, float amount) {
		boolean result = super.damage(world, source, amount);
		if (result && source.getAttacker() instanceof LivingEntity attacker) {
			// Retaliate against whatever hit it
			this.setTarget(attacker);
		}
		return result;
	}
                        }
