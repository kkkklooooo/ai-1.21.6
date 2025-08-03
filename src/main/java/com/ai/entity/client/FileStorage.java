package com.ai.entity.client;

import com.ai.Ai;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Path;

public class FileStorage {
    // 获取模组专用存储目录（全局数据）
    public static Path getModStorageDir() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path modDir = gameDir.resolve("AIEModels");

        // 确保目录存在
        if (!java.nio.file.Files.exists(modDir)) {
            try {
                java.nio.file.Files.createDirectories(modDir);
            } catch (IOException e) {
                // 处理异常
                Ai.LOGGER.info(String.valueOf(e));
            }
        }
        return modDir;
    }
}
