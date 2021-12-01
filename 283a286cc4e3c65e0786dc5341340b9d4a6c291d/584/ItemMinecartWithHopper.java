package io.gomint.inventory.item;

import io.gomint.GoMint;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface ItemMinecartWithHopper extends ItemStack<ItemMinecartWithHopper> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemMinecartWithHopper create( int amount ) {
        return GoMint.instance().createItemStack( ItemMinecartWithHopper.class, amount );
    }

}
