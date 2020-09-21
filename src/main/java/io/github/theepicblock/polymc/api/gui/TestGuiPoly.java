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
package io.github.theepicblock.polymc.api.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.DefaultedList;

public class TestGuiPoly implements GuiPoly {
    private int amountOfSlots;

    @Override
    public ScreenHandlerType<?> getClientSideType() {
        return ScreenHandlerType.GENERIC_9X3;
    }

    @Override
    public DefaultedList<ItemStack> getClientSideStackList(DefaultedList<ItemStack> input) {
        amountOfSlots = input.size()-36;
        DefaultedList<ItemStack> b = DefaultedList.ofSize(36+27,ItemStack.EMPTY);
        for (int i = 0; i < amountOfSlots; i++) {
            b.set(i,input.get(i));
        }
        for (int i = 27; i < 36+27; i++) {
            b.set(i,input.get(amountOfSlots+i-27));
        }
        return b;
    }

    @Override
    public int remapSlot(int input) {
        if (input < amountOfSlots) return input;
        return input-amountOfSlots+36;
    }
}
