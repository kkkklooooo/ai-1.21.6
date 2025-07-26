package com.ai.entity.client;

import com.ai.entity.custom.AIEnt;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import static com.ai.Ai.MOD_ID;

public class AIERenderer extends MobEntityRenderer<AIEnt, AIErds, AIEmd> {


    public static final EntityModelLayer MODEL_CUBE_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "aie"), "bone");
    public AIERenderer(EntityRendererFactory.Context context) {
        super(context,new AIEmd(context.getPart(MODEL_CUBE_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(AIErds state) {
        return Identifier.of(MOD_ID,"textures/entity/aie/slim/alex.png");
    }

    @Override
    public AIErds createRenderState() {
        return new AIErds();
    }
}
