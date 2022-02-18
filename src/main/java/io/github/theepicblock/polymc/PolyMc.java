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
package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.impl.PolyMcCommands;
import io.github.theepicblock.polymc.impl.generator.Generator;
import io.github.theepicblock.polymc.impl.misc.BlockIdRemapper;
import io.github.theepicblock.polymc.impl.misc.logging.Log4JWrapper;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class PolyMc implements ModInitializer {
    public static final String MODID = "polymc";
    public static final SimpleLogger LOGGER = new Log4JWrapper(LogManager.getLogger("PolyMc"));
    private static PolyMap map;

    /**
     * Builds the poly map, this should only be run when all blocks/items have been registered.
     * This will be called by PolyMc when the worlds are generated.
     * @deprecated this is an internal method you shouldn't call.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void generatePolyMap() {
        PolyRegistry registry = new PolyRegistry();

        // Register default global ItemPolys
        Generator.addDefaultGlobalItemPolys(registry);

        // Let mods register polys via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerPolys(registry);
        }

        // Auto generate the rest
        Generator.generateMissing(registry);

        map = registry.build();
    }

    /**
     * Returns the main PolyMap, this is the one PolyMc creates and populates at server startup via various hooks and generators.
     * As an example, it is passed to {@link PolyMcEntrypoint#registerPolys(PolyRegistry)} before being built.
     * @return the main PolyMap generated by PolyMc
     * @deprecated When this PolyMap is meant to affect players, use {@link io.github.theepicblock.polymc.api.misc.PolyMapProvider#getPolyMap(ServerPlayerEntity)} instead.
     */
    @Deprecated
    public static PolyMap getMainMap() {
        if (map == null) {
            throw new NullPointerException("Tried to access the PolyMap before it was initialized");
        }
        return map;
    }

    @Override
    public void onInitialize() {
        PolyMcCommands.registerCommands();
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            // Updates the PolyMap that the player uses as soon as the network handler is initialized
            // see ServerPlayNetworkHandler.<init>
            ((PolyMapProvider)(handler.player)).refreshUsedPolyMap();
        });

        if (ConfigManager.getConfig().remapVanillaBlockIds) {
            BlockIdRemapper.remapFromInternalList();
        }
    }
}
