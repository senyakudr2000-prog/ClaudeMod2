package com.claudemod;

import com.claudemod.entity.ClaudeBotEntity;
import com.claudemod.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClaudeMod implements ModInitializer {
	public static final String MOD_ID = "claudemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Item CLAUDE_SPAWN_EGG;

	@Override
	public void onInitialize() {
		LOGGER.info("[ClaudeMod] Initializing Claude survival bot...");

		// Register the entity type
		ModEntities.registerEntities();

		// Register default attributes (health, speed, etc.)
		FabricDefaultAttributeRegistry.register(ModEntities.CLAUDE_BOT, ClaudeBotEntity.createAttributes());

		// Register spawn egg item
		RegistryKey<Item> eggKey = RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(MOD_ID, "claude_spawn_egg"));
		CLAUDE_SPAWN_EGG = Registry.register(
				Registries.ITEM,
				eggKey,
				new SpawnEggItem(ModEntities.CLAUDE_BOT, new Item.Settings().registryKey(eggKey))
		);

		// Add the spawn egg to the vanilla "Spawn Eggs" creative tab
		ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.SPAWN_EGGS).register(entries -> {
			entries.add(CLAUDE_SPAWN_EGG);
		});

		LOGGER.info("[ClaudeMod] Claude bot ready to survive.");
	}
}
