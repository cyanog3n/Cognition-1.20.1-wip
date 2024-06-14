package com.cyanogen.experienceobelisk.block_entities.bibliophage;

import com.cyanogen.experienceobelisk.registries.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedCartographersBookshelfEntity extends AbstractInfectedBookshelfEntity {

    public InfectedCartographersBookshelfEntity(BlockPos pos, BlockState state) {
        super(RegisterBlockEntities.INFECTED_CARTOGRAPHERS_BOOKSHELF_BE.get(), pos, state);

        super.orbValue = 0;
        super.durability = 1400;

    }

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {

        if(level.getGameTime() % 20 == 0 && blockEntity instanceof AbstractInfectedBookshelfEntity bookshelf){

            bookshelf.incrementDecayValue(level, pos);

            if(Math.random() <= 0.15){
                bookshelf.infectAdjacent(level, pos);
            }

        }

    }

}