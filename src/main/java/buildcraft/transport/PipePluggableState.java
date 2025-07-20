package buildcraft.transport;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.utils.ConnectionMatrix;
import io.netty.buffer.ByteBuf;

public class PipePluggableState implements ISerializable {

    private PipePluggable[] pluggables = new PipePluggable[6];
    private final ConnectionMatrix pluggableMatrix = new ConnectionMatrix();

    private boolean[] disconnected = new boolean[6];

    public PipePluggableState() {}

    public boolean[] getDisconnectedSides() {
        return disconnected;
    }

    public void setDisconnectedSides(boolean[] disconnected) {
        this.disconnected = disconnected;
    }

    public PipePluggable[] getPluggables() {
        return pluggables;
    }

    public void setPluggables(PipePluggable[] pluggables) {
        this.pluggables = pluggables;
    }

    @Override
    public void writeData(ByteBuf data) {
        this.pluggableMatrix.clean();

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            this.pluggableMatrix.setConnected(dir, pluggables[dir.ordinal()] != null);
        }

        this.pluggableMatrix.writeData(data);

        for (PipePluggable p : pluggables) {
            if (p != null) {
                data.writeShort(PipeManager.pipePluggables.indexOf(p.getClass()));
                p.writeData(data);
            }
        }
        for (boolean b : disconnected) {
            data.writeBoolean(b);
        }
    }

    @Override
    public void readData(ByteBuf data) {
        this.pluggableMatrix.readData(data);

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (this.pluggableMatrix.isConnected(dir)) {
                try {
                    Class<? extends PipePluggable> pc = PipeManager.pipePluggables.get(data.readUnsignedShort());
                    if (pluggables[dir.ordinal()] == null || pc != pluggables[dir.ordinal()].getClass()) {
                        PipePluggable p = pc.newInstance();
                        pluggables[dir.ordinal()] = p;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (pluggables[dir.ordinal()] != null) {
                    pluggables[dir.ordinal()].readData(data);
                }
            } else {
                pluggables[dir.ordinal()] = null;
            }
        }
        for (int i = 0; i < disconnected.length; i++) {
            disconnected[i] = data.readBoolean();
        }
    }
}
