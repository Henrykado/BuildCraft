package buildcraft.transport;

import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import cofh.api.energy.IEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileGearbox extends TileBuildCraft implements IEnergyHandler {
    public TileGearbox() {
        setBattery(new RFBattery(160, 160, 160));
    }

    public void sendPower() {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = getTile(dir);
            //if (tile instanceof TileGear)
        }
    }
}
