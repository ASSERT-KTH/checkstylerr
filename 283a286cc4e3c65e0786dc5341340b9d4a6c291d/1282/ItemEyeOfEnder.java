package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:ender_eye", id = 381 )
 public class ItemEyeOfEnder extends ItemStack< io.gomint.inventory.item.ItemEyeOfEnder> implements io.gomint.inventory.item.ItemEyeOfEnder {



    @Override
    public ItemType itemType() {
        return ItemType.EYE_OF_ENDER;
    }

}
