package com.invisiblecat.reload.module.modules.combat;

import com.invisiblecat.reload.event.EventTarget;
import com.invisiblecat.reload.event.events.EventPreMotionUpdate;
import com.invisiblecat.reload.event.events.EventUpdate;
import com.invisiblecat.reload.module.Category;
import com.invisiblecat.reload.module.Module;
import com.invisiblecat.reload.setting.settings.BooleanSetting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Module {
    public List<EntityLivingBase> bots = new ArrayList<>();

    private BooleanSetting idCheck = new BooleanSetting("ID Check", true);
    private BooleanSetting pingCheck = new BooleanSetting("Ping check", true);
    private BooleanSetting nameCheck = new BooleanSetting("Name check", true);
    private BooleanSetting sameName = new BooleanSetting("Same name", true);
    private BooleanSetting ticksExist = new BooleanSetting("Ticks existed", true);
    private BooleanSetting tp = new BooleanSetting("Teleport", true);

    private int ticks = 0;

    public AntiBot() {
        super("AntiBot", 0, Category.COMBAT, AutoDisable.NONE);
        this.addSettings(idCheck, pingCheck, nameCheck, sameName, ticksExist, tp);
    }

    @EventTarget
    public void onPreMotionUpdate(EventPreMotionUpdate event) {

        List<String> names = new ArrayList<>();

        bots.clear();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) return;

            if (idCheck.isEnabled() && player.getEntityId() <= 0) {
                bots.add(player);
            }

            if (ticksExist.isEnabled() && player.ticksExisted <= 10) {
                bots.add(player);
            }

            if (pingCheck.isEnabled()) {
                NetworkPlayerInfo idk = mc.getNetHandler().getPlayerInfo(player.getUniqueID());

                if (idk != null && idk.getResponseTime() < 0)
                    bots.add(player);
            }
            if (nameCheck.isEnabled()) {
                final String valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";
                final String name = player.getName();

                for (int i = 0; i < name.length(); i++) {
                    final String c = String.valueOf(name.charAt(i));
                    if (!valid.contains(c)) {
                        bots.add(player);
                        break;
                    }
                }
            }
            if (sameName.isEnabled()) {
                final String name = player.getName();

                if (names.contains(name))
                    bots.add(player);

                names.add(name);
            }

            if (tp.isEnabled()) {
                BlockPos oldPos = new BlockPos(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
                if (ticks > 20) {
                    BlockPos newPos = new BlockPos(player.posX, player.posY, player.posZ);
                    if (oldPos.distanceSq(newPos) > 50) {
                        bots.add(player);
                        ticks = 0;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate (EventUpdate event) {
        ticks++;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        bots.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        bots.clear();
    }
}
