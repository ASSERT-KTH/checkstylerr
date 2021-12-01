package org.bukkit.entity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import com.destroystokyo.paper.entity.RangedEntity;

/**
 * Represents a Skeleton.
 */
public interface Skeleton extends Monster, RangedEntity { // Paper

    /**
     * Gets the current type of this skeleton.
     *
     * @return Current type
     * @deprecated should check what class instance this is
     */
    @Deprecated
    @NotNull
    public SkeletonType getSkeletonType();

    /**
     * @param type type
     * @deprecated Must spawn a new subtype variant
     */
    @Deprecated
    @Contract("_ -> fail")
    public void setSkeletonType(SkeletonType type);

    /*
     * @deprecated classes are different types
     */
    @Deprecated
    public enum SkeletonType {

        /**
         * Standard skeleton type.
         */
        NORMAL,
        /**
         * Wither skeleton. Generally found in Nether fortresses.
         */
        WITHER,
        /**
         * Stray skeleton. Generally found in ice biomes. Shoots tipped arrows.
         */
        STRAY;
    }
}
