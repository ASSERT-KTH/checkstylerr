package io.gomint.server.resource;

/**
 * @author geNAZt
 * @version 1.0
 */
public enum ResourceResponseStatus {

    REFUSED,
    SEND_PACKS,
    HAVE_ALL_PACKS,
    COMPLETED;

    public static ResourceResponseStatus valueOf( int statusId ) {
        switch ( statusId ) {
            case 1:
                return REFUSED;
            case 2:
                return SEND_PACKS;
            case 3:
                return HAVE_ALL_PACKS;
            case 4:
                return COMPLETED;
        }

        return null;
    }

}
