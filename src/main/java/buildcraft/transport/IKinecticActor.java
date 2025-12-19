package buildcraft.transport;

import java.util.List;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IKinecticActor {

    void passRotation(World world, ForgeDirection source, int x, int y, int z, List<Vec3> iteratedBlocks);
}
