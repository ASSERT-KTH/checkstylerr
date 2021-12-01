package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:mossy_cobblestone")
public class ItemMossyCobblestone extends ItemStack< io.gomint.inventory.item.ItemMossyCobblestone> implements io.gomint.inventory.item.ItemMossyCobblestone {

    @Override
    public ItemType itemType() {
        return ItemType.MOSSY_COBBLESTONE;
    }

}
