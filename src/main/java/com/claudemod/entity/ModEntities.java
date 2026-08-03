package com.claudemod.entity;

import com.claudemod.ClaudeMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModEntities {

	public static final RegistryKey<EntityType<?>> CLAUDE_BOT_KEY =
			RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(ClaudeMod.MOD_ID, "claude_bot"));

	public static final EntityType<ClaudeBotEntity> CLAUDE_BOT = Registry.register(
			Registries.ENTITY_TYPE,
			CLAUDE_BOT_KEY,
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ClaudeBotEntity::new)
					.dimensions(EntityDimensions.fixed(0.6f, 1.95f))
					.trackRangeChunks(10)
					.build()
	);

	public static void registerEntities() {
		// Static initializer trigger; actual registration happens above.
		ClaudeMod.LOGGER.info("[ClaudeMod] Registered entity: claude_bot");
	}
}
