package com.cyanogen.experienceobelisk.gui;

import com.cyanogen.experienceobelisk.block_entities.ExperienceObeliskEntity;
import com.cyanogen.experienceobelisk.network.PacketHandler;
import com.cyanogen.experienceobelisk.network.experience_obelisk.UpdateContents;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

import static com.cyanogen.experienceobelisk.network.experience_obelisk.UpdateContents.Request.*;
import static com.cyanogen.experienceobelisk.utils.ExperienceUtils.levelsToXP;
import static com.cyanogen.experienceobelisk.utils.ExperienceUtils.xpToLevels;

public class ExperienceObeliskScreen extends AbstractContainerScreen<ExperienceObeliskMenu> {

    public BlockPos pos;
    public ExperienceObeliskEntity xpobelisk;
    private final ResourceLocation texture = new ResourceLocation("experienceobelisk:textures/gui/screens/experience_obelisk.png");

    public ExperienceObeliskScreen(ExperienceObeliskMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.pos = menu.pos;
        this.xpobelisk = menu.entity;
    }

    protected ExperienceObeliskScreen(ExperienceObeliskMenu menu) {
        this(menu, menu.inventory, Component.literal("Experience Obelisk"));
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        //check if still necessary - i suspect not
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        assert this.minecraft != null;
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        else{
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    @Override
    protected void init() {
        setupWidgetElements();
        super.init();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {

        renderBackground(gui);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, texture);

        int textureWidth = 256;
        int textureHeight = 256;
        int x = this.width / 2 - 176 / 2;
        int y = this.height / 2 - 166 / 2;

        //breaks around 2980000 mB for some reason

        int experiencePoints = xpobelisk.getFluidAmount() / 20;

        int n = experiencePoints - levelsToXP(xpToLevels(experiencePoints)); //remaining xp
        int m = levelsToXP(xpToLevels(experiencePoints) + 1) - levelsToXP(xpToLevels(experiencePoints)); //xp for next level
        int p = n * 138 / m;

        //render gui texture
        gui.blit(texture, x, y, 0, 0, 176, 166);

        //render xp bar
        gui.blit(texture, this.width / 2 - 138 / 2, this.height / 2 + 50, 0, 169, 138, 5);
        gui.blit(texture, this.width / 2 - 138 / 2, this.height / 2 + 50, 0, 173, p, 5);

        //descriptors & info
        gui.drawCenteredString(this.font, Component.translatable("title.experienceobelisk.experience_obelisk"),
                this.width / 2,this.height / 2 - 76, 0xFFFFFF);
        gui.drawString(this.font, Component.translatable("title.experienceobelisk.experience_obelisk.store"),
                this.width / 2 - 77,this.height / 2 - 56, 0xFFFFFF);
        gui.drawString(this.font, Component.translatable("title.experienceobelisk.experience_obelisk.retrieve"),
                this.width / 2 - 77,this.height / 2 - 10, 0xFFFFFF);
        gui.drawCenteredString(this.font, experiencePoints * 20 + " mB",
                this.width / 2,this.height / 2 + 35, 0xFFFFFF);
        gui.drawCenteredString(this.font, String.valueOf(xpToLevels(experiencePoints)),
                this.width / 2,this.height / 2 + 60, 0x4DFF12);

        clearWidgets();
        setupWidgetElements();

        for(Renderable widget : this.renderables){
            widget.render(gui, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float f, int a, int b) {

    }

    //buttons and whatnot go here
    private void setupWidgetElements() {

        int w = 50; //width (divisible by 2)
        int h = 20; //height
        int s = 2; //spacing
        int y1 = 43;
        int y2 = -3;

        //settings

        Button settings = Button.builder(Component.translatable("button.experienceobelisk.experience_obelisk.settings"),
                        (onPress) -> Minecraft.getInstance().setScreen(new ExperienceObeliskOptionsScreen(pos, menu)))
                .size(20,20)
                .pos(this.width / 2 + 91, this.height / 2 - 78)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.settings")))
                .build();

        //deposit

        Button deposit1 = Button.builder(Component.literal("+1").withStyle(ChatFormatting.GREEN),
                        (onPress) -> PacketHandler.INSTANCE.sendToServer(new UpdateContents(pos, 1, FILL)))
                .size(w,h)
                .pos((int) (this.width / 2 - 1.5*w - s), this.height / 2 - y1)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.add1")))
                .build();

        Button deposit10 = Button.builder(Component.literal("+10").withStyle(ChatFormatting.GREEN),
                        (onPress) -> PacketHandler.INSTANCE.sendToServer(new UpdateContents(pos, 10, FILL)))
                .size(w,h)
                .pos(this.width / 2 - w/2, this.height / 2 - y1)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.add10")))
                .build();

        Button depositAll = Button.builder(Component.literal("+All").withStyle(ChatFormatting.GREEN),
                        (onPress) -> PacketHandler.INSTANCE.sendToServer(new UpdateContents(pos, 0, FILL_ALL)))
                .size(w,h)
                .pos((int) (this.width / 2 + 0.5*w + s), this.height / 2 - y1)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.addAll")))
                .build();

        //withdraw

        Button withdraw1 = Button.builder(Component.literal("-1").withStyle(ChatFormatting.RED),
                        (onPress) -> PacketHandler.INSTANCE.sendToServer(new UpdateContents(pos, 1, DRAIN)))
                .size(w,h)
                .pos((int) (this.width / 2 - 1.5*w - s), this.height / 2 - y2)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.drain1")))
                .build();

        Button withdraw10 = Button.builder(Component.literal("-10").withStyle(ChatFormatting.RED),
                        (onPress) -> PacketHandler.INSTANCE.sendToServer(new UpdateContents(pos, 10, DRAIN)))
                .size(w,h)
                .pos(this.width / 2 - w/2, this.height / 2 - y2)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.drain10")))
                .build();

        Button withdrawAll = Button.builder(Component.literal("-All").withStyle(ChatFormatting.RED),
                        (onPress) -> PacketHandler.INSTANCE.sendToServer(new UpdateContents(pos, 0, DRAIN_ALL)))
                .size(w,h)
                .pos((int) (this.width / 2 + 0.5*w + s), this.height / 2 - y2)
                .tooltip(Tooltip.create(Component.translatable("tooltip.experienceobelisk.experience_obelisk.drainAll")))
                .build();

        settings.setFocused(false);
        deposit1.setFocused(false);
        deposit10.setFocused(false);
        depositAll.setFocused(false);
        withdraw1.setFocused(false);
        withdraw10.setFocused(false);
        withdrawAll.setFocused(false);

        addRenderableWidget(settings);
        addRenderableWidget(deposit1);
        addRenderableWidget(deposit10);
        addRenderableWidget(depositAll);
        addRenderableWidget(withdraw1);
        addRenderableWidget(withdraw10);
        addRenderableWidget(withdrawAll);
    }

}
