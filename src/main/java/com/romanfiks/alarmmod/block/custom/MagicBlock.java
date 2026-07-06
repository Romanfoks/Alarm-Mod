package com.romanfiks.alarmmod.block.custom;

import com.romanfiks.alarmmod.block.ModBlocks;
import com.romanfiks.alarmmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MagicBlock extends Block {
    public MagicBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        level.playSound(null, pos, SoundEvents.CAT_PURR, SoundSource.BLOCKS, 1f, 1f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            if (stack.is(ModItems.REDSTONE_CHARGED_IRON.get())) {
                itemEntity.setItem(new ItemStack(ModBlocks.REDSTONE_CHARGED_IRON_BLOCK.get(), stack.getCount()));
            }
            if (stack.is(ModBlocks.REDSTONE_CHARGED_IRON_BLOCK.get().asItem())) {
                itemEntity.setItem(new ItemStack(ModItems.REDSTONE_CHARGED_IRON.get(), stack.getCount()));
            }
        }

        super.stepOn(level, pos, state, entity);
    }
}
