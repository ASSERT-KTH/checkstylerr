package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:bone", id = 352)
public class ItemBone extends ItemStack implements io.gomint.inventory.item.ItemBone {

    @Override
    public ItemType getItemType() {
        return ItemType.BONE;
    }

}
