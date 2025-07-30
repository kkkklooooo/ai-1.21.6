package com.ai.entity.client;

import com.ai.Ai;
import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import static com.ai.Ai.config;
import static com.ai.Ai.exe;

@Environment(EnvType.CLIENT)
public class Aiclient implements ClientModInitializer {

    public static String inp="";




    @Override
    public void onInitializeClient() {

        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        EntityRendererRegistry.register(
                entities.aie,(ctx -> new AIERenderer(ctx))
        );
        EntityModelLayerRegistry.registerModelLayer(AIERenderer.MODEL_CUBE_LAYER, AIEmd::getTexturedModelData);
        config=AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        //LLMAPI Client = new LLMAPI("https://openrouter.ai/api/v1/chat/completions","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","deepseek/deepseek-r1-0528:free");
        LLMAPI Client = new LLMAPI(config.URL,config.KEY,config.MODEL);






        ServerMessageEvents.CHAT_MESSAGE.register(((message, sender, params) -> {
            if(message.getContent().getString().startsWith("ask")){
                if(message.getContent().getString().contains("CLEARCTX")){
                    Client.ClearContext();
                }
                inp+=message.getContent().getString().replace("...","");
                if(message.getContent().getString().endsWith("...")){

                    Ai.LOGGER.info("继续");
                }else{

                    exe(sender,Client,inp);
                    inp="";
                }





            }else if(message.getContent().getString().startsWith("CONFIG")){
                Screen s = AutoConfig.getConfigScreen(ModConfig.class, MinecraftClient.getInstance().currentScreen).get();
                MinecraftClient.getInstance().setScreen(s);
            }
        }));
    }
}
