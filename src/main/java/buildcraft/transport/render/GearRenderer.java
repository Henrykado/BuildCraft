package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import buildcraft.BuildCraftTransport;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class GearRenderer implements ISimpleBlockRenderingHandler {

    int renderId;

    public GearRenderer(int id) {
        this.renderId = id;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {

    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        if (block != BuildCraftTransport.gear) {
            return false;
        }

        Tessellator tessellator = Tessellator.instance;
        int l = world.getBlockMetadata(x, y, z);
        IIcon iicon = renderer.getBlockIcon(block, world, x, y, z, 0);
        // ((TileGear)world.getTileEntity(x, y, z)).sides;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        double d0 = (double) iicon.getMinU();
        double d1 = (double) iicon.getMinV();
        double d2 = (double) iicon.getMaxU();
        double d3 = (double) iicon.getMaxV();
        double d4 = 0.0625D;
        double x1 = x + 1;
        double x2 = x + 1;
        double x3 = x;
        double x4 = x;
        double z1 = z;
        double z2 = z + 1;
        double z3 = z + 1;
        double z4 = z;
        double y1 = (double) y + d4;
        double y2 = (double) y + d4;
        double y3 = (double) y + d4;
        double y4 = (double) y + d4;

        x1 = x + 1 * Math.cos(30) - z * Math.sin(30);
        x2 = x + 1 * Math.cos(30) - z + 1 * Math.sin(30);
        x3 = x * Math.cos(30) - z + 1 * Math.sin(30);
        x4 = x * Math.cos(30) - z * Math.sin(30);

        z1 = x + 1 * Math.sin(30) + z * Math.cos(30);
        z2 = x + 1 * Math.sin(30) + z + 1 * Math.cos(30);
        z3 = x * Math.sin(30) + z + 1 * Math.cos(30);
        z4 = x * Math.sin(30) + z * Math.cos(30);

        if (l != 1 && l != 2 && l != 3 && l != 7) {
            if (l == 8) {
                x1 = x2 = x;
                x3 = x4 = x + 1;
                z1 = z4 = z + 1;
                z2 = z3 = z;
            } else if (l == 9) {
                x1 = x;
                x4 = x;
                x2 = x + 1;
                x3 = x + 1;
                z1 = z;
                z2 = z;
                z3 = z + 1;
                z4 = z + 1;
            }
        } else {
            // x1 = x4 = (double)(x + 1);
            // x2 = x3 = (double)(x + 0);
            // z1 = z2 = (double)(z + 1);
            // z3 = z4 = (double)(z + 0);
        }

        if (l != 2 && l != 4) {
            if (l == 3 || l == 5) {
                ++y2;
                ++y3;
            }
        } else {
            ++y1;
            ++y4;
        }

        tessellator.addVertexWithUV(x1, y, z1, d2, d1);
        tessellator.addVertexWithUV(x2, y, z2, d2, d3);
        tessellator.addVertexWithUV(x3, y, z3, d0, d3);
        tessellator.addVertexWithUV(x4, y, z4, d0, d1);
        tessellator.addVertexWithUV(x4, y, z4, d0, d1);
        tessellator.addVertexWithUV(x3, y, z3, d0, d3);
        tessellator.addVertexWithUV(x2, y, z2, d2, d3);
        tessellator.addVertexWithUV(x1, y, z1, d2, d1);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
