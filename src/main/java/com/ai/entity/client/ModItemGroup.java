package com.ai.entity.client;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup ai = FabricItemGroup.builder().icon(()->(new ItemStack(ModItem.Google))).
            displayName(Text.translatable(I18n.translate("Ai"))).
            entries((context,entries)->{

                entries.add(ModItem.Google);
            }).build();
    public static void initialize()
    {
        Registry.register(Registries.ITEM_GROUP, Identifier.of("com.ai","ai"),ai);
    }
}
