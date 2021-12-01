package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:enchanting_table", id = 116)
public class ItemEnchantmentTable extends ItemStack< io.gomint.inventory.item.ItemEnchantmentTable> implements io.gomint.inventory.item.ItemEnchantmentTable {

    @Override
    public ItemType itemType() {
        return ItemType.ENCHANTMENT_TABLE;
    }

}
