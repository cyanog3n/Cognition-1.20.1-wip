package com.cyanogen.experienceobelisk.block;

import com.cyanogen.experienceobelisk.gui.PrecisionDispellerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PrecisionDispellerBlock extends Block{

    public PrecisionDispellerBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
                .strength(9f)
                .destroyTime(1.2f)
                .requiresCorrectToolForDrops()
                .explosionResistance(1200f)
                .noOcclusion()
                .emissiveRendering((state, getter, pos) -> true)
        );
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }
    }


    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {

        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("Precision Dispeller");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                return new PrecisionDispellerMenu(pContainerId, pPlayerInventory, pPlayer, pos);
            }
        };

        //return new SimpleMenuProvider(PrecisionDispellerMenu::new, new TextComponent("Precision Dispeller"));
    }
}