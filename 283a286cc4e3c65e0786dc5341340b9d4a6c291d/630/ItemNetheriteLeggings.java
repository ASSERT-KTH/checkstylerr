package io.gomint.inventory.item;

import io.gomint.GoMint;

/**
 * @author KingAli
 * @version 1.0
 * @stability 3
 */
public interface ItemNetheriteLeggings extends ItemStack<ItemNetheriteLeggings> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemNetheriteLeggings create( int amount ) {
        return GoMint.instance().createItemStack( ItemNetheriteLeggings.class, amount );
    }
}
