package io.gomint.inventory.item;

import io.gomint.GoMint;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public interface ItemWoodenSlab extends ItemStack<ItemWoodenSlab>, ItemBurnable {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemWoodenSlab create( int amount ) {
        return GoMint.instance().createItemStack( ItemWoodenSlab.class, amount );
    }

}
