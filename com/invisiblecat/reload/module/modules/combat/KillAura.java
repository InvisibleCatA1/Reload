package com.invisiblecat.reload.module.modules.combat;


import com.invisiblecat.reload.event.EventTarget;
import com.invisiblecat.reload.event.events.EventPreMotionUpdate;
import com.invisiblecat.reload.event.events.EventUpdate;
import com.invisiblecat.reload.module.Category;
import com.invisiblecat.reload.module.Module;
import com.invisiblecat.reload.setting.settings.BooleanSetting;
import com.invisiblecat.reload.setting.settings.NumberSetting;
import com.invisiblecat.reload.utils.player.AuraUtils;
import com.invisiblecat.reload.utils.player.RotationUtils;
import net.minecraft.entity.EntityLivingBase;

public class KillAura extends Module {

    private NumberSetting range = new NumberSetting("Range", 3, 1, 6, 1);
    private NumberSetting minCps = new NumberSetting("Min CPS", 10, 1, 20, 1);
    private NumberSetting maxCps = new NumberSetting("Max CPS", 10, 1, 20, 1);

    private BooleanSetting autoBlock = new BooleanSetting("Auto Block", false);
    private BooleanSetting legitAttack = new BooleanSetting("Legit Attack", false);
    private BooleanSetting swing = new BooleanSetting("Swing", false);

    private EntityLivingBase target;


    public KillAura() {
        super("KillAura", 0, Category.COMBAT, AutoDisable.WORLD);
        this.addSettings(range, autoBlock, legitAttack, swing, minCps, maxCps);
    }

    // Find a target
    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (this.target == null) {
            this.target = AuraUtils.getTarget(this.mc.thePlayer, (int) this.range.getValue());
        }
    }


}
