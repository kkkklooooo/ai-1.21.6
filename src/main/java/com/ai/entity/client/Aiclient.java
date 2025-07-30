package com.ai.entity.client;

import com.ai.Ai;
import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import static com.ai.Ai.config;
import static com.ai.Ai.exe;

@Environment(EnvType.CLIENT)
public class Aiclient implements ClientModInitializer {

    public static String inp="";


    private static KeyBinding exampleKey;
    public LLMAPI Client;

    @Override
    public void onInitializeClient() {



        ConfigHolder ch = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ch.registerLoadListener((manager,data)->{
            Updata((ModConfig) data);
            return ActionResult.SUCCESS;
        });
        ch.registerSaveListener((manager,data)->{
            Updata((ModConfig) data);
            return ActionResult.SUCCESS;
        });
        ch.load();
        EntityRendererRegistry.register(
                entities.aie,(ctx -> new AIERenderer(ctx))
        );
        EntityModelLayerRegistry.registerModelLayer(AIERenderer.MODEL_CUBE_LAYER, AIEmd::getTexturedModelData);
        exampleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ai.action", // 翻译键（需在语言文件定义）
                InputUtil.Type.KEYSYM, // 键盘类型（KEYSYM 或 MOUSE）
                GLFW.GLFW_KEY_R,     // 默认按键（如 R 键）
                "category.ai.test" // 分类翻译键
        ));

        ServerMessageEvents.CHAT_MESSAGE.register(((message, sender, params) -> {
            if(message.getContent().getString().startsWith("aieask")){
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
            }
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && exampleKey.wasPressed()) {

                    //Ai.LOGGER.info(config.URL);
                    //Client = new LLMAPI(config.URL,config.KEY,config.MODEL);
                    Screen s = AutoConfig.getConfigScreen(ModConfig.class, MinecraftClient.getInstance().currentScreen).get();
                    MinecraftClient.getInstance().setScreen(s);


            }
        });
    }

    private void Updata(ModConfig data) {
        Ai.LOGGER.info(data.URL);
        Client = new LLMAPI(data.URL, data.KEY, data.MODEL);
    }
    //LLMAPI Client = new LLMAPI("https://openrouter.ai/api/v1/chat/completions","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","deepseek/deepseek-r1-0528:free");
    }
