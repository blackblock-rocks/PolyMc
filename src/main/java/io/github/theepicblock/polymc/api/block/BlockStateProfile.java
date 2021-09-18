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
package io.github.theepicblock.polymc.api.block;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.poly.block.ConditionalSimpleBlockPoly;
import io.github.theepicblock.polymc.impl.poly.block.PropertyRetainingReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.*;
import net.minecraft.state.property.Properties;

import net.minecraft.util.math.Direction;
import net.minecraft.state.property.BooleanProperty;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Defines a group of blocks and blockstates.
 * Is used by {@link BlockStateManager} to define which blockstates can be used and which not.
 * Also includes info on how to handle these blockstate {@link #onFirstRegister}.
 */
@SuppressWarnings("PointlessBooleanExpression")
public class BlockStateProfile {
    public final Block[] blocks;
    public final Predicate<BlockState> filter;
    public final BiConsumer<Block,PolyRegistry> onFirstRegister;
    public final String name;

    public BlockStateProfile(String name, Block[] blocks, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        this.blocks = blocks;
        this.filter = filter;
        this.onFirstRegister = onFirstRegister;
        this.name = name;
    }
    public BlockStateProfile(String name, Block block, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        this.blocks = new Block[]{block};
        this.filter = filter;
        this.onFirstRegister = onFirstRegister;
        this.name = name;
    }


    //BLOCK LISTS
    private static final Block[] LEAVES_BLOCKS = {Blocks.ACACIA_LEAVES,Blocks.BIRCH_LEAVES,Blocks.DARK_OAK_LEAVES,Blocks.JUNGLE_LEAVES,Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES};
    private static final Block[] NO_COLLISION_BLOCKS = {Blocks.SUGAR_CANE,
            Blocks.ACACIA_SAPLING, Blocks.BIRCH_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.OAK_SAPLING,
            Blocks.TRIPWIRE};
    private static final Block[] DOOR_BLOCKS = {Blocks.ACACIA_DOOR,Blocks.BIRCH_DOOR,Blocks.DARK_OAK_DOOR,Blocks.JUNGLE_DOOR,Blocks.OAK_DOOR,Blocks.SPRUCE_DOOR,Blocks.CRIMSON_DOOR,Blocks.WARPED_DOOR};
    private static final Block[] TRAPDOOR_BLOCKS = {Blocks.ACACIA_TRAPDOOR,Blocks.BIRCH_TRAPDOOR,Blocks.DARK_OAK_TRAPDOOR,Blocks.JUNGLE_TRAPDOOR,Blocks.OAK_TRAPDOOR,Blocks.SPRUCE_TRAPDOOR,Blocks.CRIMSON_TRAPDOOR,Blocks.WARPED_TRAPDOOR};

    //FILTERS
    private static final Predicate<BlockState> DEFAULT_FILTER = (blockState) -> blockState != blockState.getBlock().getDefaultState();
    private static final Predicate<BlockState> NO_COLLISION_FILTER = (blockState) -> {
        if (blockState.getBlock() == Blocks.TRIPWIRE) {
            return isStringUseable(blockState);
        } else {
            return DEFAULT_FILTER.test(blockState);
        }
    };
    private static final Predicate<BlockState> ALWAYS_TRUE_FILTER = (blockState) -> true;
    private static final Predicate<BlockState> FARMLAND_FILTER = (blockState) -> {
        int moisture = blockState.get(FarmlandBlock.MOISTURE);
        return moisture != 0 && moisture != 7;
    };
    private static final Predicate<BlockState> POWERED_FILTER = (blockState) -> blockState.get(Properties.POWERED) == true;

    /**
     * Some chorus plant states can be used as non-opaque blocks
     * (Though there are collision box issues)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    private static final Predicate<BlockState> CHORUS_PLANT_FILTER = (blockState) -> {

        boolean down = _hasDirection(blockState, Direction.DOWN);
        boolean up = _hasDirection(blockState, Direction.UP);
        boolean east = _hasDirection(blockState, Direction.EAST);
        boolean north = _hasDirection(blockState, Direction.NORTH);
        boolean south = _hasDirection(blockState, Direction.SOUTH);
        boolean west = _hasDirection(blockState, Direction.WEST);

        int east_int = east ? 1 : 0;
        int north_int = north ? 1 : 0;
        int south_int = south ? 1 : 0;
        int west_int = west ? 1 : 0;

        int sides = east_int + north_int + south_int + west_int;

        if (!up && !down) {
            if (sides == 0) {
                return true;
            }

            return sides > 2;
        }

        // A middle-piece can not have any sides
        if (up && down && sides > 0) {
            return true;
        }

        // A branch piece can only have 1 side
        return up && sides > 1;
    };

    /**
     * There are 9 Brown Mushroom states that generate in vanilla survival,
     * leave those 9 states alone (+ the all-sides block)
     * (Some other states are obtainable by shearing with other mushroom blocks,
     *  but that will be disabled)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    private static final Predicate<BlockState> BROWN_MUSHROOM_FILTER = (blockState) -> {

        boolean down = _hasDirection(blockState, Direction.DOWN);
        boolean east = _hasDirection(blockState, Direction.EAST);
        boolean north = _hasDirection(blockState, Direction.NORTH);
        boolean south = _hasDirection(blockState, Direction.SOUTH);
        boolean up = _hasDirection(blockState, Direction.UP);
        boolean west = _hasDirection(blockState, Direction.WEST);

        if (!down) {

            if (!east && !north && !south && !west) {
                // Up & no up are in use
                return false;
            }

            if (up) {
                if (west && !east && !north && !south) {
                    return false;
                }

                if (south && !east && !north && !west) {
                    return false;
                }

                if (south && west && !east && !north) {
                    return false;
                }

                if (north && !east && !south && !west) {
                    return false;
                }

                if (north && west && !east && !south) {
                    return false;
                }

                if (east && !north && !south && !west) {
                    return false;
                }

                if (east && south && !north && !west) {
                    return false;
                }

                if (east && north && !south && !west) {
                    return false;
                }
            }
        } else if (east && north && south && up && west) {
            return false;
        }

        return true;
    };

    /**
     * There are 17 Red Mushroom states that generate in vanilla survival,
     * leave those states alone (+ the all-sides block)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    private static final Predicate<BlockState> RED_MUSHROOM_FILTER = (blockState) -> {

        boolean down = _hasDirection(blockState, Direction.DOWN);
        boolean east = _hasDirection(blockState, Direction.EAST);
        boolean north = _hasDirection(blockState, Direction.NORTH);
        boolean south = _hasDirection(blockState, Direction.SOUTH);
        boolean up = _hasDirection(blockState, Direction.UP);
        boolean west = _hasDirection(blockState, Direction.WEST);

        if (!down) {

            if (west && !east && !north && !south) {
                return up;
            }

            if (!east && !north && !up && !west) {
                return south;
            }

            if (up && !east && !north && !south && !west) {
                return false;
            }

            if (south && west && !east && !north && !up) {
                return false;
            }

            if (!east && !north && !south && !west) {
                return false;
            }

            if (south && up && west && !east && !north) {
                return false;
            }

            if (north && !east && !south && !up) {
                return false;
            }

            if (north && up && !east && !south) {
                return false;
            }

            if (east && !north && !south && !up && !west) {
                return false;
            }

            if (east && up && !north && !south && !west) {
                return false;
            }

            if (east && south && !north && !up && !west) {
                return false;
            }

            if (east && south && up && !north && !west) {
                return false;
            }

            if (east && north && !south && !up && !west) {
                return false;
            }

            if (east && north && up && !south && !west) {
                return false;
            }
        } else if (east && north && south && up && west) {
            return false;
        }

        return true;
    };

    /**
     * There are 2 Mushroom Stem states that generate in vanilla survival,
     * leave those states alone + the all-sides block and the ones that can be sheared
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    private static final Predicate<BlockState> STEM_MUSHROOM_FILTER = (blockState) -> {

        boolean down = _hasDirection(blockState, Direction.DOWN);
        boolean up = _hasDirection(blockState, Direction.UP);

        // Ignore stems
        if (!down && !up) {
            return false;
        }

        boolean east = _hasDirection(blockState, Direction.EAST);
        boolean north = _hasDirection(blockState, Direction.NORTH);
        boolean south = _hasDirection(blockState, Direction.SOUTH);
        boolean west = _hasDirection(blockState, Direction.WEST);

        // Don't use the all-sides block
        if (down && east && north && south && up && west) {
            return false;
        }

        return true;
    };

    /**
     * Helper method to check if the given Direction is enabled on the given BlockState
     * (Used for mushroom blocks)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    private static boolean _hasDirection(BlockState state, Direction direction) {
        BooleanProperty booleanProperty = _getProperty(direction);
        return state.contains(booleanProperty) && (Boolean)state.get(booleanProperty);
    }

    /**
     * Helper method to get a certain Direction BooleanProperty
     * (Used for mushroom blocks)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     */
    private static BooleanProperty _getProperty(Direction direction) {
        return (BooleanProperty)ConnectingBlock.FACING_PROPERTIES.get(direction);
    }

    //ON FIRST REGISTERS
    private static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
    private static final BiConsumer<Block,PolyRegistry> NO_COLLISION_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (block == Blocks.TRIPWIRE) {
            polyRegistry.registerBlockPoly(block, new BlockPoly() {
                @Override
                public BlockState getClientBlock(BlockState input) {
                    return input.with(Properties.POWERED, false).with(Properties.DISARMED,false);
                }
                @Override public void addToResourcePack(Block block, ResourcePackMaker pack) {}
            });
        } else {
            polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
        }
    };
    private static final BiConsumer<Block,PolyRegistry> PETRIFIED_OAK_SLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new PropertyRetainingReplacementPoly(Blocks.OAK_SLAB));
    private static final BiConsumer<Block,PolyRegistry> FARMLAND_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.FARMLAND.getDefaultState(), FARMLAND_FILTER));
    private static final BiConsumer<Block,PolyRegistry> POWERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (BlockPolyPredicate)(block2) -> block2.with(Properties.POWERED, false));
    private static final BiConsumer<Block,PolyRegistry> BROWN_MUSHROOM_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.BROWN_MUSHROOM_BLOCK.getDefaultState(), BROWN_MUSHROOM_FILTER));
    private static final BiConsumer<Block,PolyRegistry> RED_MUSHROOM_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.RED_MUSHROOM_BLOCK.getDefaultState(), RED_MUSHROOM_FILTER));
    private static final BiConsumer<Block,PolyRegistry> STEM_MUSHROOM_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.MUSHROOM_STEM.getDefaultState(), STEM_MUSHROOM_FILTER));
    private static final BiConsumer<Block,PolyRegistry> CHORUS_PLANT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.CHORUS_PLANT.getDefaultState(), CHORUS_PLANT_FILTER));

    //PROFILES
    public static final BlockStateProfile BROWN_MUSHROOM_BLOCK_PROFILE = new BlockStateProfile("brown mushroom block", Blocks.BROWN_MUSHROOM_BLOCK, BROWN_MUSHROOM_FILTER, BROWN_MUSHROOM_ON_FIRST_REGISTER);
    public static final BlockStateProfile RED_MUSHROOM_BLOCK_PROFILE = new BlockStateProfile("red mushroom block", Blocks.RED_MUSHROOM_BLOCK, RED_MUSHROOM_FILTER, RED_MUSHROOM_ON_FIRST_REGISTER);
    public static final BlockStateProfile STEM_MUSHROOM_BLOCK_PROFILE = new BlockStateProfile("stem mushroom block", Blocks.MUSHROOM_STEM, STEM_MUSHROOM_FILTER, STEM_MUSHROOM_ON_FIRST_REGISTER);
    public static final BlockStateProfile NOTE_BLOCK_PROFILE = getProfileWithDefaultFilter("note block", Blocks.NOTE_BLOCK);
    public static final BlockStateProfile CHORUS_PLANT_BLOCK_PROFILE = new BlockStateProfile("chorus plant block", Blocks.CHORUS_PLANT, CHORUS_PLANT_FILTER, CHORUS_PLANT_ON_FIRST_REGISTER);


    public static final BlockStateProfile LEAVES_PROFILE = getProfileWithDefaultFilter("leaves", LEAVES_BLOCKS);
    public static final BlockStateProfile NO_COLLISION_PROFILE = new BlockStateProfile("blocks without collisions", NO_COLLISION_BLOCKS, NO_COLLISION_FILTER, NO_COLLISION_ON_FIRST_REGISTER);
    public static final BlockStateProfile PETRIFIED_OAK_SLAB_PROFILE = new BlockStateProfile("petrified oak slab", Blocks.PETRIFIED_OAK_SLAB, ALWAYS_TRUE_FILTER, PETRIFIED_OAK_SLAB_ON_FIRST_REGISTER);
    public static final BlockStateProfile FARMLAND_PROFILE = new BlockStateProfile("farmland", Blocks.FARMLAND, FARMLAND_FILTER, FARMLAND_ON_FIRST_REGISTER);
    public static final BlockStateProfile CACTUS_PROFILE = getProfileWithDefaultFilter("cactus", Blocks.CACTUS);
    public static final BlockStateProfile KELP_PROFILE = getProfileWithDefaultFilter("kelp", Blocks.KELP);
    public static final BlockStateProfile DOOR_PROFILE = new BlockStateProfile("door", DOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile TRAPDOOR_PROFILE = new BlockStateProfile("trapdoor", TRAPDOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_DOOR_PROFILE = new BlockStateProfile("metal_door", Blocks.IRON_DOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_TRAPDOOR_PROFILE = new BlockStateProfile("metal_trapdoor", Blocks.IRON_TRAPDOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);

    //OTHER CODE
    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block[] blocks) {
        return new BlockStateProfile(name, blocks, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block block) {
        return new BlockStateProfile(name, block, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    private static boolean isStringUseable(BlockState state) {
        return  state.get(Properties.POWERED) == true ||
                state.get(TripwireBlock.DISARMED) == true;
    }
}
