package com.cyanogen.experienceobelisk.gui;

import com.cyanogen.experienceobelisk.block_entities.ExperienceObeliskEntity;
import com.cyanogen.experienceobelisk.block_entities.LaserTransfiguratorEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LaserTransfiguratorScreen extends AbstractContainerScreen<LaserTransfiguratorMenu>{

    private final ResourceLocation texture = new ResourceLocation("experienceobelisk:textures/gui/screens/laser_transfigurator.png");
    public LaserTransfiguratorEntity transfigurator;
    public ExperienceObeliskEntity obelisk;
    private final Component title = Component.translatable("title.experienceobelisk.laser_transfigurator");
    private final Component inventoryTitle = Component.translatable("title.experienceobelisk.precision_dispeller.inventory");

    public LaserTransfiguratorScreen(LaserTransfiguratorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.transfigurator = menu.transfigurator;
        this.obelisk = menu.obeliskClient;
    }

    @Override
    protected void renderBg(GuiGraphics p_283065_, float p_97788_, int p_97789_, int p_97790_) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF);
        gui.drawString(this.font, this.inventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {

        renderBackground(gui);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int arrowWidth = 26;
        int completion = 0;
        if(transfigurator.getProcessTime() != 0){
            completion = transfigurator.getProcessProgress() / transfigurator.getProcessTime();
        }

        int xpBarWidth = 61;
        int levels = 0;
        double progress = 0;
        if(obelisk != null){
            levels = obelisk.getLevels();
            progress = obelisk.getProgressToNextLevel();
        }

        //render background texture
        gui.blit(texture, x, y, 0, 0, 176, 166);

        //render recipe progress
        gui.blit(texture, this.width / 2 + 109 - 88, this.height / 2 + 48 - 83, 0, 175, arrowWidth * completion, 5);

        //render xp bar
        gui.blit(texture, this.width / 2 + 107 - 88, this.height / 2 + 71 - 83, 0, 166, (int) (xpBarWidth * progress), 9);

        gui.drawCenteredString(this.font, Component.literal(String.valueOf(levels)).withStyle(ChatFormatting.GREEN),
                this.width / 2 + 52,this.height / 2 - 11, 0xFFFFFF);

        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
