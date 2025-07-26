package com.ai.entity;

import com.ai.entity.custom.AIEnt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.ai.Ai.MOD_ID;

public class entities {
    public static final RegistryKey<EntityType<?>> registryKey = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "aie"));
    public static final EntityType<AIEnt> aie = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "aie"),
            EntityType.Builder.create(AIEnt::new, SpawnGroup.CREATURE).dimensions(1f, 2f).build(registryKey));

    public static void register() {
        /*
        Registry.register(Registries.ENTITY_TYPE,
                Identifier.of(MOD_ID, "aie"),
                aie
        );*/
    }
}
