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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The most standard ItemPoly implementation
 */
public class CustomModelDataPoly implements ItemPoly {
    protected final ItemStack cachedClientItem;
    protected final int cmdValue;
    protected final Item base;

    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base) {
        this(registerManager, base, CustomModelDataManager.DEFAULT_ITEMS);
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param target          the serverside item will be of this type
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base, Item target) {
        this(registerManager, base, new Item[]{target});
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param targets         the serverside items that can be chosen from
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base, Item[] targets) {
        Pair<Item,Integer> pair = registerManager.requestCMD(targets);
        cmdValue = pair.getRight();
        cachedClientItem = new ItemStack(pair.getLeft());
        this.base = base;
        addCustomTagsToItem(cachedClientItem);
    }

    /**
     * Adds PolyMc specific tags to the item to display correctly on the client.
     * These shouldn't change depending on the stack as this method will be cached.
     * For un-cached tags, use {@link #getClientItem(ItemStack, ItemLocation)}
     */
    protected void addCustomTagsToItem(ItemStack stack) {
        var item = stack.getItem();

        NbtCompound tag = stack.getOrCreateNbt();
        tag.putInt("CustomModelData", cmdValue);
        stack.setNbt(tag);

        if (!tag.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
            tag.put("AttributeModifiers", new NbtList());
            try {
                for (var slotType : EquipmentSlot.values()) {
                    // This will only include the default attributes
                    var attributes = base.getAttributeModifiers(slotType);
                    for (var attribute : attributes.entries()) {
                        stack.addAttributeModifier(attribute.getKey(), attribute.getValue(), slotType);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack getClientItem(ItemStack input, @Nullable ItemLocation location) {
        ItemStack serverItem = cachedClientItem;
        if (input.hasNbt()) {
            serverItem = cachedClientItem.copy();
            serverItem.setNbt(input.getNbt().copy());

            // Doing this removes the custom tags, so we should add that again
            addCustomTagsToItem(serverItem);
        }

        // Add custom tooltips. Don't bother showing them if the item's not in the inventory
        if (Util.isSectionVisible(input, ItemStack.TooltipSection.ADDITIONAL) && isInventory(location)) {
            Entity holder = input.getHolder(); // This is not usually guaranteed to get the correct player. It works here though.

            var tooltips = new ArrayList<Text>(0);
            try {
                input.getItem().appendTooltip(input, holder instanceof PlayerEntity player ? player.world : null, tooltips, TooltipContext.Default.NORMAL);
            } catch (Exception | NoClassDefFoundError ignored) {}

            if (!tooltips.isEmpty()) {
                NbtList list = new NbtList();
                for (Text line : tooltips) {
                    if (line instanceof MutableText mText) {
                        // Cancels the styling of the lore
                        var style = line.getStyle();
                        style = style.withItalic(style.isItalic());
                        if (style.getColor() == null) {
                            style = style.withColor(style.getColor());
                        }
                        line = mText.setStyle(style);
                    }

                    list.add(NbtString.of(Text.Serializer.toJson(line)));
                }

                // serveritem is always either the cached item or a copy, so it's okay to modify
                NbtCompound display = serverItem.getOrCreateSubNbt("display");
                display.put("Lore", list);
            }
        }

        // Always set the name again in case the item can change its name based on NBT data
        if (!input.hasCustomName()) {
            var name = input.getName();

            if (location == ItemLocation.TRACKED_DATA) {
                // Don't override the name, otherwise it'll show up on item frames
                serverItem.setCustomName(null);
            } else  {
                // Override the style to make sure the client does not render
                // the custom name in italics, and uses the correct rarity format
                if (name instanceof MutableText mutableText) {
                    mutableText.setStyle(name.getStyle().withItalic(false).withColor(input.getRarity().formatting));
                }

                serverItem.setCustomName(name);
            }
        }

        serverItem.setCount(input.getCount());
        serverItem.setBobbingAnimationTime(input.getBobbingAnimationTime());
        return serverItem;
    }

    private static boolean isInventory(@Nullable ItemLocation location) {
        return location == ItemLocation.INVENTORY || location == null; // Be conservative and say that unknown locations are in inventory too
    }

    @Override
    public void addToResourcePack(Item item, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        // We need to copy over the modded item model into the pack (including all of the textures it references)
        // Then we need to include an override into a vanilla item model that links to that modded item model
        var moddedItemId = Registry.ITEM.getId(item);
        var moddedItemModel = moddedResources.getItemModel(moddedItemId.getNamespace(), moddedItemId.getPath());
        if (moddedItemModel == null) {
            logger.error("Can't find item model for "+moddedItemId+", can't generate resources for it");
            // Set the override to have the barrier model to signify it's missing
            moddedItemId = Registry.ITEM.getId(Items.BARRIER);
        } else {
            pack.setItemModel(moddedItemId.getNamespace(), moddedItemId.getPath(), moddedItemModel);
            pack.importRequirements(moddedResources, moddedItemModel, logger);
        }

        var clientitemId = Registry.ITEM.getId(this.cachedClientItem.getItem());

        // Get the json for the vanilla item, so we can inject an override into it
        var clientItemModel = pack.getOrDefaultVanillaItemModel(clientitemId.getNamespace(), clientitemId.getPath());
        // Add the override
        clientItemModel.getOverrides().add(JModelOverride.ofCMD(cmdValue, ResourceConstants.itemLocation(moddedItemId)));

        // Check if the modded item model has overrides
        if (moddedItemModel != null && !moddedItemModel.getOverridesReadOnly().isEmpty()) {
            // The modded item has overrides, we should remove them and use them as basis for the client item model instead
            for (var override : moddedItemModel.getOverridesReadOnly()) {
                var predicates = new HashMap<>(override.predicates());
                predicates.put("custom_model_data", (float)cmdValue);
                clientItemModel.getOverrides().add(new JModelOverride(predicates, override.model()));
            }
            moddedItemModel.getOverrides().clear();
        }
    }

    @Override
    public String getDebugInfo(Item item) {
        return "CMD: " + Util.expandTo(cmdValue, 3) + ", item:" + cachedClientItem.getTranslationKey();
    }
}
