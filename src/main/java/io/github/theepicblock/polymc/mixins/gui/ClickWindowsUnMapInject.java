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
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ClickWindowsUnMapInject {
    @Shadow public ServerPlayerEntity player;

    /**
     * @reason we need to unmap the slot id so it points to the correct value again.
     */
    @Inject(method = "onClickWindow(Lnet/minecraft/network/packet/c2s/play/ClickWindowC2SPacket;)V", at = @At("HEAD"))
    public void onClickWindowInject(ClickWindowC2SPacket packet, CallbackInfo ci) {
        try {
            GuiPoly poly = PolyMc.getMap().getGuiPoly(this.player.currentScreenHandler.getType());
            if (poly != null) {
                ClickWindowPacketAccessor accessor = (ClickWindowPacketAccessor)packet;
                accessor.setSlot(poly.unmapSlot(packet.getSlot()));
            }
        } catch (UnsupportedOperationException ignored) {}
    }
}
