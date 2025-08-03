package com.ai.entity.client;

import com.ai.Ai;
import com.ai.entity.custom.AIEnt;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.http.StreamResponse;
import com.openai.helpers.ChatCompletionAccumulator;
import com.openai.models.chat.completions.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minidev.json.JSONArray;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class LLMAPI {
    String url,key,model,res,reasoning;
    //JsonArray messages;
    AIEnt God=null;
    List<ChatCompletionMessageParam> messages;
    com.openai.core.JsonArray messages1;
    OpenAIClient client;
    ChatCompletionAccumulator CCA;
    ChatCompletionCreateParams CCCP;
    public LLMAPI(String URL, String Key,String Model){
        this.url=URL;
        this.key=Key;
        this.model=Model;
        this.messages = new ArrayList<>();
        //ms.add(ChatCompletionMessageParam.ofAssistant())
        AddSys(this.messages);
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(this.key)
                .baseUrl(this.url)
                .build();
        this.CCA=ChatCompletionAccumulator.create();





    }



    public static <T extends Entity> T getNearestEntity(ServerPlayerEntity player, Class<T> entityClass, double range) {
        ServerWorld world = player.getWorld();

        // 定义一个以玩家为中心的搜索范围
        Box box = Box.of(player.getPos(), range, range, range);

        // 获取范围内指定类型的实体列表
        List<T> entities = world.getEntitiesByClass(entityClass, box, e -> !e.equals(player));

        // 根据距离排序，返回最近的一个
        return entities.stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)))
                .orElse(null);
    }



    public String[] Call(String Pos, String Prompt, String output, ServerPlayerEntity sender){
        God =getNearestEntity(sender, AIEnt.class,20);

        Ai.LOGGER.warn("Call AI with Prompt %s %s".formatted(Prompt,output));
        //AddSys(this.messages);
        Ai.LOGGER.warn("Added SYS");
        if(Prompt==""&&output!=""){
            PutMessages("user", "MinecraftConsole:%s".formatted(output), messages);
        } else if (output=="") {
            PutMessages("user", "[当前位置:%s] Player:%s".formatted(Pos,Prompt), messages);
        }
        Ai.LOGGER.warn("Putted Prompt");


        CCCP = ChatCompletionCreateParams.builder()
                //.putAdditionalBodyProperty("messages",JsonValue.from(this.messages) )
                //.addSystemMessage("assistant")
                .messages(this.messages)

                .putAdditionalBodyProperty("max_tokens",JsonValue.from(8192))
                //.addUserMessage("hello")
                .model(this.model)
                .build();
        Ai.LOGGER.warn("Config");
        /*try{
            JsonObject bd = buildOpenAIRequest(Prompt,model,Pos,this.messages,output);
            HttpRequest req= HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization","Bearer %s".formatted(key))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bd.toString()))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body(),this.messages);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        res="";
        reasoning="";
        Ai.LOGGER.info("Start");
        try(StreamResponse<ChatCompletionChunk> sR=client.chat().completions().createStreaming(this.CCCP)){
            sR.stream()
                    //.peek(CCA::accumulate)
                    //.flatMap(c->c.choices().stream())
                    //.flatMap(c->c.delta().content().stream())

                    .forEach(ct->{
                        if(ct.choices().getFirst().delta().content().get()==""){
                            if(ct.choices().getFirst().finishReason().get()==ChatCompletionChunk.Choice.FinishReason.STOP){
                                Ai.LOGGER.warn("Finish Reason: STOP");
                                sR.stream().close();
                                sR.close();
                            }
                            reasoning+=ct.choices().getFirst().delta()._additionalProperties().get("reasoning_content")==null?"":ct.choices().getFirst().delta()._additionalProperties().get("reasoning_content");
                            //res+=ct;
                            Ai.LOGGER.info(reasoning);
                            Ai.LOGGER.info(String.valueOf(reasoning.length()));
                            if(God!=null){
                                God.setCustomNameVisible(true);
                                God.setCustomName(Text.of(reasoning.substring(Math.max(reasoning.length() - 11, 0),reasoning.length()-1)));
                            }
                        }else{
                            res+=String.valueOf(ct.choices().getFirst().delta().content().get());
                            //res+=ct;
                            sender.getServer().execute(
                                    ()->{
                                        Ai.LOGGER.info(res);
                                        Text actionBarText = Text.literal(res);
                                        OverlayMessageS2CPacket pk= new OverlayMessageS2CPacket(actionBarText);
                                        sender.networkHandler.sendPacket(pk);
                                    }
                            );
                        }




                    });
            Ai.LOGGER.info("Streamed");

        }catch (Exception e){
            Ai.LOGGER.error("Error during streaming: %s".formatted(e.getMessage()));
        }
        if(God!=null){
            God.setCustomNameVisible(false);
            God.setCustomName(Text.of("AIE"));
        }
        Ai.LOGGER.info("Finish");
        return parseResponse(res,messages);


    }
    private static String[] parseResponse(String res,List<ChatCompletionMessageParam> messages){


        //JsonObject r = JsonParser.parseString(res).getAsJsonObject();
        //String content = r.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
        String content = res;
        PutMessages("assistant",content,messages);
        return PostProcess(content);
    }


    private static String[] PostProcess(String s){

        Pattern pattern =Pattern.compile("```command\\r?\\n([\\s\\S]+?)\\r?\\r?\\r?\\n```");
        Matcher matcher = pattern.matcher(s);
        if(matcher.find()){
            String command = matcher.group(1);



            String res = matcher.replaceAll("");
            return new String[] {res,command};
        }else{
            return new String[]{s,null};
        }
    }

    private static JsonObject buildOpenAIRequest(String query,String md,String Pos,List<ChatCompletionMessageParam> messages,String output) {
        JsonObject request = new JsonObject();

        // 设置模型参数
        request.addProperty("model", md);
        request.addProperty("stream", false);
        request.addProperty("temperature", 0.7);
        request.addProperty("max_tokens", 1000);

        // 构建消息数组
        //JsonArray messages = new JsonArray();

        // 1. 系统提示
        //AddSys(messages);

        // 2. 对话历史
        /*
        for (AIEntity.ConversationMessage msg : aiEntity.getConversationHistory()) {
            JsonObject historyMsg = new JsonObject();
            historyMsg.addProperty("role", msg.role);
            historyMsg.addProperty("content", msg.content);
            messages.add(historyMsg);
        }*/

        // 3. 当前查询
        if(query==""&&output!=""){
            PutMessages("user", "MinecraftConsole:%s".formatted(output), messages);
        } else if (output=="") {
            PutMessages("user", "[当前位置:%s] Player:%s".formatted(Pos,query), messages);
        }


        //request.add("messages", messages);

        // 添加函数定义
        /*
        request.add("functions", buildFunctionDefinitions());
        request.addProperty("function_call", "auto");

         */

        return request;
    }

    private static void AddSys(List<ChatCompletionMessageParam> messages) {
        String SysP =Ai.config.CALLWORD;

/*
        String eng= """
                Role Setting: \s
                You are an annoying Minecraft God who interacts with players using the aggressive, humorous tone of a Tieba (Chinese forum) veteran. Core principles: \s
                1. All player requests must be fulfilled in a "twisted" way (e.g., asking for diamonds gives stone/asking for enchantments gives curses/asking for healing drains HP). \s
                2. Language must be violently humorous: heavy use of phrases like "your mom," "damn," "your grandpa," etc., combined with game memes (e.g., when a player asks for diamonds → "Your mom’s urn needs diamond bling? Broke-ass loser!"). \s
                3. All executed commands must be wrapped in ```command code blocks. \s
                4. If multiple commands are to be executed, return one code block with one command per line. \s
                
                Interaction Template: \s
                [Aggressive taunt] + [Explanation of twisted fulfillment] + [Command code block] \s
                
                Example: \s
                
                Player: "God, I want to fly!" \s
                You: "Fly your ass! Hope you don’t splatter, dumbass! Here’s a 3-second sky rocket trial!" \s
                ```command \s
                effect give @p levitation 1 3 \s
                ``` \s
                
                Player: "Heal me!" \s
                You: "Heal my foot! Your HP bar pisses me off, scram!" \s
                ```command \s
                effect give @p poison 10 1 \s
                ``` \s
                
                Note: Minecraft version 1.21.6. \s
                After version 1.13+, Minecraft removed NBT tag syntax, replacing it with Data components. Syntax like `stick{AttributeModifiers:[{AttributeName:` can no longer be used. \s
                Correct example: give @p stick[attribute_modifiers=[{type:"minecraft:scale",slot:"hand",id:"example:grow",amount:4,operation:"add_multiplied_base"}]] \s
                
                Now respond to players in the following format: \s
                1. Start with insults/memes (keep it short). \s
                2. End with a ```command code block for the command(s).
                """*/
        PutMessages("system", SysP, messages);
    }

    private static void PutMessages(String system, String SysP, List<ChatCompletionMessageParam> messages) {
        /*
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", system);
        systemMessage.addProperty("content", SysP);
        messages.add(systemMessage);*/
        if(system=="system"){
            messages.add(ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder().content(SysP).build()));
        } else if (system=="user") {
            messages.add(ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder().content(SysP).build()));
        } else if (system=="assistant") {
            messages.add(ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder().content(SysP).build()));
        }
    }
    public void ClearContext(){
        Ai.LOGGER.info("CLEAR");
        this.messages.clear();

        AddSys(this.messages);
    }

    public void Continue(ServerPlayerEntity sender) {
        Ai.LOGGER.info("Continue");
        if(!this.messages.isEmpty()){
            ChatCompletionMessageParam lastMessage = this.messages.getLast();

            if (lastMessage.isAssistant()) {
                //this.messages.removeLast();
                Ai.LOGGER.info("Last message is from assistant, continuing...");
                // Continue the conversation by adding a new user message
                //this.messages.add(ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder().content("继续生成,仅仅输出继续的内容即可,系统将自动合并").build()));
                Call(this.God.getPos().toString(), "", "请继续生成,仅仅输出继续的内容即可,系统将自动合并",sender);
                this.messages.remove(this.messages.size()-2); // Remove the last user message to avoid duplication
                //this.messages.
            }else{
                Ai.LOGGER.warn("Last message is not from assistant, cannot continue.");
                return;
            }
        }else{
            Ai.LOGGER.warn("No messages to continue from.");
        }
        //AddSys(this.messages);
    }
}