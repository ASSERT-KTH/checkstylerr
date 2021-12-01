package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:lit_redstone_ore")
public class ItemGlowingRedstoneOre extends ItemStack< io.gomint.inventory.item.ItemGlowingRedstoneOre> implements io.gomint.inventory.item.ItemGlowingRedstoneOre {

    @Override
    public ItemType itemType() {
        return ItemType.GLOWING_REDSTONE_ORE;
    }

}
