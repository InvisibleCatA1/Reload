package com.invisiblecat.reload.client.ui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class Element {
    private int x, y, width, height;
    private boolean toggled;
    private String name;
    protected Minecraft mc = Minecraft.getMinecraft();
    protected ScaledResolution sr = new ScaledResolution(mc);
    public Draggable draggable;


    public Element(String name, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;

        draggable = new Draggable(x, y, x + width, y + height, new Color(0,0,0,0).getRGB());

        toggled = true;
    }

    public void render() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void onEnable() {}
    public void onToggle() {}
    public void onDisable() {}
    public void toggle() {
        toggled = !toggled;
        onToggle();
        if(toggled)
            onEnable();
        else
            onDisable();
    }
    public boolean isToggled() {
        return toggled;
    }
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
    public int getX() {
        return draggable.getxPosition();
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return draggable.getyPosition();
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
}
