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
package io.github.theepicblock.polymc.mixins.gui;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayerEntity.class)
public class InventoryStackInjectImplementation {
    @ModifyVariable(method = "onHandlerRegistered(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/util/collection/DefaultedList;)V", at = @At("HEAD"))
    public DefaultedList<ItemStack> handlerRegisterModifyStack(ScreenHandler handler, DefaultedList<ItemStack> input) {
        GuiPoly poly = PolyMc.getMap().getGuiPoly(handler.getType());
        if (poly == null) return input;
        return poly.getClientSideStackList(input);
    }
}
