package io.gomint.inventory;

import io.gomint.inventory.item.ItemStack;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public interface PlayerInventory extends Inventory<PlayerInventory> {

    /**
     * Get the item in hand
     *
     * @return item stack which the client holds in hand
     */
    ItemStack<?> itemInHand();

    /**
     * Get the index of the item in hand in this inventory
     *
     * @return index of item in hand
     */
    byte itemInHandSlot();

    /**
     * Set the item in hand slot for the player
     *
     * @param slot which should be the new item in hand
     */
    PlayerInventory itemInHandSlot(byte slot );

}
