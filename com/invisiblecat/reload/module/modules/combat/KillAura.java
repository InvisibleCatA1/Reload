package com.invisiblecat.reload.module.modules.combat;

import com.invisiblecat.reload.client.Reload;
import com.invisiblecat.reload.client.ui.hud.HUD;
import com.invisiblecat.reload.event.EventTarget;
import com.invisiblecat.reload.event.events.EventPostMotionUpdate;
import com.invisiblecat.reload.event.events.EventPreMotionUpdate;
import com.invisiblecat.reload.event.events.EventUpdate;
import com.invisiblecat.reload.module.Category;
import com.invisiblecat.reload.module.Module;
import com.invisiblecat.reload.setting.settings.BooleanSetting;
import com.invisiblecat.reload.setting.settings.ModeSetting;
import com.invisiblecat.reload.setting.settings.NumberSetting;
import com.invisiblecat.reload.utils.PacketUtils;
import com.invisiblecat.reload.utils.TimerUtils;
import com.invisiblecat.reload.utils.player.AuraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class KillAura extends Module {
    private final TimerUtils timer = new TimerUtils();
    private final Random random = new Random();

    private NumberSetting range = new NumberSetting("Range", 3, 1, 8, 0.1);
    private NumberSetting minCps = new NumberSetting("Minimum CPS", 10, 1, 20, 1);
    private NumberSetting maxCps = new NumberSetting("Maximum CPS", 15, 1, 20, 1);

    private ModeSetting rotMode = new ModeSetting("Rotation Mode", "Normal", "Normal", "Down");

    private ModeSetting sort = new ModeSetting("Sort", "Distance", "Health", "Distance", "Hurt Time");

    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting others = new BooleanSetting("Outers", true);
    private BooleanSetting invsibles = new BooleanSetting("Invisibles", false);

    private BooleanSetting legit = new BooleanSetting("Legit", false);
    private BooleanSetting swing = new BooleanSetting("Swing", true);
    private BooleanSetting block = new BooleanSetting("Block", true);
    private BooleanSetting targetESP = new BooleanSetting("Target ESP", true);

    private List<EntityLivingBase> entities;
    private EntityLivingBase target;
    private float yaw, pitch,
            lastYaw, lastPitch;


    public KillAura() {
        super("KillAura", 0, Category.PLAYER, AutoDisable.WORLD);
        this.addSettings(range, sort, rotMode, players, others, legit, minCps, maxCps, swing, block, invsibles, targetESP);
    }

    @EventTarget
    public void onPreMotionUpdate(EventPreMotionUpdate event) {
        entities = getTargets();
        if (entities.size() > 0) {
            target = entities.get(0);
            yaw = getRotations(target)[0];
            pitch = getRotations(target)[1];

            event.setYaw(yaw);
            event.setPitch(pitch);

            mc.thePlayer.rotationYawHead = yaw;
            mc.thePlayer.renderYawOffset = yaw;
            mc.thePlayer.rotationPitchHead = pitch;

            attack();
        }
    }

    @EventTarget
    public void onPostMotionUpdate(EventPostMotionUpdate event) {
        if (block.isEnabled() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            mc.gameSettings.keyBindUseItem.setState(true);
        }
        if (target == null || !target.isEntityAlive() || (mc.thePlayer.getDistanceToEntity(target) > range.getValue()) && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            mc.gameSettings.keyBindUseItem.setState(false);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.setDisplayName(rotMode.getSelected());
        if (maxCps.getValue() < minCps.getValue()) maxCps.setValue(minCps.getValueInt());
        entities = getTargets();
        assert entities != null;
        target = entities.get(0);

        //attack();
    }

    private List<EntityLivingBase> getTargets() {
        AntiBot antiBot = (AntiBot) Reload.instance.moduleManager.getModuleByClass(AntiBot.class);
        List<EntityLivingBase> var2 = mc.theWorld.loadedEntityList
                .stream().filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> ((EntityLivingBase) entity))
                .filter(entity -> {
                    if (entity instanceof EntityPlayer && !players.isEnabled()) return false;

                    if (!(entity instanceof EntityPlayer) && !others.isEnabled()) return false;

                    if (!entity.isEntityAlive()) return false;

                    if (antiBot.bots.contains(entity)) return false;

                    if (entity.ticksExisted < 2) return false;

                    if (entity.isInvisible() && !invsibles.isEnabled()) return false;

                    if (mc.thePlayer.getDistanceToEntity(entity) > range.getValue()) return false;

                    return mc.thePlayer != entity;
                })
                .sorted(Comparator.comparingDouble(entity -> {
                    switch (sort.getSelected()) {
                        case "Distance":
                            return mc.thePlayer.getDistanceSqToEntity(entity);
                        case "Health":
                            return entity.getHealth();
                        case "Hurt Time":
                            return entity.hurtTime;

                        default:
                            return -1;
                    }
                })).collect(Collectors.toList());
        if (var2.isEmpty() && timer.hasTimePassed(1000, true)) return null;
        return var2;
    }

//    private void block() {
//        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem());
//        mc.gameSettings.keyBindUseItem.setState(true);
//        mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));
//        blocking = true;
//    }
//
//    private void unblock() {
//        if (blocking) {
//            mc.gameSettings.keyBindUseItem.setState(false);
//            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//            blocking = false;
//        }
//    }

    private float[] getRotations(EntityLivingBase entity) {
        lastYaw = yaw;
        lastPitch = pitch;
        double deltaX = entity.posX + (entity.posX - entity.lastTickPosX) - mc.thePlayer.posX;
        double deltaZ = entity.posZ + (entity.posZ - entity.lastTickPosZ) - mc.thePlayer.posZ;
        double deltaY = entity.posY - 3.5 + entity.getEyeHeight() - mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double dist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));
        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        float pitch = (float) Math.toDegrees(-Math.atan(deltaY / dist));

        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + v);
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + v);
        }

        switch (rotMode.getSelected()) {
            case "Normal":
                break;
            case "Down":
                pitch = RandomUtils.nextFloat(89, 90);
                break;
        }
        return new float[]{yaw, pitch};
    }

    private void attack() {
        long cps = random.nextInt(maxCps.getValueInt() - minCps.getValueInt() + 1) + minCps.getValueInt();

        if (timer.hasTimePassed(1000 / cps, true)) {
            if (!target.isEntityAlive()) return;
            AuraUtils.attack(target, legit.isEnabled());
            if (swing.isEnabled()) mc.thePlayer.swingItem();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
    }

    public EntityLivingBase getTarget() {
        return target;
    }
}
