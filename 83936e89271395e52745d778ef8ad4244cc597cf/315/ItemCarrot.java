package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:carrot", id = 391 )
public class ItemCarrot extends ItemFood implements io.gomint.inventory.item.ItemCarrot {



    @Override
    public ItemType getItemType() {
        return ItemType.CARROT;
    }

    @Override
    public float getSaturation() {
        return 0.6f;
    }

    @Override
    public float getHunger() {
        return 3;
    }

}