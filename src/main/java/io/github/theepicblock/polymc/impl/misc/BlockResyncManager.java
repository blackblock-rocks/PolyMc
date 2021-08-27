/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.block.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla clients do client-side prediction when placing and removing blocks.
 * These predictions are wrong.
 * These methods are called by {@link io.github.theepicblock.polymc.mixins.block.ResyncImplementation} to resync the blocks with the server's state.
 */
public class BlockResyncManager {
    public static boolean shouldForceSync(BlockState sourceState, BlockState clientState, Direction direction) {
        Block block = clientState.getBlock();

        if (block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK || block == Blocks.MUSHROOM_STEM) {
            return true;
        }

        if (block == Blocks.NOTE_BLOCK) {
            return direction == Direction.UP;
        } else if (block == Blocks.TRIPWIRE) {
            if (sourceState == null) return direction.getAxis().isHorizontal();

            //Checks if the connected property for the block isn't what it should be
            //If the source block in that direction is string, it should be true. Otherwise false
            return direction.getAxis().isHorizontal() &&
                    clientState.get(ConnectingBlock.FACING_PROPERTIES.get(direction.getOpposite())) != (sourceState.getBlock() instanceof TripwireBlock);
        }
        return false;
    }

    public static void onBlockUpdate(BlockState sourceState, BlockPos pos, World world, ServerPlayerEntity player, List<BlockPos> exceptions) {
        BlockPos.Mutable mPos = new BlockPos.Mutable();

        for (Direction d : Direction.values()) {
            mPos.set(pos.getX() + d.getOffsetX(), pos.getY() + d.getOffsetY(), pos.getZ() + d.getOffsetZ());
            if (exceptions != null && exceptions.contains(mPos)) continue;
            BlockState state = world.getBlockState(mPos);
            BlockPoly poly = PolyMapProvider.getPolyMap(player).getBlockPoly(state.getBlock());
            if (poly != null) {
                BlockState clientState = poly.getClientBlock(state);
                if (BlockResyncManager.shouldForceSync(sourceState, clientState, d)) {
                    BlockPos nPos = mPos.toImmutable();
                    player.networkHandler.sendPacket(new BlockUpdateS2CPacket(nPos, state));
                    List<BlockPos> newExceptions;
                    if (exceptions == null) {
                        newExceptions = new ArrayList<>();
                        newExceptions.add(pos);
                    } else {
                        exceptions.add(pos);
                        newExceptions = exceptions;
                    }
                    onBlockUpdate(clientState, nPos, world, player, newExceptions);
                }
            }
        }
    }
}
