/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import buildcraft.transport.utils.ConnectionMatrix;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.render.BCSimpleBlockRenderingHandler;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.pipes.PipeStructureCobblestone;

public class PipeRendererWorld extends BCSimpleBlockRenderingHandler {

    public static int renderPass = -1;
    public static float zFightOffset = 1F / 4096F;
    private static final double[] CHEST_BB = new double[] { 0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375 };

    public boolean renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, TileGenericPipe tile, int x, int y,
            int z) {
        PipeRenderState state = tile.renderState;
        IIconProvider icons = tile.getPipeIcons();
        FakeBlock fakeBlock = FakeBlock.INSTANCE;
        int glassColor = tile.getPipeColor();

        if (icons == null) {
            return false;
        }

        boolean rendered = false; // Set to true if you're *certain* the pass rendered something.

        if (renderPass == 0 || glassColor >= 0) {
            // Pass 0 handles the pipe texture, pass 1 handles the transparent stained glass
            int connectivity = state.pipeConnectionMatrix.getMask();
            float[] dim = new float[6];

            if (renderPass == 1) {
                fakeBlock.setColor(ColorUtils.getRGBColor(glassColor));
            } else if (glassColor >= 0 && tile.getPipe() instanceof PipeStructureCobblestone) {
                if (glassColor == 0) {
                    fakeBlock.setColor(0xDFDFDF);
                } else {
                    fakeBlock.setColor(ColorUtils.getRGBColor(glassColor));
                }
            }

            // render the unconnected pipe faces of the center block (if any)

            if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
                resetToCenterDimensions(dim);

                if (renderPass == 0) {
                    fakeBlock.getTextureState()
                            .set(icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN)));
                } else {
                    fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeStainedOverlay.getIcon());
                }

                fixForRenderPass(dim);

                renderTwoWayBlock(renderblocks, fakeBlock, x, y, z, dim, connectivity ^ 0x3f);
                rendered = true;
            }

            // render the connecting pipe faces
            for (int dir = 0; dir < 6; dir++) {
                int mask = 1 << dir;

                if ((connectivity & mask) == 0) {
                    continue; // no connection towards dir
                }

                // center piece offsets
                resetToCenterDimensions(dim);

                // extend block towards dir as it's connected to there
                dim[dir / 2] = dir % 2 == 0 ? 0 : CoreConstants.PIPE_MAX_POS;
                dim[dir / 2 + 3] = dir % 2 == 0 ? CoreConstants.PIPE_MIN_POS : 1;

                // the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5
                // -> 001111
                int renderMask = (3 << (dir & 0x6)) ^ 0x3f;

                fixForRenderPass(dim);

                // render sub block
                if (renderPass == 0) {
                    fakeBlock.getTextureState().set(
                            icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.VALID_DIRECTIONS[dir])));
                } else {
                    fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeStainedOverlay.getIcon());
                }

                renderTwoWayBlock(renderblocks, fakeBlock, x, y, z, dim, renderMask);
                rendered = true;

                // Render connection to block
                if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
                    fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeItemConnection.getIcon());
                    ForgeDirection side = ForgeDirection.getOrientation(dir);
                    int px = x + side.offsetX;
                    int py = y + side.offsetY;
                    int pz = z + side.offsetZ;
                    Block block = iblockaccess.getBlock(px, py, pz);
                    if (!(block instanceof BlockGenericPipe) && !block.isOpaqueCube() && block.getMaterial() != Material.air) {
                        double[] blockBB;
                        if (block instanceof BlockChest) {
                            // work around what seems to be a vanilla bug?
                            blockBB = CHEST_BB;
                        } else {
                            block.setBlockBoundsBasedOnState(iblockaccess, px, py, pz);

                            blockBB = new double[] { block.getBlockBoundsMinY(), block.getBlockBoundsMinX(),
                                    block.getBlockBoundsMinZ(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxX(),
                                    block.getBlockBoundsMaxZ() };
                        }
                        blockBB[3] = Math.abs(blockBB[3] - 1D);
                        blockBB[4] = Math.abs(blockBB[4] - 1D);
                        blockBB[5] = Math.abs(blockBB[5] - 1D);

                        blockBB = new double[6];

                        renderBlockConnection(renderblocks, dir, fakeBlock, x, y, z, blockBB);
                    }
                }
            }

            fakeBlock.setColor(0xFFFFFF);
        }

        renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (tile.hasPipePluggable(dir)) {
                PipePluggable p = tile.getPipePluggable(dir);
                IPipePluggableRenderer r = p.getRenderer();
                if (r != null) {
                    r.renderPluggable(renderblocks, tile.getPipe(), dir, p, fakeBlock, renderPass, x, y, z);
                }
            }
        }

        return rendered;
    }

    private void fixForRenderPass(double[] dim) {
        if (renderPass == 1) {
            for (int i = 0; i < 3; i++) {
                dim[i] += zFightOffset;
            }

            for (int i = 3; i < 6; i++) {
                dim[i] -= zFightOffset;
            }
        }
    }
    private void fixForRenderPass(float[] dim) {
        if (renderPass == 1) {
            for (int i = 0; i < 3; i++) {
                dim[i] += zFightOffset;
            }

            for (int i = 3; i < 6; i++) {
                dim[i] -= zFightOffset;
            }
        }
    }

    private void resetToCenterDimensions(float[] dim) {
        for (int i = 0; i < 3; i++) {
            dim[i] = CoreConstants.PIPE_MIN_POS;
            dim[i + 3] = CoreConstants.PIPE_MAX_POS;
        }
    }

    private void renderBlockConnection(RenderBlocks renderblocks, int dir, FakeBlock stateHost, int x, int y, int z, double[] bounds) {
        double MIN = CoreConstants.PIPE_MIN_POS - (1 / 16d); // default min x/y/z value, 3 / 16
        double MAX = CoreConstants.PIPE_MAX_POS + (1 / 16d); // default max x/y/z value, 13 / 16
        double[] renderBounds;
        switch (dir)
        {
            case 0: // DOWN
                renderblocks.uvRotateEast = 3;
                renderblocks.uvRotateWest = 3;
                renderblocks.uvRotateSouth = 3;
                renderblocks.uvRotateNorth = 3;
                renderBounds = new double[] {MIN, 0.0D - bounds[4], MIN, MAX, 0.25D - bounds[4], MAX};
                break;
            case 1: // UP
                renderBounds = new double[] {MIN, 0.75D + bounds[1], MIN, MAX, 1.0D + bounds[1], MAX};
                break;
            case 2: // NORTH, Z-
                renderblocks.uvRotateSouth = 1;
                renderblocks.uvRotateNorth = 2;
                renderBounds = new double[] {MIN, MIN, 0.0D - bounds[5], MAX, MAX, 0.25D - bounds[5]};
                break;
            case 3: // SOUTH, Z+
                renderblocks.uvRotateSouth = 2;
                renderblocks.uvRotateNorth = 1;
                renderblocks.uvRotateTop = 3;
                renderblocks.uvRotateBottom = 3;
                renderBounds = new double[] {MIN, MIN, 0.75 + bounds[2], MAX, MAX, 1.0D + bounds[2]};
                break;
            case 4: // WEST, X-
                renderblocks.uvRotateEast = 1;
                renderblocks.uvRotateWest = 2;
                renderblocks.uvRotateTop = 2;
                renderblocks.uvRotateBottom = 1;
                renderBounds = new double[] {0.0D - bounds[3], MIN, MIN, 0.25D - bounds[3], MAX, MAX};
                break;
            default: // EAST, X+
                renderblocks.uvRotateEast = 2;
                renderblocks.uvRotateWest = 1;
                renderblocks.uvRotateTop = 1;
                renderblocks.uvRotateBottom = 2;
                renderBounds = new double[] {0.75D + bounds[0], MIN, MIN, 1.0D + bounds[0], MAX, MAX};
        }
        fixForRenderPass(renderBounds);

        renderblocks.setRenderBounds(renderBounds[0], renderBounds[1], renderBounds[2], renderBounds[3], renderBounds[4], renderBounds[5]);
        //stateHost.setBlockBounds((float)renderblocks.renderMinX, (float)renderblocks.renderMinY, (float)renderblocks.renderMinZ, (float)renderblocks.renderMaxX, (float)renderblocks.renderMaxY, (float)renderblocks.renderMaxZ);
        renderblocks.renderStandardBlockWithColorMultiplier(stateHost, x, y, z, 1f, 1f, 1f);
        renderblocks.uvRotateEast = 0;
        renderblocks.uvRotateWest = 0;
        renderblocks.uvRotateSouth = 0;
        renderblocks.uvRotateNorth = 0;
        renderblocks.uvRotateTop = 0;
        renderblocks.uvRotateBottom = 0;
        //renderblocks.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    /**
     * Render a block with normal and inverted vertex order so back face culling doesn't have any effect.
     */
    private void renderTwoWayBlock(RenderBlocks renderblocks, FakeBlock stateHost, int x, int y, int z, float[] dim,
            int mask) {
        assert mask != 0;

        int c = stateHost.getBlockColor();
        float r = ((c & 0xFF0000) >> 16) / 255.0f;
        float g = ((c & 0x00FF00) >> 8) / 255.0f;
        float b = (c & 0x0000FF) / 255.0f;

        stateHost.setRenderMask(mask);
        renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
        renderblocks.renderStandardBlockWithColorMultiplier(stateHost, x, y, z, r, g, b);

        stateHost.setRenderMask((mask & 0x15) << 1 | (mask & 0x2a) >> 1); // pairwise swapped mask
        renderblocks.setRenderBounds(dim[5], dim[3], dim[4], dim[2], dim[0], dim[1]);
        renderblocks.renderStandardBlockWithColorMultiplier(stateHost, x, y, z, r * 0.67f, g * 0.67f, b * 0.67f);

        stateHost.setRenderAllSides();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        // Done with a special item renderer
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        boolean rendered = false;
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileGenericPipe) {
            TileGenericPipe pipeTile = (TileGenericPipe) tile;
            rendered = renderPipe(renderer, world, pipeTile, x, y, z);
        }

        if (!rendered) {
            fixEmptyAlphaPass(x, y, z);
        }

        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public int getRenderId() {
        return TransportProxy.pipeModel;
    }
}
