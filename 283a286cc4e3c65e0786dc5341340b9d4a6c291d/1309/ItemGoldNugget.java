package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:gold_nugget", id = 371 )
 public class ItemGoldNugget extends ItemStack< io.gomint.inventory.item.ItemGoldNugget> implements io.gomint.inventory.item.ItemGoldNugget {



    @Override
    public ItemType itemType() {
        return ItemType.GOLD_NUGGET;
    }

}
