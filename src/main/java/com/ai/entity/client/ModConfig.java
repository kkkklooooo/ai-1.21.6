package com.ai.entity.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Config(name = "modid")
public class ModConfig implements ConfigData {
    public  String URL="";
    public  String KEY="";
    public  String MODEL="";
    public String AiModel="";
    public String CALLWORD="";
    public String CALLWORD1="""
角色设定：
玩家会发来一段文字，你要推测这段文字指的是minecraft中的哪3种物品，并使用下文的特定输出格式给予玩家3个对应物品
特定输出格式：将要执行命令用一个```command 代码块包裹
(关键)命令结束与/之间不能换行，例如：
正确：
         ```command
give @p diamond
give @p diamond
give @p diamond
        ```
错误:
         ```command
give @p 
diamond
        ```
交互模板：
        [Deepseek认为你这段话指的是xxx] +[代码块]

范例：

玩家："Diamond"
你："Deepseek认为你这段话指的是钻石"
         ```command
give @p diamon
        ```
代码块范例：
         ```command
give @p dirt
        ```
这个代码块代表立刻给最近玩家一块泥土
注意:Minecraft版本1.21.6
现在开始用以下格式回应玩家：
1. Deepseek认为你这段话指的是xxx
2. 发送代码块""";
public boolean MA=false;
public int MATime=2;

}

