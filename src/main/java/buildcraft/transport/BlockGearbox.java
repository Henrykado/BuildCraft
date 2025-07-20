package buildcraft.transport;

import buildcraft.core.lib.block.BlockBuildCraft;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockGearbox extends BlockBuildCraft {
    protected BlockGearbox() {
        super(Material.wood);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileGearbox();
    }
}
