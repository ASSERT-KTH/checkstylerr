package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:porkchop", id = 319 )
public class ItemRawPorkchop extends ItemFood<io.gomint.inventory.item.ItemRawPorkchop> implements io.gomint.inventory.item.ItemRawPorkchop {



    @Override
    public float getSaturation() {
        return 0.3f;
    }

    @Override
    public float getHunger() {
        return 3;
    }

    @Override
    public ItemType itemType() {
        return ItemType.RAW_PORKCHOP;
    }

}
