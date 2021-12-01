package io.gomint.inventory.item;

import io.gomint.GoMint;

/**
 * @author Kaooot
 * @version 1.0
 * @stability 1
 */
public interface ItemUnderwaterTorch extends ItemStack<ItemUnderwaterTorch> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemUnderwaterTorch create( int amount ) {
        return GoMint.instance().createItemStack( ItemUnderwaterTorch.class, amount );
    }
}
