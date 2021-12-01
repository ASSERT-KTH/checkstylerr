package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:end_gateway", id = 209)
public class ItemEndGateway extends ItemStack< io.gomint.inventory.item.ItemEndGateway> implements io.gomint.inventory.item.ItemEndGateway {

    @Override
    public ItemType itemType() {
        return ItemType.END_GATEWAY;
    }

}
