package com.ai.entity.client;

import org.spongepowered.include.com.google.common.io.Files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class DataWriter {
    // 写入全局数据
    public static void writeGlobalData(String modelName, String model) {
        Path filePath = FileStorage.getModStorageDir().resolve(modelName);
        try {
            Files.write(model.getBytes(), filePath.toFile());
        } catch (IOException e) {
            // 处理写入错误
        }
    }
}

