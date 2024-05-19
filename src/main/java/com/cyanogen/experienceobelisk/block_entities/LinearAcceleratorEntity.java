package com.cyanogen.experienceobelisk.block_entities;

import com.cyanogen.experienceobelisk.block.LinearAcceleratorBlock;
import com.cyanogen.experienceobelisk.registries.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LinearAcceleratorEntity extends BlockEntity{

    public LinearAcceleratorEntity(BlockPos pos, BlockState state) {
        super(RegisterBlockEntities.LINEARACCELERATOR_BE.get(), pos, state);
    }

    static double orbSpeed = 2.0;
    static double entitySpeed = 0.5;

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {

        if(blockEntity instanceof LinearAcceleratorEntity accelerator &&
                (!accelerator.redstoneEnabled || level.hasNeighborSignal(pos))){

            Direction facing = state.getValue(LinearAcceleratorBlock.FACING);
            int x = 0;
            int z = 0;

            switch (facing){
                case NORTH -> z = -1;
                case SOUTH -> z = 1;
                case EAST -> x = 1;
                case WEST -> x = -1;
            }

            AABB area = new AABB(
                    pos.getX(),
                    pos.getY() + 1,
                    pos.getZ(),
                    pos.getX() + 1,
                    pos.getY() + 2,
                    pos.getZ() + 1);

            List<Entity> list = level.getEntities(null, area);

            if(!list.isEmpty()) for(Entity entity : list){
                boolean isShiftPlayer = entity instanceof Player player && player.isShiftKeyDown();

                if(entity instanceof ExperienceOrb orb){
                    orb.addDeltaMovement(new Vec3(orbSpeed * x,0,orbSpeed * z));
                }
                else if(!isShiftPlayer){
                    entity.addDeltaMovement(new Vec3(entitySpeed * x,0,entitySpeed * z));
                }

            }
        }

    }

    //-----------NBT-----------//

    public boolean redstoneEnabled = false;

    public void toggleRedstoneEnabled(){
        this.redstoneEnabled = !redstoneEnabled;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);

        this.redstoneEnabled = tag.getBoolean("isRedstoneControllable");
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);

        tag.putBoolean("isRedstoneControllable", redstoneEnabled);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();

        tag.putBoolean("isRedstoneControllable", redstoneEnabled);

        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
