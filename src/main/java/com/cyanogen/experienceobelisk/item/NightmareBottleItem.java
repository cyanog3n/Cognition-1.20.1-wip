package com.cyanogen.experienceobelisk.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class NightmareBottleItem extends BottleItem {

    public NightmareBottleItem(Properties p) {
        super(p);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {

        if(entity instanceof Player player){
            player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            player.awardStat(Stats.TIME_SINCE_REST, 20000000);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1)); //seconds probably?

            if(!player.isCreative()){
                stack.shrink(1);
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }

    public int getUseDuration(ItemStack stack) {
        return 30;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
