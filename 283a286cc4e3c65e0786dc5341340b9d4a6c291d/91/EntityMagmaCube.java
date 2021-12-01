package io.gomint.entity.monster;

import io.gomint.GoMint;
import io.gomint.entity.EntityLiving;

/**
 * @author LucGames
 * @version 1.0
 * @stability 3
 */
public interface EntityMagmaCube extends EntityLiving<EntityMagmaCube> {

    /**
     * Create a new entity magma cube with no config
     *
     * @return empty, fresh magma cube
     */
    static EntityMagmaCube create() {
        return GoMint.instance().createEntity( EntityMagmaCube.class );
    }

    /**
     * Set a new size for this entity. This changes the hitbox to factor * 0.51f (for both width and height).
     * Health is 2^factor. When the size changes the entity will be healed to its new maximum health.
     *
     * @param factor of this magma cube
     */
    EntityMagmaCube sizeFactor(int factor );

}
