package com.ai.entity.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public class ModConfigScreen {
    public static Screen createScreen(Screen parent) {
        // 创建配置构建器
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("Config Your AI Model"));

        // 获取或创建配置类别
        ConfigCategory general = builder.getOrCreateCategory(Text.of("category.yourmod.general"));

        // 添加配置选项
        /*
        general.addEntry(builder.entryBuilder()
                .startIntField(Text.of("option.yourmod.someInt"), ModConfig.someInt)
                .setDefaultValue(10)
                .setSaveConsumer(newValue -> ModConfig.someInt = newValue)
                .build());*/

        // 更多选项...

        return builder.build();
    }
}