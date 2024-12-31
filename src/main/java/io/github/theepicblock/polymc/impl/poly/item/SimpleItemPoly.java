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
package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.JModelOverride;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.TreeMap;

/**
 * The most standard ItemPoly implementation
 */
public class SimpleItemPoly implements ItemPoly {
    protected final Item clientItem;

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param target         the serverside items that can be chosen from
     */
    public SimpleItemPoly(Item target) {
        this.clientItem = target;
    }

    /**
     * Adds PolyMc specific tags to the item to display correctly on the client.
     * These shouldn't change depending on the stack as this method will be cached.
     * For un-cached tags, use {@link #getClientItem(ItemStack, ServerPlayerEntity, ItemLocation)}
     */
    protected void addCustomTagsToItem(ItemStack stack) {}

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack getClientItem(ItemStack input, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        var output = Util.copyWithItem(input, clientItem, player);

        {
            var current = input.get(DataComponentTypes.USE_COOLDOWN);
            if (current == null) {
                output.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.00001f, Optional.of(Registries.ITEM.getId(input.getItem()))));
            } else if (current.cooldownGroup().isEmpty()) {
                output.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(current.seconds(), Optional.of(Registries.ITEM.getId(input.getItem()))));
            }
        }

        this.addCustomTagsToItem(output);
        output.set(DataComponentTypes.ITEM_NAME, input.getItem().getName(input));

        return output;
    }

    @Override
    public String getDebugInfo(Item item) {
        return "item:" + clientItem.getTranslationKey();
    }
}
