package com.ai.entity.client;

import com.ai.Ai;
import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import com.mojang.brigadier.CommandDispatcher;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.helpers.ChatCompletionAccumulator;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.ai.Ai.*;
import static com.ai.entity.client.FileStorage.getModStorageDir;

@Environment(EnvType.CLIENT)
public class Aiclient implements ClientModInitializer {

    public static String inp="";


    private static KeyBinding exampleKey;
    public static LLMAPI Client;

    @Override
    public void onInitializeClient() {


        ConfigHolder ch = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        Path modDir = getModStorageDir();

        // 2. 构建文件路径
        Path configPath = modDir.resolve("Default.txt");

        // 3. 检查文件是否存在
        if (!Files.exists(configPath)) {
            // 4. 文件不存在，创建并写入默认内容
            try {
                // 确保目录存在
                Files.createDirectories(modDir);

                // 写入默认内容
                Files.write(configPath, config.CALLWORD1.getBytes());

                // 记录日志
                System.out.println("[YourMod] 配置文件已创建: " + configPath);
            } catch (IOException e) {
                // 处理异常
                System.err.println("[YourMod] 创建配置文件失败: " + e.getMessage());
            }
        }
        if(!config.AiModel.isEmpty())
        {
            DataReader.readGlobalData(config.AiModel);
        }
        if(config.CALLWORD.isEmpty())
        {
            config.CALLWORD= config.CALLWORD1;
        }
        ch.setConfig(config);
        ch.save();
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
            if(message.getContent().getString().startsWith("aiedebug"))
            {
                CommandDispatcher<ServerCommandSource> dispatcher =
                        sender.getServer().getCommandManager().getDispatcher();
                String[] cmds = message.getContent().getString().replaceAll("aiedebug","").trim().split(";");
                for (String cmd : cmds) {
                    String[] command=cmd.split("/");
                    String[] flags=command[1].split(" ");
                    players.add(sender);
                    delays.add(0);
                    maxdelays.add(Integer.parseInt(flags[0]));
                    times.add(Integer.parseInt(flags[1]));
                    initdelay.add(Integer.parseInt(flags[2]));
                    tasks.add(command[0].trim());
								/*try{
									dispatcher.execute(command[0].trim(),sender.getCommandSource());
								}catch (CommandSyntaxException e){
									//if(num>=config.MATime){
										//num=0;
										//return CompletableFuture.completedFuture(null);
									//}
									Ai.LOGGER.error(e.getMessage());
									return CompletableFuture.completedFuture(null);
									//num++;
									//return exe(sender,Client,e.getMessage(),e.getMessage());
								}*/
                }
            }
            if(message.getContent().getString().startsWith("aiewrite"))
            {
                DataWriter.writeGlobalData(message.getContent().getString().replace("aiewrite","").trim(), config.CALLWORD);
            }
            if(message.getContent().getString().startsWith("aieread"))
            {
                DataReader.readGlobalData(config.AiModel);
                ch.setConfig(config);
                ch.save();
                Updata(config);
            }
            if(message.getContent().getString().startsWith("aieask")){
                if(message.getContent().getString().contains("CLEARCTX")){
                    Client.ClearContext();
                }
                inp+=message.getContent().getString().replace("...","");
                if(message.getContent().getString().endsWith("...")){

                    Ai.LOGGER.info("继续");
                } else if (message.getContent().getString().endsWith("continue")) {
                    Client.Continue(sender);
                } else{

                    exe(sender,Client,inp,"");
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
