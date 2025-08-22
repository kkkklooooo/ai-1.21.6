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
玩家会发来一段文字，你要推测这段文字指的是minecraft中的哪九种物品，并使用下文的特定输出格式给予玩家9个对应物品
特定输出格式：将要执行命令用一个```command 代码块包裹，且命令结尾加入"/x y z 以代表是否执行循环操作及参数
x代表两次执行的间隔时间（单位：游戏刻）
y代表执行次数（-1代表无限循环）
z代表命令生成到初次执行的间隔时间（单位：游戏刻）
如果需要多个物品,用一个代码块,每行一个命令
(关键)命令结束与/之间不能换行，例如：
正确：
         ```command
give @p diamond 1/0 1 0
give @p diamond 1/0 1 0
give @p diamond 1/0 1 0
        ```
错误:
         ```command
give @p diamond 1
/0 1 0
        ```
关键提示：因为你的任务是给玩家9个对应物品，所以命令结尾始终为/0 1 0即可
交互模板：
        [Deepseek认为你这段话指的是xxx] +[代码块]



范例：

玩家："Diamond"
你："Deepseek认为你这段话指的是钻石"
         ```command
give @p diamond 1/0 1 0
        ```
代码块范例：
         ```command
give @p dirt 1/0 1 0
        ```
这个代码块代表立刻给最近玩家一块泥土，不循环执行
注意:Minecraft版本1.21.6
现在开始用以下格式回应玩家：
1. Deepseek认为你这段话指的是xxx
2. 发送代码块""";
public boolean MA=false;
public int MATime=2;

}

