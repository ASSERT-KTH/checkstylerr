package io.gomint.server.world.leveldb;

/**
 * @author geNAZt
 * @version 1.0
 */
public enum Generators {

    /**
     * Normal generator for vanilla terrain
     */
    NORMAL( 1 ),

    /**
     * Layered generator, named "Flat" in MC:PE
     */
    FLAT( 2 );

    private final int id;

    /**
     * Construct a new Generators enum value
     *
     * @param id of the generator
     */
    Generators( int id ) {
        this.id = id;
    }

    public int id() {
        return this.id;
    }

    /**
     * Get a generators ID via its numeric representation
     *
     * @param id which we want to lookup
     * @return generators enum value or null when not found
     */
    public static Generators valueOf( int id ) {
        for ( Generators generators : values() ) {
            if ( generators.id == id ) {
                return generators;
            }
        }

        return null;
    }

}
