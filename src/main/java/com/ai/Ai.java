package com.ai;

import com.ai.entity.client.LLMAPI;
import com.ai.entity.client.ModConfig;

import com.ai.entity.custom.AIEnt;
import com.ai.entity.entities;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.tick.Tick;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.ai.entity.client.Aiclient.inp;

public class Ai implements ModInitializer {
	public static final String MOD_ID = "ai";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static int num=0;
	public static ModConfig config;
	private static List<String> tasks=new ArrayList<String>();
	private static List<ServerPlayerEntity> players= new ArrayList<ServerPlayerEntity>();
	private static List<Integer> delays=new ArrayList<Integer>();
	private static List<Integer> maxdelays=new ArrayList<Integer>();
	private static List<Integer> times=new ArrayList<Integer>();
	public static CompletableFuture<Void> exe(ServerPlayerEntity sender, LLMAPI Client, String message,String output){
		if(config.MA)
		{
			return CompletableFuture.supplyAsync(() -> Client.Call(sender.getPos().toString(),message.replace("aieask ","").replace("CLEARCTX",""),output,sender))
					.thenCompose((response) -> {
						if(response[1]!=null){

							Ai.LOGGER.info(response[1]);
							sender.sendMessage(Text.literal("[AIE]: " + response[0]),false);

							CommandDispatcher<ServerCommandSource> dispatcher =
									sender.getServer().getCommandManager().getDispatcher();


							String[] cmds = response[1].split("\\R");
							for (String cmd : cmds) {
								String[] command=cmd.split("/");
								String[] flags=command[1].split(" ");
								if(Boolean.parseBoolean(flags[0]))
								{
									tasks.add(command[0].trim());
									players.add(sender);
									delays.add(Integer.parseInt(flags[1]));
									maxdelays.add(Integer.parseInt(flags[1]));
									times.add(Integer.parseInt(flags[2])-1);
								}
								try{
									dispatcher.execute(command[0],sender.getCommandSource());
								}catch (CommandSyntaxException e){
									if(num>=config.MATime){
										num=0;
										return CompletableFuture.completedFuture(null);
									}
									Ai.LOGGER.error(e.getMessage());
									num++;
									return exe(sender,Client,e.getMessage(),e.getMessage());
								}
							}
							num = 0;
							return CompletableFuture.completedFuture(null);

							// 解析并执行命令

						}else {
							sender.sendMessage(Text.literal("[AIE]: " + response[0]),false);
							return CompletableFuture.completedFuture(null);
						}



					});
		}
		return CompletableFuture.supplyAsync(() -> Client.Call(sender.getPos().toString(),message.replace("aieask ","").replace("CLEARCTX",""),"",sender))
				.thenCompose((response) -> {
					if(response!=null){

						Ai.LOGGER.info(response[1]);
						sender.sendMessage(Text.literal("[AIE]: " + response[0]),false);

						CommandDispatcher<ServerCommandSource> dispatcher =
								sender.getServer().getCommandManager().getDispatcher();


						String[] cmds = response[1].split("\\R");
						for (String cmd : cmds) {
							String[] command=cmd.split("/");
							String[] flags=command[1].split(" ");
							if(Boolean.parseBoolean(flags[0]))
							{
								tasks.add(command[0].trim());
								players.add(sender);
								delays.add(Integer.parseInt(flags[1]));
								maxdelays.add(Integer.parseInt(flags[1]));
								times.add(Integer.parseInt(flags[2]));
								return CompletableFuture.completedFuture(null);
							}
							try{
								dispatcher.execute(command[0],sender.getCommandSource());
							}catch (CommandSyntaxException e){
								Ai.LOGGER.error(e.getMessage());
								//return exe(sender,Client,e.getMessage());

								return CompletableFuture.completedFuture(null);
							}
						}
						return CompletableFuture.completedFuture(null);

						// 解析并执行命令

					}else {
						return CompletableFuture.completedFuture(null);
					}



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
		ServerTickEvents.START_SERVER_TICK.register(this::OnServerTick);



	}
	public void OnServerTick(MinecraftServer server)
	{
		for(int i=0;i<tasks.size();i++)
		{
			int a =delays.get(i);
			if(a<=1)
			{
				CommandDispatcher<ServerCommandSource> dispatcher =
						players.get(i).getServer().getCommandManager().getDispatcher();
				try{
					dispatcher.execute(tasks.get(i),players.get(i).getCommandSource());
				}catch (CommandSyntaxException e){
					Ai.LOGGER.error(e.getMessage());
				}


				int b = times.get(i);
				if(b<=1)
				{
					tasks.remove(i);
					players.remove(i);
					delays.remove(i);
					maxdelays.remove(i);
					times.remove(i);
					return;
				}
				times.set(i,b-1);
				delays.set(i,maxdelays.get(i));
				return;
			}
			delays.set(i,a-1);
		}
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