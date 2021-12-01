package io.gomint.server.entity.tileentity;

import io.gomint.server.inventory.item.Items;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.Block;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.data.BlockColor;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "Bed")
public class BedTileEntity extends TileEntity {

    private byte color = 14;

    /**
     * New bed
     *
     * @param block which created this tile
     */
    public BedTileEntity(Block block, Items items) {
        super( block, items );
    }

    @Override
    public void fromCompound( NBTTagCompound compound ) {
        super.fromCompound( compound );

        this.color = compound.getByte( "color", (byte) 0 );
    }

    public BlockColor getColor() {
        switch ( this.color ) {
            case 0:
                return BlockColor.WHITE;
            case 1:
                return BlockColor.ORANGE;
            case 2:
                return BlockColor.MAGENTA;
            case 3:
                return BlockColor.LIGHT_BLUE;
            case 4:
                return BlockColor.YELLOW;
            case 5:
                return BlockColor.LIME;
            case 6:
                return BlockColor.PINK;
            case 7:
                return BlockColor.GRAY;
            case 8:
                return BlockColor.LIGHT_GRAY;
            case 9:
                return BlockColor.CYAN;
            case 10:
                return BlockColor.PURPLE;
            case 11:
                return BlockColor.BLUE;
            case 12:
                return BlockColor.BROWN;
            case 13:
                return BlockColor.GREEN;
            case 14:
                return BlockColor.RED;
            default:
                return BlockColor.BLACK;
        }
    }

    public void setColor( BlockColor color ) {
        byte colorId = 0;
        switch ( color ) {
            case WHITE:
                colorId = 0;
                break;
            case ORANGE:
                colorId = 1;
                break;
            case MAGENTA:
                colorId = 2;
                break;
            case LIGHT_BLUE:
                colorId = 3;
                break;
            case YELLOW:
                colorId = 4;
                break;
            case LIME:
                colorId = 5;
                break;
            case PINK:
                colorId = 6;
                break;
            case GRAY:
                colorId = 7;
                break;
            case LIGHT_GRAY:
                colorId = 8;
                break;
            case CYAN:
                colorId = 9;
                break;
            case PURPLE:
                colorId = 10;
                break;
            case BLUE:
                colorId = 11;
                break;
            case BROWN:
                colorId = 12;
                break;
            case GREEN:
                colorId = 13;
                break;
            case RED:
                colorId = 14;
                break;
            case BLACK:
            default:
                colorId = 15;
        }

        this.color = colorId;
    }

    @Override
    public void toCompound( NBTTagCompound compound, SerializationReason reason ) {
        super.toCompound( compound, reason );

        compound.addValue( "id", "Bed" );
        compound.addValue( "color", this.color );
    }

    @Override
    public void update( long currentMillis, float dT ) {

    }

}
