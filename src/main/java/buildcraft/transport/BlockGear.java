package buildcraft.transport;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockGear extends BlockBuildCraft implements IKinecticActor {

    public BlockGear() {
        super(Material.wood);

        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
    }

    public void passRotation(World world, ForgeDirection source, int x, int y, int z, List<Vec3> iteratedBlocks) {
        iteratedBlocks.add(Vec3.createVectorHelper(x, y, z));

        for (ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
            if (facing == source) continue;
            Block block = world.getBlock(x + facing.offsetX, y + facing.offsetY, facing.offsetZ);
            if (block instanceof IKinecticActor) {
                ((IKinecticActor) block).passRotation(
                        world,
                        facing.getOpposite(),
                        x + facing.offsetY,
                        y + facing.offsetY,
                        z + facing.offsetZ,
                        iteratedBlocks);
            }
        }
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileGear();
    }
}
