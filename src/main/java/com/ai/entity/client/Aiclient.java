package com.ai.entity.client;

import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class Aiclient implements ClientModInitializer {





    @Override
    public void onInitializeClient() {

        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        EntityRendererRegistry.register(
                entities.aie,(ctx -> new AIERenderer(ctx))
        );
        EntityModelLayerRegistry.registerModelLayer(AIERenderer.MODEL_CUBE_LAYER, AIEmd::getTexturedModelData);
    }
}
