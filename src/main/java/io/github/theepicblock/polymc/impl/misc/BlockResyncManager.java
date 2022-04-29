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
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
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
    public static boolean shouldForceSync(World world, ServerPlayerEntity player, BlockPos oppositePos, BlockPos sourcePos, BlockState sourceState, BlockState clientState, Direction direction) {
        Block block = clientState.getBlock();

        // Mushroom blocks only trigger updates when a non-sheared face touches another mushroom block of the same type
        if (block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK || block == Blocks.MUSHROOM_STEM) {

            // See if the client-side blockstate has a non-sheared face on the opposite side of the updated block
            Boolean hasNonShearedFace = BlockStateProfile.hasBooleanDirection(clientState, direction.getOpposite());

            if (hasNonShearedFace) {

                // Now get the server-side blockstate of the opposite side
                BlockState oppositeState = world.getBlockState(oppositePos);

                // Try to get the player's BlockPoly for this opposite side
                BlockPoly poly = PolyMapProvider.getPolyMap(player).getBlockPoly(oppositeState.getBlock());

                BlockState oppositeClientState = null;

                if (poly == null) {
                    // There is no poly map, so the server-side state should be the same as the client-side's one
                    oppositeClientState = oppositeState;
                } else {
                    // There is a polymap, so the client-side state differs from the server-side one
                    oppositeClientState = poly.getClientBlock(oppositeState);
                }

                // Get the opposite block as used on the client-side
                Block oppositeClientBlock = oppositeClientState.getBlock();

                // If the 2 touching blocks on the client side are NOT the same,
                // no update will occur, and we can safely skip it.
                return block == oppositeClientBlock;
            }

            return false;
        }

        // Wall blocks update whenever a block updates around them
        if (block instanceof WallBlock) {
            return true;
        }

        if (block == Blocks.NOTE_BLOCK) {
            return direction == Direction.UP;
        } else if (block == Blocks.MYCELIUM || block == Blocks.PODZOL) {
            return direction == Direction.DOWN;
        } else if (block == Blocks.TRIPWIRE) {
            if (sourceState == null) return direction.getAxis().isHorizontal();

            //Checks if the connected property for the block isn't what it should be
            //If the source block in that direction is string, it should be true. Otherwise false
            return direction.getAxis().isHorizontal() &&
                    clientState.get(ConnectingBlock.FACING_PROPERTIES.get(direction.getOpposite())) != (sourceState.getBlock() instanceof TripwireBlock);
        }
        return false;
    }

    public static void onBlockUpdate(BlockState sourceState, BlockPos sourcePos, World world, ServerPlayerEntity player, List<BlockPos> checkedBlocks) {
        if (checkedBlocks == null) checkedBlocks = new ArrayList<>();
        _onBlockUpdate(sourceState, sourcePos, world, player, checkedBlocks,0);
    }

    private static void _onBlockUpdate(BlockState sourceState, BlockPos sourcePos, World world, ServerPlayerEntity player, List<BlockPos> checkedBlocks, int level) {

        // Huge volumes of poly-blocks could cause a stack overflow,
        // so set a limit on how many times this method can recurse
        if (level > 1000) {
            return;
        }

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (Direction direction : Direction.values()) {
            pos.set(sourcePos.getX() + direction.getOffsetX(), sourcePos.getY() + direction.getOffsetY(), sourcePos.getZ() + direction.getOffsetZ());

            if (checkedBlocks.contains(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            BlockPoly poly = Util.tryGetPolyMap(player).getBlockPoly(state.getBlock());

            if (poly != null) {
                BlockState clientState = poly.getClientBlock(state);

                if (BlockResyncManager.shouldForceSync(world, player, sourcePos, pos, sourceState, clientState, direction)) {
                    BlockPos newPos = pos.toImmutable();
                    player.networkHandler.sendPacket(new BlockUpdateS2CPacket(newPos, state));
                    checkedBlocks.add(sourcePos);

                    _onBlockUpdate(clientState, newPos, world, player, checkedBlocks, level + 1);
                }
            } else {
                Block block = state.getBlock();

                // Always send wallblock updates
                if (block instanceof WallBlock) {
                    BlockPos newPos = pos.toImmutable();
                    player.networkHandler.sendPacket(new BlockUpdateS2CPacket(newPos, state));
                    checkedBlocks.add(sourcePos);

                    _onBlockUpdate(state, newPos, world, player, checkedBlocks, level + 1);
                }
            }

            // If the lower half of a door is interacted with, we should check the upper half as well
            boolean isUpperDoor = direction == Direction.UP && state.getBlock() instanceof DoorBlock && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
            if (isUpperDoor) {
                checkedBlocks.add(sourcePos);
                _onBlockUpdate(null, pos, world, player, checkedBlocks, level + 1);
            }
        }
    }
}
