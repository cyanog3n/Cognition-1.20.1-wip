package com.cyanogen.experienceobelisk.event;

import com.cyanogen.experienceobelisk.item.MendingNeurogelItem;
import com.cyanogen.experienceobelisk.item.ResurrectingNeurogelItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTooltip(ItemTooltipEvent event){
        DescriptionTooltips.handleTooltip(event);
        ResurrectingNeurogelItem.handleTooltip(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRegisterItemDecorator(RegisterItemDecorationsEvent event){
    }

    @SubscribeEvent
    public void onItemStackedOnOther(ItemStackedOnOtherEvent event){
        MendingNeurogelItem.handleItem(event);
        ResurrectingNeurogelItem.handleItem(event);
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event){
        ResurrectingNeurogelItem.handleDestruction(event);
    }


}
