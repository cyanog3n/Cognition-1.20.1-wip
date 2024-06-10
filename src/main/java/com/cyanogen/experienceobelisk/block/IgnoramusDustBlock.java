package com.cyanogen.experienceobelisk.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class IgnoramusDustBlock extends FallingBlock {

    public IgnoramusDustBlock() {
        super(Properties.copy(Blocks.SAND)
                .strength(0.3f)
        );
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter getter, BlockPos pos) {
        return -8356741;
    }
}