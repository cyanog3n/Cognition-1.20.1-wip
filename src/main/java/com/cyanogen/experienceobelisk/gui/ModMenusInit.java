package com.cyanogen.experienceobelisk.gui;

import com.cyanogen.experienceobelisk.ExperienceObelisk;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenusInit {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, ExperienceObelisk.MOD_ID);

    public static final RegistryObject<MenuType<PrecisionDispellerMenu>> PRECISION_DISPELLER_MENU = MENUS.register("precision_dispeller_menu",
            ()-> IForgeMenuType.create(PrecisionDispellerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
