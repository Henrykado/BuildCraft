package buildcraft.transport;

import buildcraft.core.lib.block.BlockBuildCraft;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockGearbox extends BlockBuildCraft {
    public BlockGearbox() {
        super(Material.wood);

        setStepSound(soundTypeWood);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileGearbox();
    }
}
