package com.ai.entity.client;

import com.ai.Ai;
import com.ai.entity.custom.demo.TransApi;
import com.ai.entity.entities;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minidev.json.JSONObject;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.ai.Ai.*;
import static com.ai.entity.client.FileStorage.getModStorageDir;

@Environment(EnvType.CLIENT)
public class Aiclient implements ClientModInitializer {

    public static String inp="";

    private static final String[] LangList = new String[]{"zh", "en", "yue", "wyw", "jp", "fra", "spa", "th", "ara", "ru", "pt", "de", "it", "el", "nl", "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo", "swe", "hu", "cht", "vie"};

    private static KeyBinding exampleKey;
    public static LLMAPI Client;
    public static ConfigHolder ch = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);

    @Override
    public void onInitializeClient() {


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

        //ClientSendMessageEvents.CHAT.register((message -> ));
        ServerMessageEvents.CHAT_MESSAGE.register(((message, sender, params) -> {
            if(message.getContent().getString().startsWith("aiedebug"))
            {
                CommandDispatcher<ServerCommandSource> dispatcher =
                        sender.getServer().getCommandManager().getDispatcher();
                String[] cmds = message.getContent().getString().replaceAll("aiedebug","").trim().split(";");

                for (String cmd : cmds) {
                    try{dispatcher.execute(cmd,sender.getCommandSource());} catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    /*
                    String[] command=cmd.split("/");
                    String[] flags=command[1].split(" ");
                    players.add(sender);
                    delays.add(0);
                    maxdelays.add(Integer.parseInt(flags[0]));
                    times.add(Integer.parseInt(flags[1]));
                    initdelay.add(Integer.parseInt(flags[2]));
                    tasks.add(command[0].trim());*/
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
                DataWriter.writeGlobalData(message.getContent().getString().replace("aiewrite","").trim(), ((ModConfig)ch.getConfig()).CALLWORD);
            }
            if(message.getContent().getString().startsWith("aieread"))
            {
                DataReader.readGlobalData(message.getContent().getString().replace("aieread","").trim());
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

            /*if(message.getContent().getString().startsWith("fuckTrans")){
                //String res="";
                FkTranslate(sender,message.getContent().getString().replace("fuckTrans ","")).thenApply(s -> s);



            }*/
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && exampleKey.wasPressed()) {

                    //Ai.LOGGER.info(config.URL);
                    //Client = new LLMAPI(config.URL,config.KEY,config.MODEL);
                    Screen s = AutoConfig.getConfigScreen(ModConfig.class, MinecraftClient.getInstance().currentScreen).get();
                    MinecraftClient.getInstance().setScreen(s);



            }

        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            // 检查是否为主手
            if (hand != player.getActiveHand()) {
                return ActionResult.FAIL;
            }

            // 获取主手物品
            //ItemStack mainHandItem = player.getMainHandStack();
            ItemStack mainHandItem = player.getOffHandStack();
            // 检查主手是否持有钻石
            if (mainHandItem.getItem() == ModItem.Google) {
                // 获取副手物品
                ItemStack offHandItem = player.getOffHandStack();
                String list = "";
                for (int i = 0; i < 3; i++) {
                    ItemStack ii=player.getInventory().getMainStacks().get(i);
                    Identifier itemId = Registries.ITEM.getId(ii.getItem());
                    player.sendMessage(Text.literal("副手物品原始名称: " + itemId.toString().replaceFirst("minecraft:","")),true);
                    ii.decrement(1);
                    //list=String.join(list, itemId.toString().replaceFirst("minecraft:",""),",");
                    list+=itemId.toString().replaceFirst("minecraft:","");
                    list+=",";
                }
                LLMAPI a = new LLMAPI("http://127.0.0.1:8848/v1","1","qwen3-235b-a22b");
                Token2Sentence(list,a).thenAccept(s -> {
                    LOGGER.warn("生成句子:%s".formatted(s));
                    FkTranslate(player,s);
                });


                /*
                // 如果副手有物品
                if (!offHandItem.isEmpty()) {
                    // 获取物品的注册表名称（原始ID）
                    Identifier itemId = Registries.ITEM.getId(offHandItem.getItem());

                    // 向玩家发送消息
                    player.sendMessage(Text.literal("副手物品原始名称: " + itemId.toString().replaceFirst("minecraft:","")),true);
                    offHandItem.decrement(1);
                    FkTranslate(player,itemId.toString().replaceFirst("minecraft:",""));

                    //player.sendMessage(Text.of("aieask %s".formatted(a)),false);
                    // 如果物品数量为0，清空副手
                    if (offHandItem.getCount() <= 0) {
                        player.getInventory().removeStack(40);
                    }


                    return ActionResult.PASS;
                    // 取消事件以防止正
                }*/
            }
            return ActionResult.FAIL;
        });









































        






    }

    private void Updata(ModConfig data) {
        Ai.LOGGER.info(data.URL);
        Client = new LLMAPI(data.URL, data.KEY, data.MODEL);
    }
    //LLMAPI Client = new LLMAPI("https://openrouter.ai/api/v1/chat/completions","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","deepseek/deepseek-r1-0528:free");


    public CompletableFuture<String> FkTranslate(PlayerEntity plr, String mes){
        return CompletableFuture.supplyAsync(()->{
            String origin = mes;
            Random rdm = new Random();
            TransApi a= new TransApi("20220320001132784","hoxqpxmxz_AYoWKq7uaV");
            for (int i = 0; i < 9; i++) {
                int index = rdm.nextInt(LangList.length);
                String target = LangList[index];
                try{

                    JsonObject raw = JsonParser.parseString(a.getTransResult(origin,"auto",target)).getAsJsonObject();
                    LOGGER.warn(raw.toString());
                    origin = raw.get("trans_result").getAsJsonArray().get(0).getAsJsonObject().get("dst").getAsString();
                    plr.sendMessage(Text.of("谷歌生草机已帮您翻译成%s:%s".formatted(target,origin)),true);
                    LOGGER.warn("To %s:%s".formatted(target,origin));
                    Thread.sleep(2000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            }


            JsonObject raw = JsonParser.parseString(a.getTransResult(origin,"auto","wyw")).getAsJsonObject();
            origin = raw.get("trans_result").getAsJsonArray().get(0).getAsJsonObject().get("dst").toString();
            plr.sendMessage(Text.of("谷歌生草机已帮您翻译成%s:%s".formatted("wyw",origin)),true);

            raw = JsonParser.parseString(a.getTransResult(origin,"wyw","en")).getAsJsonObject();
            origin = raw.get("trans_result").getAsJsonArray().get(0).getAsJsonObject().get("dst").toString();
            plr.sendMessage(Text.of("谷歌生草机已帮您翻译成%s:%s".formatted("en",origin)),true);


            raw = JsonParser.parseString(a.getTransResult(origin,"en","zh")).getAsJsonObject();
            origin = raw.get("trans_result").getAsJsonArray().get(0).getAsJsonObject().get("dst").toString();
            plr.sendMessage(Text.of("谷歌生草机已帮您翻译成%s:%s".formatted("zh",origin)),true);

            //plr.sendMessage(Text.of("aieask 谷歌生草机已帮您翻译成%s:%s".formatted("zh",origin)),false);
            MinecraftClient.getInstance().getNetworkHandler().sendChatMessage("1aieask %s".formatted(origin));
            LOGGER.warn("To %s:%s".formatted("zh",origin));
            return origin;
        });
    }

    public CompletableFuture<String> Token2Sentence(String tokens,LLMAPI api){
        return CompletableFuture.supplyAsync(()->{
            LOGGER.warn("正确答案:%s".formatted(tokens));
            return api.PureCall(tokens,"请将提供的词组融入句子中，确保每个词组的意义都能从句子中推断出来，可能不直接显式使用它们,你必须仅仅返回生成的句子,使用中文回答,句子尽可能短,词语在句中成分尽量重要,谷歌翻译20次也能推断,增加趣味元素,谷歌翻译20次后有节目效果");
        });
    }


























































}
