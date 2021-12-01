package io.gomint.inventory.item;

import io.gomint.GoMint;

/**
 * @author Kaooot
 * @version 1.0
 * @stability 3
 */
public interface ItemDriedKelp extends ItemStack<ItemDriedKelp> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemDriedKelp create( int amount ) {
        return GoMint.instance().createItemStack( ItemDriedKelp.class, amount );
    }
}
