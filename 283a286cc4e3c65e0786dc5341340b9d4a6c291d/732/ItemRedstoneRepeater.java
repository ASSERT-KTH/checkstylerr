package io.gomint.inventory.item;

import io.gomint.GoMint;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 1
 */
public interface ItemRedstoneRepeater extends ItemStack<ItemRedstoneRepeater> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemRedstoneRepeater create( int amount ) {
        return GoMint.instance().createItemStack( ItemRedstoneRepeater.class, amount );
    }

}
