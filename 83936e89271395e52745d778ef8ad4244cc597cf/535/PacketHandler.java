package io.gomint.server.network.handler;

import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.Packet;

/**
 * @param <T> type of packet this handler should handle
 * @author geNAZt
 * @version 1.0
 */
public interface PacketHandler<T extends Packet> {

    /**
     * Handle a incoming packet
     *
     * @param packet            The packet which did income
     * @param currentTimeMillis The time where the tick started
     * @param connection        The connection for which the packet did come
     */
    void handle( T packet, long currentTimeMillis, PlayerConnection connection ) throws Exception;

}
