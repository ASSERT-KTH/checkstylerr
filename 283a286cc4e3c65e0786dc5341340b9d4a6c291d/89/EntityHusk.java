package io.gomint.entity.monster;

import io.gomint.GoMint;
import io.gomint.entity.EntityAgeable;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface EntityHusk extends EntityAgeable<EntityHusk> {

    /**
     * Create a new entity husk with no config
     *
     * @return empty, fresh husk
     */
    static EntityHusk create() {
        return GoMint.instance().createEntity( EntityHusk.class );
    }

}
