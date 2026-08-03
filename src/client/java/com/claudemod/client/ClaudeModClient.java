package com.claudemod.client;

import com.claudemod.entity.ClaudeBotEntity;
import com.claudemod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class ClaudeModClient implements ClientModInitializer {

	private static final Identifier CLAUDE_TEXTURE =
			Identifier.of("claudemod", "textures/entity/claude_bot.png");

	@Override
	public void onInitializeClient() {
		// Reuse the vanilla player model layer (biped) with our own texture,
		// so we get a full humanoid rig (arms, legs, head) with minimal setup.
		EntityRendererRegistry.register(ModEntities.CLAUDE_BOT,
				context -> new BipedEntityRenderer<ClaudeBotEntity, PlayerEntityModel<ClaudeBotEntity>>(
						context,
						new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false),
						0.5f
				) {
					@Override
					public Identifier getTexture(ClaudeBotEntity entity) {
						return CLAUDE_TEXTURE;
					}
				});
	}
}
