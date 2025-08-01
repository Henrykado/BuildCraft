/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import buildcraft.core.lib.gui.slots.SlotPhantom;

public class SlotPackager extends SlotPhantom {

    public SlotPackager(IInventory iinventory, int slotIndex, int posX, int posY) {
        super(iinventory, slotIndex, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack != null;
    }

    @Override
    public boolean canShift() {
        return false;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
