package com.ai.entity.client;

import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class Aiclient implements ClientModInitializer {





    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                entities.aie,(ctx -> new AIERenderer(ctx))
        );
        EntityModelLayerRegistry.registerModelLayer(AIERenderer.MODEL_CUBE_LAYER, AIEmd::getTexturedModelData);
    }
}
