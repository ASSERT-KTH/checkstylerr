package io.github.robvanderleek.jlifx.packet;

import io.github.robvanderleek.jlifx.bulb.Bulb;
import io.github.robvanderleek.jlifx.bulb.BulbMeshFirmwareStatus;
import io.github.robvanderleek.jlifx.bulb.GatewayBulb;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PacketService {
    private static final Log LOG = LogFactory.getLog(PacketService.class);

    public void sendPowerManagementPacket(Bulb bulb, boolean on) throws IOException {
        Packet packet = new PowerManagementPacket(bulb.getMacAddress(), on);
        bulb.getGatewayBulb().sendPacket(packet);
    }

    public void sendColorManagementPacket(Bulb bulb, Color color, int fadetime, float brightness) {
        Packet packet = new ColorManagementPacket(bulb.getMacAddress(), color, fadetime, brightness);
        try {
            bulb.getGatewayBulb().sendPacket(packet);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public void sendSetDimAbsolutePacket(Bulb bulb, float brightness) throws IOException {
        Packet packet = new SetDimAbsolutePacket(brightness);
        bulb.getGatewayBulb().sendPacket(packet);
    }

    public List<StatusResponsePacket> sendStatusRequestPacket(GatewayBulb bulb) {
        Packet packet = new StatusRequestPacket();
        try {
            List<Packet> responsePackets = bulb.sendPacketAndGetResponses(packet);
            return responsePackets.stream().map(StatusResponsePacket::new).collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error(e);
            return Collections.emptyList();
        }
    }

    Packet sendWifiInfoRequestPacket(GatewayBulb bulb) throws IOException {
        Packet packet = new WifiInfoRequestPacket();
        return bulb.sendPacketAndGetResponse(packet);
    }

    public BulbMeshFirmwareStatus getMeshFirmwareStatus(GatewayBulb bulb) throws IOException {
        Packet packet = new MeshFirmwareRequestPacket();
        Packet responsePacket = bulb.sendPacketAndGetResponse(packet);
        return BulbMeshFirmwareStatus.fromPacket(responsePacket);
    }

}