package com.ai;

import com.ai.entity.client.LLMAPI;
import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class Ai implements ModInitializer {
	public static final String MOD_ID = "ai";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	public static CompletableFuture<Void> exe(ServerPlayerEntity sender, LLMAPI Client, String message){
		return CompletableFuture.supplyAsync(() -> Client.Call(sender.getPos().toString(),message.replace("ask ","").replace("CLEARCTX","")))
				.thenCompose((response) -> {
					Ai.LOGGER.info(response[1]);
					sender.sendMessage(Text.literal("[God]: " + response[0]),false);

					CommandDispatcher<ServerCommandSource> dispatcher =
							sender.getServer().getCommandManager().getDispatcher();


					String[] cmds = response[1].split("\\R");
					for (String cmd : cmds) {
						try{
							dispatcher.execute(cmd,sender.getCommandSource());
						}catch (CommandSyntaxException e){
							return exe(sender,Client,e.getMessage());
						}
					}
					return CompletableFuture.completedFuture(null);

					// 解析并执行命令
							/*
							processStringByLines(response[1], item -> {
								try {
									dispatcher.execute(item, sender.getCommandSource());
								} catch (CommandSyntaxException e) {
									// 如果命令执行过程中出现错误，捕获并打印异常

									e.printStackTrace();
								}
							});*/


				});

	}

	@Override
	public void onInitialize() {
		entities.register();
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		FabricDefaultAttributeRegistry.register(
				entities.aie,AIEnt.createAttributes()
		);

		LLMAPI Client = new LLMAPI("https://openrouter.ai/api/v1/chat/completions","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","deepseek/deepseek-r1-0528:free");







		ServerMessageEvents.CHAT_MESSAGE.register(((message, sender, params) -> {
			if(message.getContent().getString().startsWith("ask")){
				if(message.getContent().getString().contains("CLEARCTX")){
					Client.ClearContext();
				}
				exe(sender,Client,message.getContent().getString());







            }
		}));
	}


	@FunctionalInterface
	public interface LineProcessorCallback {
		/**
		 * 处理分割后的每一行字符串。
		 * @param item 分割后的单个字符串（一行）。
		 */
		void processLine(String item);
	}

	public static void processStringByLines(String inputString, LineProcessorCallback callback) {
		if (inputString == null || inputString.isEmpty()) {
			return;
		}

		// 使用 "\R" 正则表达式分割字符串，它可以处理各种换行符（\n, \r\n, \r）
		// 如果你需要过滤掉空行，可以在分割后进行额外的过滤操作。
		String[] lines = inputString.split("\\R"); // [1, 2, 4, 13]

		for (String line : lines) {
			// 对每一行执行回调
			callback.processLine(line);
		}
	}
}