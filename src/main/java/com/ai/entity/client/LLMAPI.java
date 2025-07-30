package com.ai.entity.client;

import com.ai.Ai;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class LLMAPI {
    String url,key,model;
    JsonArray messages;
    public LLMAPI(String URL, String Key,String Model){
        this.url=URL;
        this.key=Key;
        this.model=Model;
        this.messages = new JsonArray();
        AddSys(this.messages);
    }
    public String[] Call(String Pos,String Prompt,String output){
        try{
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
        }
    }
    private static String[] parseResponse(String res,JsonArray messages){


        JsonObject r = JsonParser.parseString(res).getAsJsonObject();
        String content = r.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
        PutMessages("assistant",content,messages);
        return PostProcess(content);
    }


    private static String[] PostProcess(String s){
        
        Pattern pattern =Pattern.compile("```command\\r?\\n([\\s\\S]+?)\\r?\\n```");
        Matcher matcher = pattern.matcher(s);
        if(matcher.find()){
            String command = matcher.group(1);



            String res = matcher.replaceAll("");
            return new String[] {res,command};
        }else{
            return null;
        }
    }

    private static JsonObject buildOpenAIRequest(String query,String md,String Pos,JsonArray messages,String output) {
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


        request.add("messages", messages);

        // 添加函数定义
        /*
        request.add("functions", buildFunctionDefinitions());
        request.addProperty("function_call", "auto");

         */

        return request;
    }

    private static void AddSys(JsonArray messages) {
        String SysP =Ai.config.Prompt;

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

    private static void PutMessages(String system, String SysP, JsonArray messages) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", system);
        systemMessage.addProperty("content", SysP);
        messages.add(systemMessage);
    }
    public void ClearContext(){
        Ai.LOGGER.info("CLEAR");
        this.messages = new JsonArray();
        AddSys(this.messages);
    }
}