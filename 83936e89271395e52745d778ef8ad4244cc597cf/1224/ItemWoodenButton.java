package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public interface ItemWoodenButton extends ItemStack, ItemBurnable {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
     */
    static ItemWoodenButton create( int amount ) {
        return GoMint.instance().createItemStack( ItemWoodenButton.class, amount );
    }

    /**
     * Get the type of wood from which this button has been made
     *
     * @return type of wood
     */
    LogType getWoodType();

    /**
     * Set the type of wood for this button
     *
     * @param logType type of wood
     */
    void setWoodType(LogType logType);

}
