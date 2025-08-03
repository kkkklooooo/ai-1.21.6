package com.ai.entity.client;

import com.ai.Ai;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.ai.Ai.config;

public class DataReader {
    // 读取全局数据
    public static void readGlobalData(String fileName) {
        Path filePath = FileStorage.getModStorageDir().resolve(fileName+".txt");

        /*if (!Files.exists(filePath)) {
            return Collections.emptyList();
        }*/

        try {
            config = (ModConfig) Aiclient.ch.getConfig();
            config.CALLWORD="";
            for(String s :Files.readAllLines(filePath))
            {
                config.CALLWORD+=s;
            }
        } catch (IOException e) {
            // 处理读取错误
            Ai.LOGGER.info(String.valueOf(e));
            config.CALLWORD=config.CALLWORD1;

        }
    }
}
