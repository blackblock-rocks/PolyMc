package io.github.theepicblock.polymc.api.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class ItemStackPoly extends ItemStack {

    public static final ItemStackPoly EMPTY = new ItemStackPoly((Item)null);

    private ItemStack polyMcOriginalItemStack = null;

    public ItemStackPoly(ItemConvertible item) {
        super(item);
    }

    public ItemStackPoly(ItemConvertible item, int count) {
        super(item, count);
    }

    public void setPolyMcOriginalItemStack(ItemStack stack) {
        this.polyMcOriginalItemStack = stack;
    }

    public Optional<TooltipData> getTooltipData() {
        System.out.println("Getting ItemStackPoly tooltipdata!");

        if (this.polyMcOriginalItemStack == null) {
            return this.getItem().getTooltipData(this);
        } else {
            return this.getItem().getTooltipData(this.polyMcOriginalItemStack);
        }
    }

    public ItemStackPoly copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStackPoly itemStack = new ItemStackPoly(this.getItem(), this.getCount());
            itemStack.setCooldown(this.getCooldown());
            if (this.getTag() != null) {
                itemStack.setTag(this.getTag().copy());
            }

            return itemStack;
        }
    }

    public void saySomething() {
        System.out.println("HELLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
    }

}
