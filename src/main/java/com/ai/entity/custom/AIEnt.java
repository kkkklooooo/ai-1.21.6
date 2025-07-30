package com.ai.entity.custom;

import com.ai.Ai;
import com.ai.entity.client.LLMAPI;
import com.microsoft.aad.msal4j.Prompt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public class AIEnt extends HostileEntity {


    public static DefaultAttributeContainer createAttributes(){
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH,33550336.0)
                .add(EntityAttributes.MOVEMENT_SPEED,1f)
                .add(EntityAttributes.FLYING_SPEED,1f)
                .add(EntityAttributes.ATTACK_DAMAGE,0.5f)
                .add(EntityAttributes.FOLLOW_RANGE,10)
                .build();
    }

    public AIEnt(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }


    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if(getWorld().isClient()){
            /*
            CompletableFuture.supplyAsync(() -> L.Call("hello"))
                    .thenAcceptAsync(response -> {
                        player.sendMessage(Text.literal("[AI] " + response),false);
                    },MinecraftClient.getInstance());
            */


            //String rrr=L.Call("hello");
            //Ai.LOGGER.info(rrr);
            //player.sendMessage(Text.literal("<%s>:%s".formatted(this.getName(),rrr)),false);
            MinecraftClient.getInstance().setScreen(new ChatScreen("aieask "));

        }
        return ActionResult.SUCCESS;
    }
}
